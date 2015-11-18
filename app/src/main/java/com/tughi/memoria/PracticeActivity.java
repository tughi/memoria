package com.tughi.memoria;

import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Locale;

public class PracticeActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_SCOPE_LETTERS,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_DISABLED + " = 0 AND strftime('%s', 'now') * 1000 - " + Exercises.COLUMN_PRACTICE_TIME + " > 0";
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_SCOPE_LETTERS = 2;
    private static final int EXERCISE_DEFINITION = 3;
    private static final int EXERCISE_RATING = 4;

    private AudioManager audioManager;
    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // force sync
        startService(new Intent(this, SyncService.class));

        setContentView(R.layout.practice_activity);

        getSupportLoaderManager().initLoader(0, null, this);

        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (textToSpeech.isLanguageAvailable(Locale.JAPAN) >= TextToSpeech.LANG_AVAILABLE) {
                    textToSpeech.setLanguage(Locale.JAPAN);
                }
            }
        });

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onDestroy() {
        textToSpeech.shutdown();

        super.onDestroy();
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
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.content);

        PracticeExercise fragmentExercise = null;
        if (fragment != null) {
            Bundle fragmentArguments = fragment.getArguments();
            fragmentExercise = fragmentArguments.getParcelable(PracticeFragment.ARG_EXERCISE);
        }

        PracticeExercise exercise = null;
        if (cursor.moveToFirst()) {
            exercise = new PracticeExercise(
                    cursor.getLong(EXERCISE_ID),
                    cursor.getString(EXERCISE_SCOPE),
                    cursor.getString(EXERCISE_SCOPE_LETTERS),
                    cursor.getString(EXERCISE_DEFINITION),
                    cursor.getInt(EXERCISE_RATING)
            );
        }

        if (exercise != null) {
            if (!exercise.equals(fragmentExercise)) {
                replacePracticeFragment(fragmentManager, exercise);
            }
        } else if (!(fragment instanceof PracticeBreakFragment)) {
            replaceBreakFragment(fragmentManager);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    private void replacePracticeFragment(FragmentManager fragmentManager, PracticeExercise practiceExercise) {
        Bundle args = new Bundle();
        args.putParcelable(PracticeFragment.ARG_EXERCISE, practiceExercise);

        PracticeFragment practiceFragment;
        switch (practiceExercise.rating % PracticeFragment.PRACTICE_TYPES) {
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
    }

    private void replaceBreakFragment(FragmentManager fragmentManager) {
        PracticeBreakFragment fragment = new PracticeBreakFragment();
        fragment.setArguments(new Bundle());

        fragmentManager
                .beginTransaction()
                .replace(R.id.content, fragment)
                .commitAllowingStateLoss();
    }

    @SuppressWarnings("deprecation")
    protected void speak(String scope) {
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0) {
            textToSpeech.speak(scope, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
