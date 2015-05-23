package com.tughi.memoria;

import android.content.Intent;

public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        startService(new Intent(this, SyncService.class));
    }

}
