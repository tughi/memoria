package com.tughi.memoria;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;

import java.io.IOException;
import java.util.Date;

public class PracticeReminderService extends IntentService {

    private static final String[] EXERCISES_PROJECTION = {
            Exercises.COLUMN_ID,
            Exercises.COLUMN_NEW,
    };
    private static final String EXERCISES_SELECTION = Exercises.COLUMN_PRACTICE_TIME + " < CAST(? AS INTEGER)";

    private GoogleApiClient googleApiClient;
    private DriveFile exercisesDriveFile;

    public PracticeReminderService() {
        super(PracticeReminderService.class.getSimpleName());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Application application = (Application) getApplication();
        googleApiClient = application.getGoogleApiClient();
        exercisesDriveFile = application.getExercisesDriveFile();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (googleApiClient.isConnected() && exercisesDriveFile != null) {
            Status status = Drive.DriveApi.requestSync(googleApiClient).await();

            if (status.isSuccess()) {
                DriveResource.MetadataResult metadataResult = exercisesDriveFile.getMetadata(googleApiClient).await();
                if (metadataResult.getStatus().isSuccess()) {
                    Metadata metadata = metadataResult.getMetadata();
                    Date modifiedDate = metadata.getModifiedDate();

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    long lastPullDate = preferences.getLong("last_pull_date", 0);
                    if (lastPullDate < modifiedDate.getTime()) {
                        try {
                            SyncService.handlePull(this, googleApiClient, exercisesDriveFile);

                            preferences.edit()
                                    .putLong("last_pull_date", modifiedDate.getTime())
                                    .apply();
                        } catch (IOException exception) {
                            Log.w(getClass().getName(), "Failed to open the drive file", exception);
                        }
                    }
                }
            }
        }

        String[] selectionArgs = {Long.toString(System.currentTimeMillis() / 1000)};
        Cursor cursor = getContentResolver().query(Exercises.CONTENT_URI, EXERCISES_PROJECTION, EXERCISES_SELECTION, selectionArgs, null);
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
