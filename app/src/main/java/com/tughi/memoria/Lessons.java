package com.tughi.memoria;

import android.net.Uri;

public class Lessons {

    public static final Uri CONTENT_URI = Uri.parse("content://" + ExercisesProvider.AUTHORITY + "/lessons");
    public static final Uri CONTENT_SYNC_URI = Uri.withAppendedPath(CONTENT_URI, "sync");
    public static final Uri CONTENT_USER_URI = Uri.withAppendedPath(CONTENT_URI, "user");

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_DISABLED = "disabled";

}
