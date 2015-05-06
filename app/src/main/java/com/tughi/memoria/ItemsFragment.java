package com.tughi.memoria;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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

public class ItemsFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] ITEMS_PROJECTION = {
            Items.Columns.ID,
            Items.Columns.PROBLEM,
            Items.Columns.SOLUTION,
    };
    private static final int ITEM_ID = 0;
    private static final int ITEM_PROBLEM = 1;
    private static final int ITEM_SOLUTION = 2;

    private ItemsAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Items.CONTENT_URI, ITEMS_PROJECTION, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (adapter == null) {
            setListAdapter(adapter = new ItemsAdapter(cursor));
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
        startActivity(new Intent(getActivity(), ItemEditActivity.class).setData(ContentUris.withAppendedId(Items.CONTENT_URI, id)));
    }

    private class ItemsAdapter extends BaseAdapter {

        private Cursor cursor;

        public ItemsAdapter(Cursor cursor) {
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
            throw new IllegalStateException("Broken cursor state?");
        }

        @Override
        public long getItemId(int position) {
            if (cursor.moveToPosition(position)) {
                return cursor.getLong(ITEM_ID);
            }
            throw new IllegalStateException("Broken cursor state?");
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }

            Cursor cursor = getItem(position);
            ((TextView) convertView.findViewById(android.R.id.text1)).setText(cursor.getString(ITEM_PROBLEM));
            ((TextView) convertView.findViewById(android.R.id.text2)).setText(cursor.getString(ITEM_SOLUTION));

            return convertView;
        }

    }

}
