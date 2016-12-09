package com.tughi.memoria;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.tughi.android.database.sqlite.DatabaseOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class ExercisesProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider";

    private static final String TABLE_LESSONS_SYNC = "lessons_sync";
    private static final String TABLE_LESSONS_USER = "lessons_user";

    private static final String VIEW_LESSONS = "lessons";

    private static final String TABLE_EXERCISES_SYNC = "exercises_sync";
    private static final String TABLE_EXERCISES_USER = "exercises_user";

    private static final String VIEW_EXERCISES = "exercises";

    private static final int URI_LESSONS = 0;
    private static final int URI_LESSONS_SYNC = 1;
    private static final int URI_LESSONS_USER = 2;
    private static final int URI_LESSON = 3;
    private static final int URI_LESSON_EXERCISES = 4;
    private static final int URI_EXERCISES = 5;
    private static final int URI_EXERCISES_SYNC = 6;
    private static final int URI_EXERCISES_USER = 7;
    private static final int URI_EXERCISE = 8;

    private UriMatcher uriMatcher;

    private static final Map<String, String> LESSONS_PROJECTION_MAP;

    static {
        Map<String, String> projection = LESSONS_PROJECTION_MAP = new HashMap<>();
        projection.put(Lessons.COLUMN_ID, Lessons.COLUMN_ID);
        projection.put(Lessons.COLUMN_TITLE, Lessons.COLUMN_TITLE);
        projection.put(Lessons.COLUMN_DISABLED, Lessons.COLUMN_DISABLED);
    }

    private static final Map<String, String> EXERCISES_PROJECTION_MAP;

    static {
        Map<String, String> projection = EXERCISES_PROJECTION_MAP = new HashMap<>();
        projection.put(Exercises.COLUMN_ID, Exercises.COLUMN_ID);
        projection.put(Exercises.COLUMN_SCOPE, Exercises.COLUMN_SCOPE);
        projection.put(Exercises.COLUMN_SCOPE_LETTERS, Exercises.COLUMN_SCOPE_LETTERS);
        projection.put(Exercises.COLUMN_DEFINITION, Exercises.COLUMN_DEFINITION);
        projection.put(Exercises.COLUMN_NOTES, Exercises.COLUMN_NOTES);
        projection.put(Exercises.COLUMN_PRACTICE_TIME, Exercises.COLUMN_PRACTICE_TIME);
        projection.put(Exercises.COLUMN_EASINESS_FACTOR, Exercises.COLUMN_EASINESS_FACTOR);
        projection.put(Exercises.COLUMN_PRACTICE_COUNT, Exercises.COLUMN_PRACTICE_COUNT);
        projection.put(Exercises.COLUMN_PRACTICE_INTERVAL, Exercises.COLUMN_PRACTICE_INTERVAL);
        projection.put(Exercises.COLUMN_DISABLED, Exercises.COLUMN_DISABLED);
        projection.put(Exercises.COLUMN_NEW, "(" + Exercises.COLUMN_PRACTICE_COUNT + " = 0) AS " + Exercises.COLUMN_NEW);
    }

    private DatabaseOpenHelper helper;

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "lessons", URI_LESSONS);
        uriMatcher.addURI(AUTHORITY, "lessons/sync", URI_LESSONS_SYNC);
        uriMatcher.addURI(AUTHORITY, "lessons/user", URI_LESSONS_USER);
        uriMatcher.addURI(AUTHORITY, "lessons/#", URI_LESSON);
        uriMatcher.addURI(AUTHORITY, "lessons/#/exercises", URI_LESSON_EXERCISES);
        uriMatcher.addURI(AUTHORITY, "exercises", URI_EXERCISES);
        uriMatcher.addURI(AUTHORITY, "exercises/sync", URI_EXERCISES_SYNC);
        uriMatcher.addURI(AUTHORITY, "exercises/user", URI_EXERCISES_USER);
        uriMatcher.addURI(AUTHORITY, "exercises/#", URI_EXERCISE);

        helper = new DatabaseOpenHelper(getContext(), "exercises.db", 2);

        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        switch (uriMatcher.match(uri)) {
            case URI_LESSONS:
                return queryLessons(projection, selection, selectionArgs, sortOrder);
            case URI_LESSON:
                selection = AND(Lessons.COLUMN_ID + " = " + uri.getLastPathSegment(), selection);
                return queryLessons(projection, selection, selectionArgs, sortOrder);
            case URI_LESSON_EXERCISES:
                selection = AND(Exercises.COLUMN_LESSON_ID + " = " + uri.getPathSegments().get(1), selection);
                return queryExercises(projection, selection, selectionArgs, sortOrder);
            case URI_EXERCISES:
                return queryExercises(projection, selection, selectionArgs, sortOrder);
            case URI_EXERCISE:
                selection = AND(Exercises.COLUMN_ID + " = " + uri.getLastPathSegment(), selection);
                return queryExercises(projection, selection, selectionArgs, sortOrder);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private Cursor queryLessons(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();

        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(VIEW_LESSONS);
        query.setProjectionMap(LESSONS_PROJECTION_MAP);

        Cursor cursor = query.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), Exercises.CONTENT_URI);
        return cursor;
    }

    private Cursor queryExercises(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = helper.getReadableDatabase();

        SQLiteQueryBuilder query = new SQLiteQueryBuilder();
        query.setTables(VIEW_EXERCISES);
        query.setProjectionMap(EXERCISES_PROJECTION_MAP);

        Cursor cursor = query.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), Exercises.CONTENT_URI);
        return cursor;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        switch (uriMatcher.match(uri)) {
            case URI_LESSONS_SYNC:
                return insertOrReplaceLessonSync(values);
            case URI_LESSONS_USER:
                return insertOrIgnoreLessonUser(values);
            case URI_EXERCISES_SYNC:
                return insertOrReplaceExerciseSync(values);
            case URI_EXERCISES_USER:
                return insertOrIgnoreExerciseUser(values);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private Uri insertOrReplaceLessonSync(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertWithOnConflict(TABLE_LESSONS_SYNC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (id > 0) {
            return ContentUris.withAppendedId(Lessons.CONTENT_URI, id);
        }
        return null;
    }

    private Uri insertOrIgnoreLessonUser(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertWithOnConflict(TABLE_LESSONS_USER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id > 0) {
            return ContentUris.withAppendedId(Lessons.CONTENT_URI, id);
        }
        return null;
    }

    private Uri insertOrReplaceExerciseSync(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertWithOnConflict(TABLE_EXERCISES_SYNC, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        if (id > 0) {
            return ContentUris.withAppendedId(Exercises.CONTENT_URI, id);
        }
        return null;
    }

    private Uri insertOrIgnoreExerciseUser(ContentValues values) {
        SQLiteDatabase db = helper.getWritableDatabase();
        long id = db.insertWithOnConflict(TABLE_EXERCISES_USER, null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (id > 0) {
            return ContentUris.withAppendedId(Exercises.CONTENT_URI, id);
        }
        return null;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_LESSON:
                return updateLessons(values, AND(Lessons.COLUMN_ID + " = " + uri.getLastPathSegment(), selection), selectionArgs);
            case URI_EXERCISE:
                return updateExercises(values, AND(Exercises.COLUMN_ID + " = " + uri.getLastPathSegment(), selection), selectionArgs);
        }
        throw new UnsupportedOperationException("Not supported: " + uri);
    }

    private int updateLessons(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int count = db.update(TABLE_LESSONS_USER, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
        return count;
    }

    private int updateExercises(ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        int count = db.update(TABLE_EXERCISES_USER, values, selection, selectionArgs);
        if (count > 0) {
            getContext().getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
        }
        return count;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        switch (uriMatcher.match(uri)) {
            case URI_EXERCISES_SYNC:
                return deleteExercises(selection, selectionArgs);
        }
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private int deleteExercises(String selection, String[] selectionArgs) {
        SQLiteDatabase db = helper.getWritableDatabase();
        return db.delete(VIEW_EXERCISES, selection, selectionArgs);
    }

    @Override
    public String getType(@NonNull Uri uri) {
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
