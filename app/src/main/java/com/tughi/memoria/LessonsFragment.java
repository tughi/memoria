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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class LessonsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] LESSONS_PROJECTION = {
            Lessons.COLUMN_ID,
            Lessons.COLUMN_TITLE,
            Lessons.COLUMN_DISABLED,
    };
    private static final String LESSONS_SORT_ORDER = Lessons.COLUMN_TITLE;
    private static final int LESSON_ID = 0;
    private static final int LESSON_TITLE = 1;
    private static final int LESSON_DISABLED = 2;

    private LessonsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Lessons.CONTENT_URI, LESSONS_PROJECTION, null, null, LESSONS_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (adapter == null) {
            setListAdapter(adapter = new LessonsAdapter(cursor));
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
        startActivity(new Intent(getActivity(), ExercisesActivity.class).setData(ContentUris.withAppendedId(Lessons.CONTENT_URI, id)));
    }

    private class LessonsAdapter extends BaseAdapter {

        private Cursor cursor;

        public LessonsAdapter(Cursor cursor) {
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
                return cursor.getLong(LESSON_ID);
            }
            throw new IllegalStateException("Could not move the cursor to position: " + position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FragmentActivity activity = getActivity();

            if (convertView == null) {
                convertView = LayoutInflater.from(activity).inflate(R.layout.lessons_item, parent, false);
            }

            Cursor cursor = getItem(position);
            TextView titleTextView = (TextView) convertView.findViewById(R.id.title);
            titleTextView.setText(cursor.getString(LESSON_TITLE));
            titleTextView.setEnabled(cursor.getInt(LESSON_DISABLED) == 0);

            return convertView;
        }

    }

}
