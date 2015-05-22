package com.tughi.memoria;

import android.net.Uri;

public class Exercises {

    public static final Uri CONTENT_URI = Uri.parse("content://" + ExercisesProvider.AUTHORITY + "/exercises");
    public static final Uri CONTENT_SYNC_URI = Uri.withAppendedPath(CONTENT_URI, "sync");

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SCOPE = "scope";
    public static final String COLUMN_DEFINITION = "definition";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_PRACTICE_TIME = "practice_time";
    public static final String COLUMN_RATING = "rating";
    public static final String COLUMN_SYNC_ID = "sync_id";
    public static final String COLUMN_SYNC_TIME = "sync_time";
    public static final String COLUMN_NEW = "new";

    protected static CharSequence getRatingText(final int rating) {
        StringBuilder text = new StringBuilder();

        int index = 0;
        for (; index < 5 && index < rating; index++) {
            text.append('★');
        }
        for (; index < 5; index++) {
            text.append('☆');
        }

        text.append(" (").append(Integer.toString(rating)).append(')');

        return text;
    }

}
