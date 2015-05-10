package com.tughi.memoria;

import android.net.Uri;

public class Exercises {

    public static final Uri CONTENT_URI = Uri.parse("content://" + ExercisesProvider.AUTHORITY + "/exercises");

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCOPE = "scope";
    public static final String COLUMN_DEFINITION = "definition";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_PRACTICE_TIME = "practice_time";
    public static final String COLUMN_RATING = "rating";

}
