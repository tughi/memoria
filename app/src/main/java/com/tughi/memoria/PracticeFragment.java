package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class PracticeFragment extends Fragment {

    /**
     * A {@link PracticeExercise} instance
     */
    public static final String ARG_EXERCISE = "exercise";

    public static final int PRACTICE_IMMEDIATELY = 0;
    public static final int PRACTICE_NORMAL = 500;
    public static final int PRACTICE_DELAYED = 1500;

    protected static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_RATING,
    };
    protected static final int EXERCISE_ID = 0;
    protected static final int EXERCISE_SCOPE = 1;
    protected static final int EXERCISE_SCOPE_LETTERS = 2;
    protected static final int EXERCISE_DEFINITION = 3;
    protected static final int EXERCISE_RATING = 4;

    private PracticeExercise exercise;

    public PracticeExercise getExercise() {
        return exercise;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        exercise = arguments.getParcelable(ARG_EXERCISE);
    }

    protected void submitAnswer(PracticeExercise solution) {
        if (BuildConfig.DEBUG) {
            if (solution != null) {
                if (!solution.scopeLetters.equals(exercise.scopeLetters) && !solution.definition.equals(exercise.definition)) {
                    throw new IllegalStateException("Invalid solution");
                }
            }
        }

        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                final Context context = (Context) params[0];
                final PracticeExercise exercise = (PracticeExercise) params[1];
                final PracticeExercise solution = (PracticeExercise) params[2];

                final long currentTime = System.currentTimeMillis();

                final int rating;
                final long practiceTime;
                if (solution == null) {
                    rating = Math.round(exercise.rating / 2.3f);
                    practiceTime = 0;
                } else if (solution == exercise) {
                    rating = exercise.rating + 1;

                    long interval = (long) (Math.pow(3, rating - 1));
                    interval += interval * (currentTime % 1000 + 1);

                    practiceTime = currentTime + interval;
                } else {
                    rating = exercise.rating + 1;
                    practiceTime = 0;
                }

                ContentValues values = new ContentValues();
                values.put(Exercises.COLUMN_UPDATED_TIME, currentTime);
                values.put(Exercises.COLUMN_RATING, rating);
                values.put(Exercises.COLUMN_PRACTICE_TIME, practiceTime);
                int result = context.getContentResolver()
                        .update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exercise.id), values, null, null);

                if (result > 0) {
                    Intent intent = new Intent(context, SyncService.class);
                    PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, currentTime + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15, pendingIntent);
                }

                return Boolean.TRUE;
            }
        }.execute(getActivity().getApplicationContext(), exercise, solution);
    }

}
