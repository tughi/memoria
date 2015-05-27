package com.tughi.memoria;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.Calendar;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // force sync
        startService(new Intent(this, SyncService.class));

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

}
