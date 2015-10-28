package com.tughi.memoria;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.tughi.android.database.sqlite.DatabaseOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class ExercisesProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final String TABLE_EXERCISES = "exercises";

    private static final int URI_EXERCISES = 0;
    private static final int URI_EXERCISES_SYNC = 1;
    private static final int URI_EXERCISE = 2;

    private UriMatcher uriMatcher;

    private static final Map<String, String> EXERCISES_PROJECTION_MAP;

    static {
        Map<String, String> projection = EXERCISES_PROJECTION_MAP = new HashMap<>();
        projection.put(Exercises.COLUMN_ID, Exercises.COLUMN_ID);
        projection.put(Exercises.COLUMN_CREATED_TIME, Exercises.COLUMN_CREATED_TIME);
        projection.put(Exercises.COLUMN_UPDATED_TIME, Exercises.COLUMN_UPDATED_TIME);
        projection.put(Exercises.COLUMN_SCOPE, Exercises.COLUMN_SCOPE);
        projection.put(Exercises.COLUMN_SCOPE_LETTERS, Exercises.COLUMN_SCOPE_LETTERS);
        projection.put(Exercises.COLUMN_DEFINITION, Exercises.COLUMN_DEFINITION);
        projection.put(Exercises.COLUMN_NOTES, Exercises.COLUMN_NOTES);
        projection.put(Exercises.COLUMN_PRACTICE_TIME, Exercises.COLUMN_PRACTICE_TIME);
        projection.put(Exercises.COLUMN_RATING, Exercises.COLUMN_RATING);
        projection.put(Exercises.COLUMN_SYNC_TIME, Exercises.COLUMN_SYNC_TIME);
        projection.put(Exercises.COLUMN_NEW, "(" + Exercises.COLUMN_RATING + " = 0) AS " + Exercises.COLUMN_NEW);
    }

    private DatabaseOpenHelper helper;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "exercises", URI_EXERCISES);
        uriMatcher.addURI(AUTHORITY, "exercises/sync", URI_EXERCISES_SYNC);
        uriMatcher.addURI(AUTHORITY, "exercises/#", URI_EXERCISE);

        helper = new DatabaseOpenHelper(getContext(), "exercises.db", 1);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISE:
                selection = AND(Exercises.COLUMN_ID + " = " + uri.getLastPathSegment(), selection);
            case URI_EXERCISES:
                return queryExercises(projection, selection, selectionArgs, sortOrder);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private Cursor queryExercises(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();

        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(TABLE_EXERCISES);
        query.setProjectionMap(EXERCISES_PROJECTION_MAP);

        Cursor cursor = query.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), Exercises.CONTENT_URI);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISES:
                return insertExercise(values);
            case URI_EXERCISES_SYNC:
                replaceExercises(values);
                return null;
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private Uri insertExercise(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertOrThrow(TABLE_EXERCISES, null, values);
        Uri uri = ContentUris.withAppendedId(Exercises.CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
    }

    private void replaceExercises(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        db.replace(TABLE_EXERCISES, null, values);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISE:
                return updateExercises(values, AND(Exercises.COLUMN_ID + " = " + uri.getLastPathSegment(), selection), selectionArgs);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private int updateExercises(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int count = db.update(TABLE_EXERCISES, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISES_SYNC:
                return deleteExercises(selection, selectionArgs);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private int deleteExercises(String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(TABLE_EXERCISES, selection, selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private String AND(String... conditions) {
        StringBuilder buffer = new StringBuilder();
        for (String condition : conditions) {
            if (condition != null) {
                buffer.append(" AND (");
                buffer.append(condition);
                buffer.append(")");
            }
        }
        return buffer.length() > 0 ? buffer.substring(" AND ".length()) : null;
    }

}
