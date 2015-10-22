package com.tughi.memoria;

import android.content.Intent;

import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEventService;

/**
 * Handles the Google Drive events for the exercises file.
 */
public class ExercisesEventService extends DriveEventService {

    @Override
    public void onChange(ChangeEvent event) {
        if (event.hasContentChanged()) {
            startActivity(new Intent(this, SyncService.class).setAction(SyncService.ACTION_PULL));
        }
    }

}
