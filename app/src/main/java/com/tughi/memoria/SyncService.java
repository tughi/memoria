package com.tughi.memoria;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SyncService extends IntentService {

    private static final String SERVICE_NAME = SyncService.class.getSimpleName();

    private static final String HTTP_METHOD_GET = "GET";

    private static final String PREFERENCE_EXERCISES_LAST_UPDATED_TIME = "exercises_last_updated_time";
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
            updateContent(serverUrl);
        } catch (IOException exception) {
            Log.e(getClass().getName(), "Download failed!", exception);
        } finally {
            getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
    }

    private void updateContent(String serverUrl) throws IOException {
        long lastUpdatedTime = preferences.getLong(PREFERENCE_EXERCISES_LAST_UPDATED_TIME, 0);
        HttpURLConnection connection = openConnection(serverUrl + "/api/content?since=" + lastUpdatedTime, HTTP_METHOD_GET);

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
                if (currentJsonToken != JsonToken.START_OBJECT) {
                    throw new IOException("The root must be an object");
                }

                while (jsonParser.nextToken() == JsonToken.FIELD_NAME) {
                    String fieldName = jsonParser.getCurrentName();
                    switch (fieldName) {
                        case "lessons":
                            updateLessons(jsonParser);
                            break;
                        case "exercises":
                            updateExercises(jsonParser);
                            break;
                        case "lastUpdatedTime":
                            if (jsonParser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
                                throw new IOException("Number expected");
                            }

                            lastUpdatedTime = jsonParser.readValueAs(Long.class);

                            preferences.edit()
                                    .putLong(PREFERENCE_EXERCISES_LAST_UPDATED_TIME, lastUpdatedTime)
                                    .apply();
                    }
                }

                /* FIXME
                String deleteSelection = Exercises.COLUMN_SYNC_TIME + " != CAST(? AS INTEGER)";
                String[] deleteSelectionArgs = { Long.toString(syncTime) };
                contentResolver.delete(Exercises.CONTENT_SYNC_URI, deleteSelection, deleteSelectionArgs);
                */
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

    private void updateLessons(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
            throw new IOException("Array expected");
        }

        if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            Object[] keys = jsonParser.readValueAs(Object[].class);
            if (keys.length != 2 || !"id".equals(keys[0]) || !"title".equals(keys[1])) {
                throw new IOException("Unexpected lesson keys");
            }

            final ContentResolver contentResolver = getContentResolver();

            final ContentValues lessonSync = new ContentValues();
            final ContentValues lessonUser = new ContentValues();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                Object[] values = jsonParser.readValueAs(Object[].class);

                lessonSync.put(Lessons.COLUMN_ID, ((Number) values[0]).longValue());
                lessonSync.put(Lessons.COLUMN_TITLE, (String) values[1]);

                contentResolver.insert(Lessons.CONTENT_SYNC_URI, lessonSync);

                lessonUser.put(Lessons.COLUMN_ID, ((Number) values[0]).longValue());

                contentResolver.insert(Lessons.CONTENT_USER_URI, lessonUser);
            }
        }
    }

    private void updateExercises(JsonParser jsonParser) throws IOException {
        if (jsonParser.nextToken() != JsonToken.START_ARRAY) {
            throw new IOException("Array expected");
        }

        if (jsonParser.nextToken() != JsonToken.END_ARRAY) {
            Object[] keys = jsonParser.readValueAs(Object[].class);
            if (keys.length != 5 || !"id".equals(keys[0]) || !"lessonId".equals(keys[1]) || !"japanese".equals(keys[2]) || !"translation".equals(keys[3]) || !"notes".equals(keys[4])) {
                throw new IOException("Unexpected lesson keys");
            }

            final ContentResolver contentResolver = getContentResolver();

            final ContentValues exerciseSync = new ContentValues();
            final ContentValues exerciseUser = new ContentValues();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                Object[] values = jsonParser.readValueAs(Object[].class);

                String scope = (String) values[2];
                StringBuilder scopeLetters = new StringBuilder(scope.length());
                for (char c : scope.toCharArray()) {
                    if (Character.isLetterOrDigit(c)) {
                        scopeLetters.append(c);
                    }
                }

                exerciseSync.put(Exercises.COLUMN_ID, ((Number) values[0]).longValue());
                exerciseSync.put(Exercises.COLUMN_LESSON_ID, ((Number) values[1]).longValue());
                exerciseSync.put(Exercises.COLUMN_SCOPE, scope);
                exerciseSync.put(Exercises.COLUMN_SCOPE_LETTERS, scopeLetters.toString());
                exerciseSync.put(Exercises.COLUMN_DEFINITION, (String) values[3]);
                exerciseSync.put(Exercises.COLUMN_NOTES, (String) values[4]);
                // FIXME
//                exerciseSync.put(Exercises.COLUMN_RATING, exerciseSync.rating);
//                exerciseSync.put(Exercises.COLUMN_PRACTICE_TIME, exerciseSync.practiceTime);
//                exerciseSync.put(Exercises.COLUMN_DISABLED, exerciseSync.disabled);
//                exerciseSync.put(Exercises.COLUMN_SYNC_TIME, syncTime);

                contentResolver.insert(Exercises.CONTENT_SYNC_URI, exerciseSync);

                exerciseUser.put(Exercises.COLUMN_ID, ((Number) values[0]).longValue());
                contentResolver.insert(Exercises.CONTENT_USER_URI, exerciseUser);
            }
        }
    }

    private HttpURLConnection openConnection(String url, String method) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");
        return connection;
    }

}
