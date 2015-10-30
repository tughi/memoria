package com.tughi.memoria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class PracticeActivity extends AppCompatActivity implements Handler.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = "STRFTIME('%s', 'now') * 1000 - " + Exercises.COLUMN_PRACTICE_TIME + " > 0";
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + " DESC, " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_SCOPE_LETTERS = 2;
    private static final int EXERCISE_DEFINITION = 3;
    private static final int EXERCISE_RATING = 4;

    private Cursor exercisesCursor;

    private Handler practiceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.practice_activity);

        practiceHandler = new Handler(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.practice_activity, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exercises:
                startActivity(new Intent(this, ExercisesActivity.class));
                return true;
            case R.id.preferences:
                startActivity(new Intent(this, PreferencesActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message message) {
        replacePracticeFragment();

        return true;
    }

    private void replacePracticeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        if (exercisesCursor != null && exercisesCursor.moveToFirst()) {
            PracticeExercise practiceExercise = new PracticeExercise(
                    exercisesCursor.getLong(EXERCISE_ID),
                    exercisesCursor.getString(EXERCISE_SCOPE),
                    exercisesCursor.getString(EXERCISE_SCOPE_LETTERS),
                    exercisesCursor.getString(EXERCISE_DEFINITION),
                    exercisesCursor.getInt(EXERCISE_RATING)
            );

            Bundle args = new Bundle();
            args.putParcelable(PracticeFragment.ARG_EXERCISE, practiceExercise);

            PracticeFragment practiceFragment;
            final int exerciseType = practiceExercise.rating % 5;
            switch (exerciseType) {
                case 4:
                    practiceFragment = new AnswerInputFragment();
                    break;
                case 3:
                    args.putBoolean(AnswerPickerFragment.ARG_INVERT, true);
                case 2:
                case 1:
                default:
                    practiceFragment = new AnswerPickerFragment();
                    break;
            }
            practiceFragment.setArguments(args);

            fragmentManager.beginTransaction()
                    .replace(R.id.content, practiceFragment)
                    .commitAllowingStateLoss();
        } else {
            // TODO: handle the case where no exercises are left to practice on
            Fragment fragment = fragmentManager.findFragmentById(R.id.content);
            if (fragment != null) {
                fragmentManager.beginTransaction()
                        .remove(fragment)
                        .commitAllowingStateLoss();
            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        exercisesCursor = cursor;

        if (getSupportFragmentManager().findFragmentById(R.id.content) == null) {
            continuePractice(PracticeFragment.PRACTICE_IMMEDIATELY);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        exercisesCursor = null;
    }

    public void continuePractice(int when) {
        practiceHandler.sendEmptyMessageDelayed(0, when);
    }

}
