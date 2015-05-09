package com.tughi.memoria;

import android.net.Uri;

public class Items {

    public static final Uri CONTENT_URI = Uri.parse("content://" + KnowledgeProvider.AUTHORITY + "/items");

    public interface Columns {
        String ID = "_id";
        String PROBLEM = "problem";
        String SOLUTION = "solution";
        String NOTES = "notes";
        String RATING = "rating";
        String TESTED = "tested";
    }

}
