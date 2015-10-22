package com.tughi.memoria;

import com.google.android.gms.drive.events.ChangeEvent;
import com.google.android.gms.drive.events.DriveEventService;

/**
 * Handles the Google Drive events for the exercises file.
 */
public class ExercisesEventService extends DriveEventService {

    @Override
    public void onChange(ChangeEvent event) {
        if (event.hasContentChanged()) {
            // TODO: import file
        }
    }

}
