package com.tughi.memoria;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class PracticeBreakFragment extends Fragment implements Handler.Callback, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_PRACTICE_TIME,
    };
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_PRACTICE_TIME = 0;

    private static final int MESSAGE_UPDATE_TIMER = 0;

    private TextView textView;

    private Handler handler = new Handler(this);

    private long practiceTime;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.practice_break_fragment, container, false);
        textView = (TextView) view.findViewById(R.id.break_time);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (practiceTime != 0) {
            if (practiceTime > 0) {
                handler.sendEmptyMessage(0);
            }
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (isResumed()) {
            long currentTime = System.currentTimeMillis();

            CharSequence timeText = DateUtils.getRelativeTimeSpanString(practiceTime, currentTime, DateUtils.SECOND_IN_MILLIS);
            textView.setText(getString(R.string.break_time, timeText));

            if (currentTime > practiceTime) {
                getActivity().getContentResolver().notifyChange(Exercises.CONTENT_URI, null);
            }

            handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_TIMER, DateUtils.SECOND_IN_MILLIS);
        }

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), Exercises.CONTENT_URI, EXERCISES_PROJECTION, null, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME);

            handler.sendEmptyMessage(MESSAGE_UPDATE_TIMER);
        }
        // TODO: handle the case where no exercises are left to practice on
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // ignored
    }

}
