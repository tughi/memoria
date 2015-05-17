package com.tughi.memoria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;
    private static final int EXERCISE_RATING = 3;

    private Cursor exercisesCursor;

    private Handler practiceHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        practiceHandler = new Handler(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.exercises:
                startActivity(new Intent(this, ExercisesActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.practice_activity, menu);

        return true;
    }

    @Override
    public boolean handleMessage(Message message) {
        replacePracticeFragment();

        return true;
    }

    private void replacePracticeFragment() {
        if (exercisesCursor != null && exercisesCursor.moveToFirst()) {
            final int exerciseRating = exercisesCursor.getInt(EXERCISE_RATING);

            Bundle args = new Bundle();
            args.putLong(Exercises.COLUMN_ID, exercisesCursor.getLong(EXERCISE_ID));
            args.putString(Exercises.COLUMN_SCOPE, exercisesCursor.getString(EXERCISE_SCOPE));
            args.putString(Exercises.COLUMN_DEFINITION, exercisesCursor.getString(EXERCISE_DEFINITION));
            args.putInt(Exercises.COLUMN_RATING, exerciseRating);

            PracticeFragment practiceFragment;
            if (exercisesCursor.getCount() > 10) {
                switch (exerciseRating % 5) {
                    case 4:
                        practiceFragment = new AnswerInputFragment();
                        break;
                    case 3:
                        args.putBoolean(AnswerPickerFragment.ARG_INVERT, exerciseRating == 3);
                    case 2:
                    case 1:
                    default:
                        practiceFragment = new AnswerPickerFragment();
                        break;
                }
            } else {
                practiceFragment = new AnswerInputFragment();
            }

            practiceFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content, practiceFragment)
                    .commit();
        } else {
            // TODO: handle the case where no exercises are left to practice on
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        exercisesCursor = cursor;

        if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
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
