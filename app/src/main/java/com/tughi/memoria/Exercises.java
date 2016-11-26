package com.tughi.memoria;

import android.net.Uri;

public class Exercises {

    public static final Uri CONTENT_URI = Uri.parse("content://" + ExercisesProvider.AUTHORITY + "/exercises");
    public static final Uri CONTENT_SYNC_URI = Uri.withAppendedPath(CONTENT_URI, "sync");
    public static final Uri CONTENT_USER_URI = Uri.withAppendedPath(CONTENT_URI, "user");

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LESSON_ID = "lesson_id";
    public static final String COLUMN_SCOPE = "scope";
    public static final String COLUMN_SCOPE_LETTERS = "scope_letters";
    public static final String COLUMN_DEFINITION = "definition";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_EASINESS_FACTOR = "easiness_factor";
    public static final String COLUMN_PRACTICE_COUNT = "practice_count";
    public static final String COLUMN_PRACTICE_INTERVAL = "practice_interval";
    public static final String COLUMN_PRACTICE_TIME = "practice_time";
    public static final String COLUMN_DISABLED = "disabled";
    public static final String COLUMN_NEW = "new";

}
