package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public abstract class PracticeFragment extends Fragment {

    public static final int PRACTICE_IMMEDIATELY = 0;
    public static final int PRACTICE_NORMAL = 500;
    public static final int PRACTICE_DELAYED = 1500;

    private long exerciseId;
    private String exerciseScope;
    private String exerciseDefinition;
    private int exerciseRating;

    public String getExerciseScope() {
        return exerciseScope;
    }

    public String getExerciseDefinition() {
        return exerciseDefinition;
    }

    public int getExerciseRating() {
        return exerciseRating;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        exerciseId = arguments.getLong(Exercises.COLUMN_ID);
        exerciseScope = arguments.getString(Exercises.COLUMN_SCOPE);
        exerciseDefinition = arguments.getString(Exercises.COLUMN_DEFINITION);
        exerciseRating = arguments.getInt(Exercises.COLUMN_RATING);
    }

    protected boolean submitAnswer(String answerScope) {
        final boolean correct = checkAnswer(answerScope);

        new AsyncTask<Object, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Object... params) {
                final Context context = (Context) params[0];
                final ContentResolver contentResolver = context.getContentResolver();

                final long currentTime = System.currentTimeMillis();

                final int rating;
                final long practiceTime;
                if (correct) {
                    rating = exerciseRating + 1;

                    long interval = (long) (Math.pow(3, rating - 1));
                    interval += (long) (interval * (currentTime % 1000 + 1) / 1000.);

                    practiceTime = currentTime / 1000 + interval;
                } else {
                    rating = Math.round(exerciseRating / 2.3f);
                    practiceTime = 0;
                }

                ContentValues values = new ContentValues();
                values.put(Exercises.COLUMN_RATING, rating);
                values.put(Exercises.COLUMN_PRACTICE_TIME, practiceTime);
                int result = contentResolver.update(ContentUris.withAppendedId(Exercises.CONTENT_URI, exerciseId), values, null, null);

                if (result > 0) {
                    PendingIntent intent = PendingIntent.getService(context, 0, new Intent(context, SyncService.class), PendingIntent.FLAG_CANCEL_CURRENT);
                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarmManager.set(AlarmManager.RTC, currentTime + AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15, intent);
                }

                return Boolean.TRUE;
            }
        }.execute(getActivity().getApplicationContext());

        return correct;
    }

    private boolean checkAnswer(String answerScope) {
        StringBuilder exercise = new StringBuilder(exerciseScope.length());
        for (char c : exerciseScope.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                exercise.append(c);
            }
        }
        StringBuilder answer = new StringBuilder(answerScope.length());
        for (char c : answerScope.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                answer.append(c);
            }
        }
        return exercise.toString().equals(answer.toString());
    }

}
