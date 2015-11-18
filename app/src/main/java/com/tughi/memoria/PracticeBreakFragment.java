package com.tughi.memoria;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
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
            Exercises.COLUMN_ID,
            Exercises.COLUMN_PRACTICE_TIME,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_DISABLED + " = 0 AND " + Exercises.COLUMN_RATING + " > 0";
    private static final String EXERCISES_SORT_ORDER = Exercises.COLUMN_PRACTICE_TIME;
    private static final int EXERCISE_ID = 0;
    private static final int EXERCISE_PRACTICE_TIME = 1;

    private static final int MESSAGE_UPDATE_TIMER = 0;

    private TextView textView;

    private Handler handler = new Handler(this);

    private long practiceTime;

    private long randomExerciseId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.practice_break_fragment, container, false);
        textView = (TextView) view.findViewById(R.id.break_time);

        view.findViewById(R.id.random).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (randomExerciseId > 0) {
                    view.setEnabled(false);

                    new AsyncTask<Object, Object, Boolean>() {
                        @Override
                        protected Boolean doInBackground(Object... params) {
                            final Context context = (Context) params[0];
                            final long exerciseId = (long) params[1];

                            ContentValues values = new ContentValues();
                            values.put(Exercises.COLUMN_PRACTICE_TIME, System.currentTimeMillis());
                            context.getContentResolver().update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exerciseId), values, null, null);

                            return Boolean.TRUE;
                        }
                    }.execute(getActivity().getApplicationContext(), randomExerciseId);
                }
            }
        });

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
        return new CursorLoader(getActivity(), Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, null, EXERCISES_SORT_ORDER);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            practiceTime = cursor.getLong(EXERCISE_PRACTICE_TIME);

            handler.sendEmptyMessage(MESSAGE_UPDATE_TIMER);

            int exercises = cursor.getCount();
            if (exercises > 3) {
                int randomExercise = (int) (exercises * 2 / 3. + Math.random() * exercises / 3.);
                if (cursor.moveToPosition(randomExercise)) {
                    randomExerciseId = cursor.getLong(EXERCISE_ID);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // ignored
    }

}
