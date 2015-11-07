package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public abstract class PracticeFragment extends Fragment {

    /**
     * A {@link PracticeExercise} instance
     */
    public static final String ARG_EXERCISE = "exercise";

    public static final int PRACTICE_TYPES = 5;

    private static final long TIME_SECOND = 1000;
    private static final long TIME_MINUTE = 60 * TIME_SECOND;
    private static final long TIME_HOUR = 60 * TIME_MINUTE;
    private static final long TIME_DAY = 24 * TIME_HOUR;
    private static final long TIME_WEEK = 7 * TIME_DAY;

    private static final long[] PRACTICE_TIMES = {
            0,
            TIME_SECOND,
            2 * TIME_SECOND,
            3 * TIME_SECOND,
            5 * TIME_SECOND,
            10 * TIME_SECOND,
            15 * TIME_SECOND,
            30 * TIME_SECOND,
            TIME_MINUTE,
            2 * TIME_MINUTE,
            3 * TIME_MINUTE,
            5 * TIME_MINUTE,
            10 * TIME_MINUTE,
            15 * TIME_MINUTE,
            30 * TIME_MINUTE,
            TIME_HOUR,
            2 * TIME_HOUR,
            3 * TIME_HOUR,
            6 * TIME_HOUR,
            9 * TIME_HOUR,
            12 * TIME_HOUR,
            TIME_DAY,
            2 * TIME_DAY,
            3 * TIME_DAY,
            4 * TIME_DAY,
            5 * TIME_DAY,
            6 * TIME_DAY,
            TIME_WEEK,
            2 * TIME_WEEK,
            3 * TIME_WEEK,
            5 * TIME_WEEK,
            8 * TIME_WEEK,
            13 * TIME_WEEK,
            21 * TIME_WEEK,
            34 * TIME_WEEK,
            55 * TIME_WEEK,
    };

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

    private static final Handler mainHandler = new Handler();

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        switch (exercise.rating) {
            case 0:
                view.findViewById(R.id.rating_01).setVisibility(View.INVISIBLE);
            case 1:
                view.findViewById(R.id.rating_02).setVisibility(View.INVISIBLE);
            case 2:
                view.findViewById(R.id.rating_03).setVisibility(View.INVISIBLE);
            case 3:
                view.findViewById(R.id.rating_04).setVisibility(View.INVISIBLE);
            case 4:
                view.findViewById(R.id.rating_05).setVisibility(View.INVISIBLE);
            case 5:
                view.findViewById(R.id.rating_06).setVisibility(View.INVISIBLE);
            case 6:
                view.findViewById(R.id.rating_07).setVisibility(View.INVISIBLE);
            case 7:
                view.findViewById(R.id.rating_08).setVisibility(View.INVISIBLE);
            case 8:
                view.findViewById(R.id.rating_09).setVisibility(View.INVISIBLE);
            case 9:
                view.findViewById(R.id.rating_10).setVisibility(View.INVISIBLE);
            case 10:
                view.findViewById(R.id.rating_11).setVisibility(View.INVISIBLE);
            case 11:
                view.findViewById(R.id.rating_12).setVisibility(View.INVISIBLE);
            case 12:
                view.findViewById(R.id.rating_13).setVisibility(View.INVISIBLE);
            case 13:
                view.findViewById(R.id.rating_14).setVisibility(View.INVISIBLE);
            case 14:
                view.findViewById(R.id.rating_15).setVisibility(View.INVISIBLE);
            case 15:
                view.findViewById(R.id.rating_16).setVisibility(View.INVISIBLE);
            case 16:
                view.findViewById(R.id.rating_17).setVisibility(View.INVISIBLE);
            case 17:
                view.findViewById(R.id.rating_18).setVisibility(View.INVISIBLE);
            case 18:
                view.findViewById(R.id.rating_19).setVisibility(View.INVISIBLE);
            case 19:
                view.findViewById(R.id.rating_20).setVisibility(View.INVISIBLE);
            case 20:
                view.findViewById(R.id.rating_21).setVisibility(View.INVISIBLE);
            case 21:
                view.findViewById(R.id.rating_22).setVisibility(View.INVISIBLE);
            case 22:
                view.findViewById(R.id.rating_23).setVisibility(View.INVISIBLE);
            case 23:
                view.findViewById(R.id.rating_24).setVisibility(View.INVISIBLE);
            case 24:
                view.findViewById(R.id.rating_25).setVisibility(View.INVISIBLE);
            case 25:
                view.findViewById(R.id.rating_26).setVisibility(View.INVISIBLE);
            case 26:
                view.findViewById(R.id.rating_27).setVisibility(View.INVISIBLE);
            case 27:
                view.findViewById(R.id.rating_28).setVisibility(View.INVISIBLE);
            case 28:
                view.findViewById(R.id.rating_29).setVisibility(View.INVISIBLE);
            case 29:
                view.findViewById(R.id.rating_30).setVisibility(View.INVISIBLE);
            case 30:
                view.findViewById(R.id.rating_31).setVisibility(View.INVISIBLE);
            case 31:
                view.findViewById(R.id.rating_32).setVisibility(View.INVISIBLE);
            case 32:
                view.findViewById(R.id.rating_33).setVisibility(View.INVISIBLE);
            case 33:
                view.findViewById(R.id.rating_34).setVisibility(View.INVISIBLE);
            case 34:
                view.findViewById(R.id.rating_35).setVisibility(View.INVISIBLE);
            case 35:
                view.findViewById(R.id.rating_36).setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.practice_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.skip:
                int newRating = Math.max(exercise.rating - 1, 1);
                long newPracticeTime = System.currentTimeMillis() + TIME_HOUR;

                new UpdateExerciseTask(getActivity()).execute(exercise, newRating, newPracticeTime);

                return true;
        }
        return super.onOptionsItemSelected(item);
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
            int newRatingValue = Math.max(exercise.rating * 3 / 4, 1);
            if (newRatingValue > 1 && newRatingValue % PRACTICE_TYPES == exercise.rating % PRACTICE_TYPES) {
                newRatingValue--;
            }

            newRating = newRatingValue;
            newPracticeTime = 0;
        } else if (solution == exercise) {
            newRating = Math.min(exercise.rating + 1, PRACTICE_TIMES.length - 1);
            newPracticeTime = (long) (currentTime + PRACTICE_TIMES[newRating - 1] + Math.random() * (PRACTICE_TIMES[newRating] - PRACTICE_TIMES[newRating - 1]));
        } else {
            newRating = Math.max(exercise.rating - 1, 1);
            newPracticeTime = currentTime + 5 * TIME_MINUTE;
        }

        final Context context = getActivity().getApplicationContext();
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isResumed()) {
                    new UpdateExerciseTask(context).execute(exercise, newRating, newPracticeTime);
                }
            }
        }, solution == null ? 1500 : 500);
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
