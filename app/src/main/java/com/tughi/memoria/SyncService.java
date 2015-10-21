package com.tughi.memoria;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles the synchronization of the local database with Google Drive.
 */
public class SyncService extends IntentService {

    public static final String ACTION_PULL = BuildConfig.APPLICATION_ID + ".intent.action.PULL";
    public static final String ACTION_PUSH = BuildConfig.APPLICATION_ID + ".intent.action.PUSH";

    private static final String EXERCISES_FILE = "exercises.json";

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
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_ID;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;
    private static final int EXERCISE_NOTES = 3;
    private static final int EXERCISE_PRACTICE_TIME = 4;
    private static final int EXERCISE_RATING = 5;
    private static final int EXERCISE_SYNC_ID = 6;

    private GoogleApiClient googleApiClient;
    private DriveId backupFileId;

    public SyncService() {
        super(SyncService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = ((Application) getApplication()).getGoogleApiClient();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!googleApiClient.isConnected()) {
            Log.w(getClass().getName(), "GoogleApiClient is not connected!");
            return;
        }

        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);

        if (backupFileId == null) {
            // check if file already exists
            Query query = new Query.Builder()
                    .addFilter(Filters.eq(SearchableField.TITLE, EXERCISES_FILE))
                    .build();
            DriveApi.MetadataBufferResult metadataBufferResult = appFolder.queryChildren(googleApiClient, query)
                    .await();

            for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                backupFileId = metadata.getDriveId();
            }

            metadataBufferResult.release();
        }

        if (backupFileId == null) {
            // create empty file
            MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                    .setTitle(EXERCISES_FILE)
                    .setMimeType("application/json")
                    .build();
            backupFileId = appFolder.createFile(googleApiClient, changeSet, null)
                    .await()
                    .getDriveFile()
                    .getDriveId();
        }

        try {
            switch (intent.getAction()) {
                case ACTION_PULL:
                    downloadExercises();
                    break;
                case ACTION_PUSH:
                    uploadExercises();
                    break;
            }
        } catch (IOException exception) {
            Log.w(getClass().getName(), "Failed", exception);
        }
    }

    private void downloadExercises() throws IOException {
        DriveFile backupFile = Drive.DriveApi.getFile(googleApiClient, backupFileId);
        DriveContents backupFileContents = backupFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null)
                .await()
                .getDriveContents();

        int updates = 0;

        InputStream input = new FileInputStream(backupFileContents.getParcelFileDescriptor().getFileDescriptor());
        try {
            JsonFactory jsonFactory = new MappingJsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(input);

            JsonToken currentJsonToken;

            currentJsonToken = jsonParser.nextToken();
            if (currentJsonToken != JsonToken.START_ARRAY) {
                throw new IOException("The root must be an array");
            }

            ContentResolver contentResolver = getContentResolver();
            String updateSelection = Exercises.COLUMN_ID + " = CAST(? AS INTEGER)";
            String[] updateSelectionArgs = new String[1];

            long syncTime = System.currentTimeMillis();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                Exercise exercise = jsonParser.readValueAs(Exercise.class);

                ContentValues values = new ContentValues();
                values.put(Exercises.COLUMN_ID, exercise.id);
                values.put(Exercises.COLUMN_SCOPE, exercise.scope);
                values.put(Exercises.COLUMN_DEFINITION, exercise.definition);
                values.put(Exercises.COLUMN_NOTES, exercise.notes);
                values.put(Exercises.COLUMN_RATING, exercise.rating);
                values.put(Exercises.COLUMN_PRACTICE_TIME, exercise.practiceTime);
                values.put(Exercises.COLUMN_SYNC_TIME, syncTime);

                updateSelectionArgs[0] = Long.toString(exercise.id);
                updates += contentResolver.update(Exercises.CONTENT_SYNC_URI, values, updateSelection, updateSelectionArgs);
            }

            // TODO: delete non-synced exercises
        } finally {
            input.close();
        }

        if (updates > 0) {
            getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
    }

    private void uploadExercises() throws IOException {
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
        if (cursor == null) {
            return;
        }

        DriveFile backupFile = Drive.DriveApi.getFile(googleApiClient, backupFileId);
        DriveContents backupFileContents = backupFile.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                .await()
                .getDriveContents();

        OutputStream output = new FileOutputStream(backupFileContents.getParcelFileDescriptor().getFileDescriptor());
        try {
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(output);
            jsonGenerator.writeStartArray();

            if (cursor.moveToFirst()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Exercise exercise = new Exercise();
                do {
                    exercise.id = cursor.getLong(EXERCISE_ID);
                    exercise.scope = cursor.getString(EXERCISE_SCOPE);
                    exercise.definition = cursor.getString(EXERCISE_DEFINITION);
                    exercise.notes = cursor.getString(EXERCISE_NOTES);
                    exercise.rating = cursor.getInt(EXERCISE_RATING);
                    exercise.practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME);

                    objectMapper.writeValue(jsonGenerator, exercise);
                } while (cursor.moveToNext());
            }
            cursor.close();

            jsonGenerator.writeEndArray();
            jsonGenerator.flush();
        } finally {
            output.close();
        }

        backupFileContents.commit(googleApiClient, null)
                .await();
    }

    private static class Exercise {
        public long id;
        public String scope;
        public String definition;
        public String notes;
        public int rating;
        public long practiceTime;
    }

}
