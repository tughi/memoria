package com.tughi.memoria;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.NotificationCompat;

public class PracticeReminderService extends IntentService {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_DISABLED + " = 0 AND " + Exercises.COLUMN_RATING + " > 0 AND " + Exercises.COLUMN_PRACTICE_TIME + " < CAST(? AS INTEGER)";

    public PracticeReminderService() {
        super(PracticeReminderService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String[] selectionArgs = {Long.toString(System.currentTimeMillis())};
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, selectionArgs, null);
        if (cursor == null) {
            return;
        }
        int count = cursor.getCount();
        cursor.close();

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (count > 0) {
            Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(getString(R.string.practice_time))
                    .setContentText(getString(R.string.practice_exercises, count))
                    .setContentIntent(PendingIntent.getActivity(this, 0, new Intent(this, PracticeActivity.class), PendingIntent.FLAG_CANCEL_CURRENT))
                    .setAutoCancel(true)
                    .build();
            notificationManager.notify(R.id.practice_notification, notification);
        } else {
            notificationManager.cancel(R.id.practice_notification);
        }
    }

}
