package com.tughi.memoria;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class ExercisesFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_SCOPE,
            Exercises.COLUMN_RATING,
            Exercises.COLUMN_PRACTICE_TIME,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_NEW + ", " + Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_SCOPE = 1;
    private static final int EXERCISE_RATING = 2;
    private static final int EXERCISE_PRACTICE_TIME = 3;

    private ExercisesAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.exercises_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                startActivity(new Intent(getActivity(), EditExerciseActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
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
        startActivity(new Intent(getActivity(), EditExerciseActivity.class).setData(ContentUris.withAppendedId(Exercises.CONTENT_URI, id)));
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
            ((TextView) convertView.findViewById(R.id.rating)).setText(Exercises.getRatingText(cursor.getInt(EXERCISE_RATING)));

            long practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME) * 1000;
            TextView practiceTimeTextView = (TextView) convertView.findViewById(R.id.practice_time);
            if (DateUtils.isToday(practiceTime)) {
                practiceTimeTextView.setText(DateUtils.formatDateTime(activity, practiceTime, DateUtils.FORMAT_SHOW_TIME));
            } else if (practiceTime == 0) {
                practiceTimeTextView.setText("");
            } else {
                practiceTimeTextView.setText(DateUtils.formatDateTime(activity, practiceTime, DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_SHOW_DATE));
            }

            return convertView;
        }

    }

}
