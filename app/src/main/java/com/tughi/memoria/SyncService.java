package com.tughi.memoria;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Handles the synchronization of the local database with Google Drive.
 */
public class SyncService extends IntentService {

    public static final String ACTION_PULL = BuildConfig.APPLICATION_ID + ".intent.action.PULL";
    public static final String ACTION_PUSH = BuildConfig.APPLICATION_ID + ".intent.action.PUSH";

    private GoogleApiClient googleApiClient;
    private DriveFile exercisesDriveFile;

    public SyncService() {
        super(SyncService.class.getSimpleName());
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
        if (!googleApiClient.isConnected()) {
            Log.w(getClass().getName(), "GoogleApiClient is not connected!");
            return;
        }

        if (exercisesDriveFile == null) {
            Log.w(getClass().getName(), "Missing exercises drive file!");
            return;
        }

        try {
            String action = intent.getAction();
            Log.i(getClass().getName(), action);
            switch (action) {
                case ACTION_PULL:
                    handlePull(this, googleApiClient, exercisesDriveFile);
                    break;
                case ACTION_PUSH:
                    handlePush();
                    break;
            }
        } catch (IOException exception) {
            Log.w(getClass().getName(), "Failed", exception);
        }
    }

    public static void handlePull(Context context, GoogleApiClient googleApiClient, DriveFile exercisesDriveFile) throws IOException {
        DriveContents exercisesContents = exercisesDriveFile.open(googleApiClient, DriveFile.MODE_READ_ONLY, null)
                .await()
                .getDriveContents();

        InputStream input = new FileInputStream(exercisesContents.getParcelFileDescriptor().getFileDescriptor());

        ExercisesJsonHelper.importFromJson(context, input);
    }

    private void handlePush() throws IOException {
        DriveContents exercisesContents = exercisesDriveFile.open(googleApiClient, DriveFile.MODE_WRITE_ONLY, null)
                .await()
                .getDriveContents();

        OutputStream output = new FileOutputStream(exercisesContents.getParcelFileDescriptor().getFileDescriptor());

        ExercisesJsonHelper.exportToJson(this, output);

        exercisesContents.commit(googleApiClient, null)
                .await();
    }

}
