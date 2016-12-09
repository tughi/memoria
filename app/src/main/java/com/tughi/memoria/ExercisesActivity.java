package com.tughi.memoria;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

public class ExercisesActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String FRAGMENT_TAG = "exercises";

    private static final String[] LESSONS_PROJECTION = {
            Lessons.COLUMN_TITLE,
            Lessons.COLUMN_DISABLED,
    };
    private static final int LESSON_TITLE = 0;
    private static final int LESSON_DISABLED = 1;

    private Uri lessonUri;
    private boolean lessonDisabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        lessonUri = getIntent().getData();
        if (lessonUri == null) {
            finish();
            return;
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(FRAGMENT_TAG) == null) {
            Bundle args = new Bundle();
            args.putParcelable(ExercisesFragment.ARG_EXERCISES_URI, Uri.withAppendedPath(lessonUri, "exercises"));

            fragmentManager.beginTransaction()
                    .replace(android.R.id.content, Fragment.instantiate(this, ExercisesFragment.class.getName(), args), FRAGMENT_TAG)
                    .commit();
        }

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.exercises_activity, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem disabledItem = menu.findItem(R.id.disabled);
        disabledItem.setChecked(lessonDisabled);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == R.id.disabled) {
            new AsyncTask<Object, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Object... objects) {
                    ContentValues values = new ContentValues();
                    values.put(Lessons.COLUMN_DISABLED, !item.isChecked());
                    getContentResolver().update(lessonUri, values, null, null);
                    return Boolean.TRUE;
                }
            }.execute();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle params) {
        return new CursorLoader(this, lessonUri, LESSONS_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(cursor.getString(LESSON_TITLE));
            }

            lessonDisabled = cursor.getInt(LESSON_DISABLED) != 0;
            supportInvalidateOptionsMenu();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

}
