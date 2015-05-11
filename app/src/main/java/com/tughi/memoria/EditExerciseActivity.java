package com.tughi.memoria;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class EditExerciseActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private Uri exerciseUri;

    private EditText scopeEditText;
    private EditText definitionEditText;
    private EditText notesEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        exerciseUri = getIntent().getData();

        setContentView(R.layout.edit_exercise_activity);
        scopeEditText = (EditText) findViewById(R.id.scope);
        definitionEditText = (EditText) findViewById(R.id.definition);
        notesEditText = (EditText) findViewById(R.id.notes);

        if (exerciseUri != null) {
            getSupportLoaderManager().initLoader(0, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.edit_exercise_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.done:
                new SaveExerciseTask().execute();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, exerciseUri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            scopeEditText.setText(cursor.getString(cursor.getColumnIndex(Exercises.COLUMN_SCOPE)));
            definitionEditText.setText(cursor.getString(cursor.getColumnIndex(Exercises.COLUMN_DEFINITION)));
            notesEditText.setText(cursor.getString(cursor.getColumnIndex(Exercises.COLUMN_NOTES)));

            loader.abandon();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // nothing to do here
    }

    private class SaveExerciseTask extends AsyncTask<Object, Void, Boolean> {

        private Context context;
        private ContentValues values;

        private String error;

        @Override
        protected void onPreExecute() {
            context = getApplicationContext();

            values = new ContentValues();
            putValue(Exercises.COLUMN_SCOPE, scopeEditText);
            putValue(Exercises.COLUMN_DEFINITION, definitionEditText);
            putValue(Exercises.COLUMN_NOTES, notesEditText);
        }

        private void putValue(String key, EditText editText) {
            String value = editText.getText().toString().trim();
            if (value.isEmpty()) {
                values.putNull(key);
            } else {
                values.put(key, value);
            }
        }

        @Override
        protected Boolean doInBackground(Object... params) {
            try {
                ContentResolver contentResolver = context.getContentResolver();
                Uri uri = exerciseUri;
                if (uri == null) {
                    uri = contentResolver.insert(Exercises.CONTENT_URI, values);
                } else {
                    if (contentResolver.update(uri, values, null, null) <= 0) {
                        uri = null;
                    }
                }
                return uri != null;
            } catch (Exception exception) {
                error = exception.getMessage();
                return Boolean.FALSE;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!isFinishing()) {
                if (result == Boolean.TRUE) {
                    finish();
                } else if (error != null) {
                    Toast.makeText(context, error, Toast.LENGTH_LONG).show();
                }
            }
        }
    }

}
