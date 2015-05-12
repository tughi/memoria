package com.tughi.memoria;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class PracticeFragment extends Fragment {

    public static final int PRACTICE_IMMEDIATELY = 0;
    public static final int PRACTICE_NORMAL = 500;
    public static final int PRACTICE_DELAYED = 1500;

    private long exerciseId;
    private String exerciseScope;
    private String exerciseDefinition;
    private int exerciseRating;

    public String getExerciseScope() {
        return exerciseScope;
    }

    public String getExerciseDefinition() {
        return exerciseDefinition;
    }

    public int getExerciseRating() {
        return exerciseRating;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        exerciseId = arguments.getLong(Exercises.COLUMN_ID);
        exerciseScope = arguments.getString(Exercises.COLUMN_SCOPE);
        exerciseDefinition = arguments.getString(Exercises.COLUMN_DEFINITION);
        exerciseRating = arguments.getInt(Exercises.COLUMN_RATING);
    }

    protected boolean submitAnswer(String answerProblem) {
        final boolean correct = exerciseScope.equals(answerProblem);

        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                final Context context = (Context) params[0];

                final int rating;
                final long practiceTime;
                if (correct) {
                    rating = exerciseRating + 1;

                    final long currentTime = System.currentTimeMillis() / 1000;
                    practiceTime = currentTime + (int) Math.pow(3, rating);
                } else {
                    rating = exerciseRating / 2;
                    practiceTime = 0;
                }

                ContentValues values = new ContentValues();
                values.put(Exercises.COLUMN_RATING, rating);
                values.put(Exercises.COLUMN_PRACTICE_TIME, practiceTime);

                return context.getContentResolver().update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exerciseId), values, null, null) > 0;
            }
        }.execute(getActivity().getApplicationContext());

        return correct;
    }

}
