package com.tughi.memoria;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.tughi.android.database.sqlite.DatabaseOpenHelper;

public class ExercisesProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final String TABLE_ITEMS = "exercises";

    private static final int URI_EXERCISES = 0;
    private static final int URI_EXERCISE = 1;

    private UriMatcher uriMatcher;

    private DatabaseOpenHelper helper;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "exercises", URI_EXERCISES);
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
        Cursor cursor = db.query(TABLE_ITEMS, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), Exercises.CONTENT_URI);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISES:
                return insertExercise(values);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private Uri insertExercise(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertOrThrow(TABLE_ITEMS, null, values);
        Uri uri = ContentUris.withAppendedId(Exercises.CONTENT_URI, id);
        getContext().getContentResolver().notifyChange(uri, null);
        return uri;
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
        int count = db.update(TABLE_ITEMS, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
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