package com.tughi.memoria;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

public class PracticeReminderService extends IntentService {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_PRACTICE_TIME + " < CAST(? AS INTEGER)";

    public PracticeReminderService() {
        super(PracticeReminderService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] selectionArgs = { Long.toString(System.currentTimeMillis() / 1000) };
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, selectionArgs, null);
        int count = cursor.getCount();
        cursor.close();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (count > 0) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.practice_time))
                    .setContentText(getString(R.string.practice_exercises, count))
                    .build();
            notificationManager.notify(R.id.practice_notification, notification);
        } else {
            notificationManager.cancel(R.id.practice_notification);
        }
    }

}
