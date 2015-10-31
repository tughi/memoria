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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public abstract class PracticeFragment extends Fragment {

    /**
     * A {@link PracticeExercise} instance
     */
    public static final String ARG_EXERCISE = "exercise";

    public static final int PRACTICE_IMMEDIATELY = 0;
    public static final int PRACTICE_NORMAL = 750;
    public static final int PRACTICE_DELAYED = 2500;

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

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.practice_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.skip:
                int newRating = exercise.rating - 1;
                long newPracticeTime = System.currentTimeMillis() + 60 * 60 * 1000;

                new UpdateExerciseTask(getActivity()) {
                    @Override
                    protected void onPostExecute(Boolean result) {
                        continuePractice(100);
                    }
                }.execute(exercise, newRating, newPracticeTime);

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void continuePractice(int when) {
        ((PracticeActivity) getActivity()).continuePractice(when);
    }

    protected void submitAnswer(PracticeExercise solution) {
        if (BuildConfig.DEBUG) {
            if (solution != null) {
                if (!solution.scopeLetters.equals(exercise.scopeLetters) && !solution.definition.equals(exercise.definition)) {
                    throw new IllegalStateException("Invalid solution");
                }
            }
        }

        final long currentTime = System.currentTimeMillis();

        final int newRating;
        final long newPracticeTime;
        if (solution == null) {
            newRating = Math.round(exercise.rating / 2.3f);
            newPracticeTime = 0;
        } else if (solution == exercise) {
            newRating = exercise.rating + 1;

            long interval = (long) (Math.pow(3, newRating - 1));
            interval += interval * (currentTime % 1000 + 1);

            newPracticeTime = currentTime + interval;
        } else {
            newRating = exercise.rating + 1;
            newPracticeTime = 0;
        }

        new UpdateExerciseTask(getActivity()).execute(exercise, newRating, newPracticeTime);
    }

    protected static class UpdateExerciseTask extends AsyncTask<Object, Void, Boolean> {

        private final Context context;

        public UpdateExerciseTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            final PracticeExercise exercise = (PracticeExercise) params[0];
            final Integer newRating = (Integer) params[1];
            final Long newPracticeTime = (Long) params[2];

            ContentValues values = new ContentValues();
            values.put(Exercises.COLUMN_UPDATED_TIME, System.currentTimeMillis());
            values.put(Exercises.COLUMN_RATING, newRating);
            values.put(Exercises.COLUMN_PRACTICE_TIME, newPracticeTime);
            int result = context.getContentResolver()
                    .update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exercise.id), values, null, null);

            if (result > 0) {
                Intent intent = new Intent(context, SyncService.class);
                PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15, pendingIntent);
            }

            return Boolean.TRUE;
        }

    }

}
