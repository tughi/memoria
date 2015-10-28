package com.tughi.memoria;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides helper methods to importFromJson to JSON and import from JSON the exercises.
 */
public class ExercisesJsonHelper {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_NOTES,
            Exercises.COLUMN_PRACTICE_TIME,
            Exercises.COLUMN_RATING,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_ID;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_SCOPE_LETTERS = 2;
    private static final int EXERCISE_DEFINITION = 3;
    private static final int EXERCISE_NOTES = 4;
    private static final int EXERCISE_PRACTICE_TIME = 5;
    private static final int EXERCISE_RATING = 6;

    public static void importFromJson(Context context, InputStream input) throws IOException {
        int updates = 0;

        try {
            JsonFactory jsonFactory = new MappingJsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(input);

            JsonToken currentJsonToken;

            currentJsonToken = jsonParser.nextToken();
            if (currentJsonToken != JsonToken.START_ARRAY) {
                throw new IOException("The root must be an array");
            }

            ContentResolver contentResolver = context.getContentResolver();
            String updateSelection = Exercises.COLUMN_ID + " = CAST(? AS INTEGER)";
            String[] updateSelectionArgs = new String[1];

            long syncTime = System.currentTimeMillis();

            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                Exercise exercise = jsonParser.readValueAs(Exercise.class);

                ContentValues values = new ContentValues();
                values.put(Exercises.COLUMN_ID, exercise.id);
                values.put(Exercises.COLUMN_SCOPE, exercise.scope);
                values.put(Exercises.COLUMN_SCOPE_LETTERS, exercise.scopeLetters);
                values.put(Exercises.COLUMN_DEFINITION, exercise.definition);
                values.put(Exercises.COLUMN_NOTES, exercise.notes);
                values.put(Exercises.COLUMN_RATING, exercise.rating);
                values.put(Exercises.COLUMN_PRACTICE_TIME, exercise.practiceTime);
                values.put(Exercises.COLUMN_SYNC_TIME, syncTime);

                updateSelectionArgs[0] = Long.toString(exercise.id);
                updates += contentResolver.update(Exercises.CONTENT_SYNC_URI, values, updateSelection, updateSelectionArgs);
            }
        } finally {
            input.close();
        }

        // TODO: delete non-synced exercises

        if (updates > 0) {
            context.getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
    }

    public static void asyncImportToJson(final Context context, final InputStream input) {
        new AsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                try {
                    importFromJson(context, input);
                    return Boolean.TRUE;
                } catch (IOException exception) {
                    Log.w(getClass().getName(), "Import failed", exception);
                }

                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                String text;
                if (result == Boolean.TRUE) {
                    text = "Import finished with success";
                } else {
                    text = "Import failed";
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    public static void exportToJson(Context context, OutputStream output) throws IOException {
        try {
            Cursor cursor = context.getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
            if (cursor == null) {
                return;
            }

            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(output);
            jsonGenerator.setPrettyPrinter(new DefaultPrettyPrinter());
            jsonGenerator.writeStartArray();

            if (cursor.moveToFirst()) {
                ObjectMapper objectMapper = new ObjectMapper();
                Exercise exercise = new Exercise();
                do {
                    exercise.id = cursor.getLong(EXERCISE_ID);
                    exercise.scope = cursor.getString(EXERCISE_SCOPE);
                    exercise.scopeLetters = cursor.getString(EXERCISE_SCOPE_LETTERS);
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
    }

    public static void asyncExportToJson(final Context context, final OutputStream output) {
        new AsyncTask<Object, Object, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                try {
                    exportToJson(context, output);
                    return Boolean.TRUE;
                } catch (IOException exception) {
                    Log.w(getClass().getName(), "Export failed", exception);
                }

                return Boolean.FALSE;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                String text;
                if (result == Boolean.TRUE) {
                    text = "Export finished with success";
                } else {
                    text = "Export failed";
                }
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }.execute();
    }

    private static class Exercise {
        public long id;
        public String scope;
        public String scopeLetters;
        public String definition;
        public String notes;
        public int rating;
        public long practiceTime;
    }

}
