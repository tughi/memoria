package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

import java.util.Calendar;

public class Application extends android.app.Application {

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
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

}
