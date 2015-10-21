package com.tughi.memoria;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Used to activate the synchronization with Google Drive (AppFolder).
 */
public class SyncSetupActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_RESOLVE_DRIVE_CONNECTION = 1;

    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = ((Application) getApplication()).getGoogleApiClient();
        googleApiClient.registerConnectionCallbacks(this);
        googleApiClient.registerConnectionFailedListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        googleApiClient.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_RESOLVE_DRIVE_CONNECTION) {
            if (resultCode == RESULT_OK) {
                googleApiClient.connect();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);

        super.onDestroy();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        startService(new Intent(this, SyncService.class).setAction(SyncService.ACTION_PULL));

        finish();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // ignored
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_DRIVE_CONNECTION);
            } catch (IntentSender.SendIntentException exception) {
                // TODO: inform user
                Log.e(getClass().getName(), "Failed to start resolution", exception);
            }
        } else {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 0)
                    .show();
        }
    }

}
