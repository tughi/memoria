package com.tughi.memoria;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;

public class ExercisesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_EXERCISES_URI = "exercises_uri";

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_DEFINITION,
            Exercises.COLUMN_EASINESS_FACTOR,
            Exercises.COLUMN_PRACTICE_TIME,
            Exercises.COLUMN_DISABLED,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_DISABLED + ", " + Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_DEFINITION = 2;
    private static final int EXERCISE_EASINESS_FACTOR = 3;
    private static final int EXERCISE_PRACTICE_TIME = 4;
    private static final int EXERCISE_DISABLED = 5;

    private ExercisesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri exercisesUri = getArguments().getParcelable(ARG_EXERCISES_URI);
        return new CursorLoader(getActivity(), exercisesUri, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (adapter == null) {
            setListAdapter(adapter = new ExercisesAdapter(cursor));
        } else {
            adapter.setCursor(cursor);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (adapter != null) {
            adapter.setCursor(null);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        startActivity(new Intent(getActivity(), ExerciseEditActivity.class).setData(ContentUris.withAppendedId(Exercises.CONTENT_URI, id)));
    }

    private class ExercisesAdapter extends BaseAdapter {

        private Cursor cursor;

        public ExercisesAdapter(Cursor cursor) {
            this.cursor = cursor;
        }

        public void setCursor(Cursor cursor) {
            this.cursor = cursor;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return cursor != null ? cursor.getCount() : 0;
        }

        @Override
        public Cursor getItem(int position) {
            if (cursor.moveToPosition(position)) {
                return cursor;
            }
            throw new IllegalStateException("Could not move the cursor to position: " + position);
        }

        @Override
        public long getItemId(int position) {
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(EXERCISE_ID);
            }
            throw new IllegalStateException("Could not move the cursor to position: " + position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FragmentActivity activity = getActivity();

            if (convertView == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.exercises_item, parent, false);
            }

            Cursor cursor = getItem(position);
            ((TextView) convertView.findViewById(R.id.scope)).setText(cursor.getString(EXERCISE_SCOPE));
            ((TextView) convertView.findViewById(R.id.definition)).setText(Html.fromHtml(cursor.getString(EXERCISE_DEFINITION)));


            TextView statusTextView = (TextView) convertView.findViewById(R.id.status);
            int disabled = cursor.getInt(EXERCISE_DISABLED);
            if (disabled != 0) {
                statusTextView.setText(R.string.disabled);
            } else {
                int rating = cursor.getInt(EXERCISE_EASINESS_FACTOR);
                if (rating == 0) {
                    statusTextView.setText(R.string.scheduled);
                } else {
                    String status = cursor.getString(EXERCISE_EASINESS_FACTOR);

                    long practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME);
                    if (DateUtils.isToday(practiceTime)) {
                        status += " | " + DateFormat.getTimeInstance(DateFormat.SHORT).format(practiceTime);
                    } else if (practiceTime != 0) {
                        status += " | " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(practiceTime);
                    }

                    statusTextView.setText(status);
                }
            }

            return convertView;
        }

    }

}
