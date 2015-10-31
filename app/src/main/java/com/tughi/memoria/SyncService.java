package com.tughi.memoria;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import java.util.HashMap;
import java.util.Map;

public class SyncService extends IntentService {

    private static final String SERVICE_NAME = SyncService.class.getSimpleName();

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_CREATED_TIME,
            Exercises.COLUMN_UPDATED_TIME,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_NOTES,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_PRACTICE_TIME,
            Exercises.COLUMN_SYNC_TIME,
    };
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_CREATED_TIME = 1;
    private static final int EXERCISE_UPDATED_TIME = 2;
    private static final int EXERCISE_SCOPE = 3;
    private static final int EXERCISE_SCOPE_LETTERS = 4;
    private static final int EXERCISE_DEFINITION = 5;
    private static final int EXERCISE_NOTES = 6;
    private static final int EXERCISE_RATING = 7;
    private static final int EXERCISE_PRACTICE_TIME = 8;
    private static final int EXERCISE_SYNC_TIME = 9;

    private static final String BASE_URL_PATH = "/api/v1/exercises";

    private static final String HTTP_METHOD_GET = "GET";
    private static final String HTTP_METHOD_POST = "POST";
    private static final String HTTP_METHOD_PATCH = "PATCH";

    private static final String PREFERENCE_EXERCISES_ETAG = "exercises_etag";

    private SharedPreferences preferences;

    public SyncService() {
        super(SERVICE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        preferences = getSharedPreferences(SERVICE_NAME, MODE_PRIVATE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String serverUrl = preferences.getString(PreferencesActivity.PREFERENCE_SERVER_URL, null);
        if (serverUrl == null) {
            // sync disabled
            return;
        }

        try {
            uploadExercises(serverUrl);

            downloadExercises(serverUrl);
        } catch (IOException exception) {
            Log.e(getClass().getName(), "Download failed!", exception);
        } finally {
            getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
    }

    private void uploadExercises(String serverUrl) {
        String selection = Exercises.COLUMN_SYNC_TIME + " IS NULL OR " + Exercises.COLUMN_SYNC_TIME + " < " + Exercises.COLUMN_UPDATED_TIME;
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, selection, null, null);
        if (cursor == null) {
            return;
        }

        if (cursor.moveToFirst()) {
            do {
                try {
                    Map<String, Object> exercise = new HashMap<>();
                    exercise.put(Exercises.COLUMN_UPDATED_TIME, cursor.getLong(EXERCISE_UPDATED_TIME));
                    exercise.put(Exercises.COLUMN_SCOPE, cursor.getString(EXERCISE_SCOPE));
                    exercise.put(Exercises.COLUMN_SCOPE_LETTERS, cursor.getString(EXERCISE_SCOPE_LETTERS));
                    exercise.put(Exercises.COLUMN_DEFINITION, cursor.getString(EXERCISE_DEFINITION));
                    exercise.put(Exercises.COLUMN_NOTES, cursor.getString(EXERCISE_NOTES));
                    exercise.put(Exercises.COLUMN_RATING, cursor.getInt(EXERCISE_RATING));
                    exercise.put(Exercises.COLUMN_PRACTICE_TIME, cursor.getLong(EXERCISE_PRACTICE_TIME));

                    HttpURLConnection connection;
                    if (cursor.isNull(EXERCISE_SYNC_TIME)) {
                        connection = openConnection(serverUrl + BASE_URL_PATH, HTTP_METHOD_POST);
                        exercise.put(Exercises.COLUMN_CREATED_TIME, cursor.getLong(EXERCISE_CREATED_TIME));
                    } else {
                        connection = openConnection(serverUrl + BASE_URL_PATH + "/" + cursor.getString(EXERCISE_ID), HTTP_METHOD_PATCH);
                    }

                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.writeValue(connection.getOutputStream(), exercise);

                    JsonNode response = objectMapper.readTree(connection.getInputStream());
                    Log.d(getClass().getName(), "response: " + response);
                } catch (IOException exception) {
                    Log.e(getClass().getName(), "Upload failed!", exception);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

    private void downloadExercises(String serverUrl) throws IOException {
        HttpURLConnection connection = openConnection(serverUrl + BASE_URL_PATH, HTTP_METHOD_GET);

        String exercisesETag = preferences.getString(PREFERENCE_EXERCISES_ETAG, null);
        if (exercisesETag != null) {
            connection.addRequestProperty("If-None-Match", exercisesETag);
        }

        try {
            int responseCode = connection.getResponseCode();
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_MODIFIED:
                    // sync not required
                    return;
                default:
                    throw new IOException("Unexpected response code: (" + responseCode + ") " + connection.getResponseMessage());
            }

            InputStream in = connection.getInputStream();
            try {
                JsonFactory jsonFactory = new MappingJsonFactory();
                JsonParser jsonParser = jsonFactory.createParser(in);

                JsonToken currentJsonToken;

                currentJsonToken = jsonParser.nextToken();
                if (currentJsonToken != JsonToken.START_ARRAY) {
                    throw new IOException("The root must be an array");
                }

                long syncTime = System.currentTimeMillis();
                ContentResolver contentResolver = getContentResolver();

                ContentValues values = new ContentValues();
                while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                    ServerExercise exercise = jsonParser.readValueAs(ServerExercise.class);

                    values.put(Exercises.COLUMN_ID, exercise.id);
                    values.put(Exercises.COLUMN_CREATED_TIME, exercise.createdTime);
                    values.put(Exercises.COLUMN_UPDATED_TIME, exercise.updatedTime);
                    values.put(Exercises.COLUMN_SCOPE, exercise.scope);
                    values.put(Exercises.COLUMN_SCOPE_LETTERS, exercise.scopeLetters);
                    values.put(Exercises.COLUMN_DEFINITION, exercise.definition);
                    values.put(Exercises.COLUMN_NOTES, exercise.notes);
                    values.put(Exercises.COLUMN_RATING, exercise.rating);
                    values.put(Exercises.COLUMN_PRACTICE_TIME, exercise.practiceTime);
                    values.put(Exercises.COLUMN_SYNC_TIME, syncTime);

                    contentResolver.insert(Exercises.CONTENT_SYNC_URI, values);

                    values.clear();
                }

                String deleteSelection = Exercises.COLUMN_SYNC_TIME + " != CAST(? AS INTEGER)";
                String[] deleteSelectionArgs = {Long.toString(syncTime)};
                contentResolver.delete(Exercises.CONTENT_SYNC_URI, deleteSelection, deleteSelectionArgs);
            } finally {
                in.close();
            }

            preferences.edit()
                    .putString(PREFERENCE_EXERCISES_ETAG, connection.getHeaderField("ETag"))
                    .apply();
        } finally {
            connection.disconnect();
        }
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

    private static class ServerExercise {

        public long id;

        @JsonProperty(Exercises.COLUMN_CREATED_TIME)
        public long createdTime;

        @JsonProperty(Exercises.COLUMN_UPDATED_TIME)
        public long updatedTime;

        public String scope;

        @JsonProperty(Exercises.COLUMN_SCOPE_LETTERS)
        public String scopeLetters;

        public String definition;

        public String notes;

        public int rating;

        @JsonProperty(Exercises.COLUMN_PRACTICE_TIME)
        public long practiceTime;

    }

}
