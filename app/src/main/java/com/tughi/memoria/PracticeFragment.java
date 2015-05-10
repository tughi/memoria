package com.tughi.memoria;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class PracticeFragment extends Fragment {

    public static final int NEXT_PROBLEM_IMMEDIATELY = 0;
    public static final int NEXT_PROBLEM_NORMAL = 500;
    public static final int NEXT_PROBLEM_DELAYED = 1500;

    private long itemId;
    private String itemProblem;
    private String itemSolution;
    private int itemRating;

    public String getItemProblem() {
        return itemProblem;
    }

    public String getItemSolution() {
        return itemSolution;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        itemId = arguments.getLong(Items.Columns.ID);
        itemProblem = arguments.getString(Items.Columns.PROBLEM);
        itemSolution = arguments.getString(Items.Columns.SOLUTION);
        itemRating = arguments.getInt(Items.Columns.RATING);
    }

    protected boolean submitAnswer(String answerProblem) {
        final boolean correct = itemProblem.equals(answerProblem);

        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                Context context = (Context) params[0];

                ContentValues values = new ContentValues();
                values.put(Items.Columns.RATING, correct ? Math.max(itemRating + 1, 5) : Math.round(itemRating / 2.));
                values.put(Items.Columns.TESTED, System.currentTimeMillis());

                return context.getContentResolver().update(ContentUris.withAppendedId(Items.CONTENT_URI, itemId), values, null, null) > 0;
            }
        }.execute(getActivity().getApplicationContext());

        return correct;
    }

}
