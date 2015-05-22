package com.tughi.memoria;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SyncService extends IntentService {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_NOTES,
            Exercises.COLUMN_PRACTICE_TIME,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_SYNC_ID,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;
    private static final int EXERCISE_NOTES = 3;
    private static final int EXERCISE_PRACTICE_TIME = 4;
    private static final int EXERCISE_RATING = 5;
    private static final int EXERCISE_SYNC_ID = 6;

    public static final String APPLICATION_USER = "f2e38c2e-fffb-11e4-a322-1697f925ec7b";

    public static final String HTTP_METHOD_GET = "GET";
    public static final String HTTP_METHOD_POST = "POST";
    public static final String HTTP_METHOD_PUT = "PUT";

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        long syncTime = System.currentTimeMillis();

        int updates = 0;
        try {
            updates = downloadExercises(syncTime);

            uploadExercises(syncTime);
        } catch (IOException exception) {
            Log.e(getClass().getName(), "Download failed!", exception);
        } finally {
            if (updates > 0) {
                getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
            }
        }
    }

    private int downloadExercises(long syncTime) throws IOException {
        int result = 0;

        String params = "limit=1000&where=" + URLEncoder.encode("{\"user\":\"" + APPLICATION_USER + "\"}", "UTF-8");
        HttpURLConnection connection = openConnection("https://api.parse.com/1/classes/Exercise?" + params, HTTP_METHOD_GET);

        InputStream in = connection.getInputStream();
        try {
            JsonFactory jsonFactory = new MappingJsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(in);

            JsonToken currentJsonToken;

            currentJsonToken = jsonParser.nextToken();
            if (currentJsonToken != JsonToken.START_OBJECT) {
                throw new IOException("The root must be an object");
            }

            while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonParser.getCurrentName();

                currentJsonToken = jsonParser.nextToken();
                if (fieldName.equals("results")) {
                    if (currentJsonToken == JsonToken.START_ARRAY) {
                        ContentResolver contentResolver = getContentResolver();
                        String updateSelection = Exercises.COLUMN_SCOPE + " = ? AND " + Exercises.COLUMN_PRACTICE_TIME + " <= CAST(? AS INTEGER)";
                        String[] updateSelectionArgs = new String[2];

                        while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                            Exercise exercise = jsonParser.readValueAs(Exercise.class);

                            ContentValues values = new ContentValues();
                            values.put(Exercises.COLUMN_SCOPE, exercise.scope);
                            values.put(Exercises.COLUMN_DEFINITION, exercise.definition);
                            values.put(Exercises.COLUMN_NOTES, exercise.notes);
                            values.put(Exercises.COLUMN_RATING, exercise.rating);
                            values.put(Exercises.COLUMN_PRACTICE_TIME, exercise.practiceTime);
                            values.put(Exercises.COLUMN_SYNC_ID, exercise.objectId);
                            values.put(Exercises.COLUMN_SYNC_TIME, syncTime);

                            updateSelectionArgs[0] = exercise.scope;
                            updateSelectionArgs[1] = Long.toString(exercise.practiceTime);
                            result += contentResolver.update(Exercises.CONTENT_SYNC_URI, values, updateSelection, updateSelectionArgs);
                        }
                    } else {
                        throw new IOException("The 'results' should have been an array");
                    }
                } else {
                    // skip
                    jsonParser.skipChildren();
                }
            }
        } finally {
            in.close();
        }

        return result;
    }

    private void uploadExercises(long syncTime) {
        String selection = Exercises.COLUMN_SYNC_TIME + " < " + syncTime;
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, selection, null, EXERCISES_SORT_ORDER);
        if (cursor.moveToFirst()) {
            do {
                try {
                    HttpURLConnection connection;
                    if (cursor.isNull(EXERCISE_SYNC_ID)) {
                        connection = openConnection("https://api.parse.com/1/classes/Exercise", HTTP_METHOD_POST);
                    } else {
                        connection = openConnection("https://api.parse.com/1/classes/Exercise/" + cursor.getString(EXERCISE_SYNC_ID), HTTP_METHOD_PUT);
                    }

                    Exercise exercise = new Exercise();
                    exercise.scope = cursor.getString(EXERCISE_SCOPE);
                    exercise.definition = cursor.getString(EXERCISE_DEFINITION);
                    exercise.notes = cursor.getString(EXERCISE_NOTES);
                    exercise.rating = cursor.getInt(EXERCISE_RATING);
                    exercise.practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME);
                    exercise.user = APPLICATION_USER;

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(connection.getOutputStream(), exercise);

                    JsonNode response = objectMapper.readTree(connection.getInputStream());
                    Log.d(getClass().getName(), "response: " + response);
                } catch (IOException exception) {
                    Log.e(getClass().getName(), "Upload failed!", exception);
                }
            } while (cursor.moveToNext());
        }
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestProperty("X-Parse-Application-Id", "HnyRQiFVVgeyFPhMGX6mGpTTVXojtW7YkkaIL9YX");
        connection.setRequestProperty("X-Parse-REST-API-Key", "2iqeHuTq3f6Mklow9DT5LUDFJatBbBuSs23Pl5Wk");

        connection.setRequestMethod(method);
        if (HTTP_METHOD_POST.equals(method) || HTTP_METHOD_PUT.equals(method)) {
            connection.setRequestProperty("Content-Type", "application/json");
        }
        return connection;
    }

    @JsonIgnoreProperties({"createdAt", "updatedAt"})
    private static class Exercise {
        public String objectId;
        public String scope;
        public String definition;
        public String notes;
        public int rating;
        public long practiceTime;
        public String user;
    }

}
