package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.util.Calendar;

public class Application extends android.app.Application {

    private static final String EXERCISES_DRIVE_FILE = "exercises-v1.json";

    private GoogleApiClient googleApiClient;
    private DriveFile exercisesDriveFile;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                        new InitExercisesDriveFileTask().execute();
                    }

                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        if (connectionResult.hasResolution()) {
                            Context context = Application.this;

                            String text = "The synchronization feature requires access to Google Drive";
                            Notification notification = new NotificationCompat.Builder(context)
                                    .setContentTitle("Google Drive access required")
                                    .setContentText(text)
                                    .setSmallIcon(R.drawable.ic_notification)
                                    .setContentIntent(PendingIntent.getActivity(context, 0, new Intent(context, SyncSetupActivity.class), 0))
                                    .setAutoCancel(true)
                                    .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                                    .setColor(getResources().getColor(R.color.primary))
                                    .build();

                            NotificationManagerCompat.from(context).notify(R.id.google_drive_access_notification, notification);
                        }
                    }
                })
                .build();
        googleApiClient.connect();


        // set repeating practice alarm
        PendingIntent intent = PendingIntent.getService(this, 0, new Intent(this, PracticeReminderService.class), PendingIntent.FLAG_CANCEL_CURRENT);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.HOUR, 1);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), AlarmManager.INTERVAL_HOUR, intent);
    }

    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    public DriveFile getExercisesDriveFile() {
        return exercisesDriveFile;
    }

    public class InitExercisesDriveFileTask extends AsyncTask<Object, Object, DriveFile> {

        @Override
        protected DriveFile doInBackground(Object... params) {
            DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);

            if (exercisesDriveFile == null) {
                // check if file already exists
                Query query = new Query.Builder()
                        .addFilter(Filters.eq(SearchableField.TITLE, EXERCISES_DRIVE_FILE))
                        .build();
                DriveApi.MetadataBufferResult metadataBufferResult = appFolder.queryChildren(googleApiClient, query)
                        .await();

                for (Metadata metadata : metadataBufferResult.getMetadataBuffer()) {
                    exercisesDriveFile = Drive.DriveApi.getFile(googleApiClient, metadata.getDriveId());
                }

                metadataBufferResult.release();
            }

            if (exercisesDriveFile == null) {
                // create empty file
                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(EXERCISES_DRIVE_FILE)
                        .setMimeType("application/json")
                        .build();
                DriveFolder.DriveFileResult driveFileResult = appFolder.createFile(googleApiClient, changeSet, null)
                        .await();
                exercisesDriveFile = driveFileResult
                        .getDriveFile();
            }

            if (exercisesDriveFile != null) {
                // register for file updates
                exercisesDriveFile.addChangeSubscription(googleApiClient);
            }

            return exercisesDriveFile;
        }

    }

}
