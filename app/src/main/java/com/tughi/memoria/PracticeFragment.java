package com.tughi.memoria;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public abstract class PracticeFragment extends Fragment {

    /**
     * A {@link PracticeExercise} instance
     */
    public static final String ARG_EXERCISE = "exercise";

    private static final double EASINESS_FACTOR_MIN = 1.3;

    protected static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_EASINESS_FACTOR,
            Exercises.COLUMN_PRACTICE_COUNT,
            Exercises.COLUMN_PRACTICE_INTERVAL,
    };
    protected static final int EXERCISE_ID = 0;
    protected static final int EXERCISE_SCOPE = 1;
    protected static final int EXERCISE_SCOPE_LETTERS = 2;
    protected static final int EXERCISE_DEFINITION = 3;
    protected static final int EXERCISE_EASINESS_FACTOR = 4;
    protected static final int EXERCISE_PRACTICE_COUNT = 5;
    protected static final int EXERCISE_PRACTICE_INTERVAL = 6;

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

        updateRatingViews(view, exercise.easinessFactor);
    }

    private void updateRatingViews(View view, double easinessFactor) {
        view.findViewById(R.id.rating_01).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_02).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_03).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_04).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_05).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_06).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_07).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_08).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_09).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_10).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_11).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_12).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_13).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_14).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_15).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_16).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_17).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_18).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_19).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_20).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_21).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_22).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_23).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_24).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_25).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_26).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_27).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_28).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_29).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_30).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_31).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_32).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_33).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_34).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_35).setVisibility(View.VISIBLE);
        view.findViewById(R.id.rating_36).setVisibility(View.VISIBLE);

        switch ((int) ((Math.min(easinessFactor, 4) - EASINESS_FACTOR_MIN) * 36 / (4 - EASINESS_FACTOR_MIN))) {
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
                /* TODO: reschedule skipped exercise
                int newRating = Math.max(exercise.rating - 1, 1);
                long newPracticeTime = System.currentTimeMillis() + TIME_HOUR;

                new UpdateExerciseTask(getActivity()).execute(exercise, newRating, newPracticeTime);
                */

                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void submitAnswer(PracticeExercise solution, int solutionQuality) {
        if (BuildConfig.DEBUG) {
            if (solution != null) {
                if (!solution.scopeLetters.equals(exercise.scopeLetters) && !solution.definition.equals(exercise.definition)) {
                    throw new IllegalStateException("Invalid solution");
                }
            }
        }

        final double newEasinessFactor;
        switch (solutionQuality) {
            case 0:
                newEasinessFactor = Math.max(exercise.easinessFactor - 0.45, EASINESS_FACTOR_MIN);
                break;
            case 1:
                newEasinessFactor = Math.max(exercise.easinessFactor - 0.34, EASINESS_FACTOR_MIN);
                break;
            case 2:
                newEasinessFactor = Math.max(exercise.easinessFactor - 0.23, EASINESS_FACTOR_MIN);
                break;
            case 3:
                newEasinessFactor = Math.max(exercise.easinessFactor - 0.12, EASINESS_FACTOR_MIN);
                break;
            case 4:
                newEasinessFactor = exercise.easinessFactor;
                break;
            default:
                newEasinessFactor = exercise.easinessFactor + 0.11;
                break;
        }

        updateRatingViews(getView(), newEasinessFactor);

        final int newPracticeCount = solutionQuality == 0 ? 1 : exercise.practiceCount + 1;
        final long newPracticeInterval = newEasinessFactor < 2.7 ? 3000 : newPracticeCount > 2 ? Math.round(exercise.practiceInterval * newEasinessFactor) : newPracticeCount == 2 ? 30000 : 5000;
        final long newPracticeTime = System.currentTimeMillis() + newPracticeInterval;

        if (BuildConfig.DEBUG) {
            Log.d(getClass().getName(), exercise.scope + ": " + newEasinessFactor + " - " + newPracticeCount + " - " + DateUtils.getRelativeTimeSpanString(getContext(), newPracticeTime));
        }

        PracticeActivity activity = (PracticeActivity) getActivity();
        if (solution != null) {
            activity.speak(solution.scope);
        }

        final Context context = activity.getApplicationContext();
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isResumed()) {
                    new UpdateExerciseTask(context).execute(exercise, newEasinessFactor, newPracticeCount, newPracticeInterval, newPracticeTime);
                }
            }
        }, solutionQuality == 5 ? 500 : 1500);
    }

    protected static class UpdateExerciseTask extends AsyncTask<Object, Void, Boolean> {

        private final Context context;

        public UpdateExerciseTask(Context context) {
            this.context = context.getApplicationContext();
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            final PracticeExercise exercise = (PracticeExercise) params[0];
            final Double newEasinessFactor = (Double) params[1];
            final Integer newPracticeCount = (Integer) params[2];
            final Long newPracticeInterval = (Long) params[3];
            final Long newPracticeTime = (Long) params[4];

            ContentResolver contentResolver = context.getContentResolver();

            ContentValues values = new ContentValues();
            values.put(Exercises.COLUMN_EASINESS_FACTOR, newEasinessFactor);
            values.put(Exercises.COLUMN_PRACTICE_COUNT, newPracticeCount);
            values.put(Exercises.COLUMN_PRACTICE_INTERVAL, newPracticeInterval);
            values.put(Exercises.COLUMN_PRACTICE_TIME, newPracticeTime);

            contentResolver.update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exercise.id), values, null, null);

            return Boolean.TRUE;
        }

    }

}
