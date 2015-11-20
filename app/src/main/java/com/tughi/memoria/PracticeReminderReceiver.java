package com.tughi.memoria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PracticeReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, PracticeReminderService.class));
    }

}
