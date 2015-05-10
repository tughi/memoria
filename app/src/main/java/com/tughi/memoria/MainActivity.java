package com.tughi.memoria;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, Handler.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_PRACTICE_TIME + " < CAST(STRFTIME('%s', 'now') AS INTEGER)";
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;
    private static final int EXERCISE_RATING = 3;

    private Cursor exercisesCursor;

    private DrawerLayout drawerLayout;
    private View drawerView;

    private Handler practiceHandler;
    private Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        practiceHandler = new Handler(this);

        setContentView(R.layout.main_activity);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = findViewById(R.id.drawer);
        drawerView.findViewById(R.id.practice).setOnClickListener(this);
        drawerView.findViewById(R.id.exercises).setOnClickListener(this);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.practice:
                replacePracticeFragment();
                break;
            case R.id.exercises:
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.content, new ExercisesFragment())
                        .commit();
                break;
        }

        drawerLayout.closeDrawer(drawerView);
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
            if (exerciseRating < 4) {
                args.putBoolean(AnswerPickerFragment.ARG_INVERT, exerciseRating == 3);
                practiceFragment = new AnswerPickerFragment();
            } else if (exerciseRating == 4) {
                practiceFragment = new AnswerInputFragment();
            } else {
                switch (random.nextInt(3)) {
                    case 0:
                        args.putBoolean(AnswerPickerFragment.ARG_INVERT, true);
                    case 1:
                        practiceFragment = new AnswerPickerFragment();
                        break;
                    case 2:
                    default:
                        practiceFragment = new AnswerInputFragment();
                        break;
                }
            }

            practiceFragment.setArguments(args);

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, practiceFragment)
                    .commit();
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
