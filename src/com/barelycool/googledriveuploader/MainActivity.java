package com.barelycool.googledriveuploader;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

/**
 * Main activity for the Google Drive Uploader application. Upon creation this
 * activity will attempt to connect to the Google Drive service. The first time
 * this activity is launched after install the user will be prompted to select a
 * Google account or to add a new one. Once the user selects an account a
 * connection to the Google Drive service will be attempted.
 */
public class MainActivity extends Activity implements ConnectionCallbacks,
    OnConnectionFailedListener
{
    // ::-----------------------------------------------------------------------
    // :: Activity Interface

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Build a GoogleApiClient object that will be used to connect with the
        // Google Drive service. Add this activity as both connection and
        // connection failed listeners so that appropriate action can be taken
        // once a connection is attempted.
        _googleApiClient =
            new GoogleApiClient.Builder(this).addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();

        Log.i(TAG, "Attempting to connect to the Google Drive service.");

        // Attempt to connect to the Google Drive service.
        _googleApiClient.connect();
    }

    @Override
    protected void onActivityResult(final int requestCode,
        final int resultCode, final Intent data)
    {
        switch (requestCode) {
            case RESOLVE_CONNECTION_REQUEST_CODE:
            {
                if (resultCode == RESULT_OK)
                {
                    _googleApiClient.connect();
                }

                break;
            }
            default:
            {
                // Do nothing
            }
        }
    }

    // ::-----------------------------------------------------------------------
    // :: ConnectionCallback Interface

    @Override
    public void onConnected(final Bundle arg0)
    {
        Log.i(TAG, "Successfully connected to the Google Drive service.");
    }

    @Override
    public void onConnectionSuspended(final int arg0)
    {
        // TODO Auto-generated method stub
    }

    // ::-----------------------------------------------------------------------
    // :: OnConnectionFailedListener Interface

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult)
    {
        if (connectionResult.hasResolution())
        {
            try
            {
                connectionResult.startResolutionForResult(this,
                    RESOLVE_CONNECTION_REQUEST_CODE);
            }
            catch (final IntentSender.SendIntentException e)
            {
                // Unable to resolve, message user appropriately.
            }
        }
        else
        {
            GooglePlayServicesUtil.getErrorDialog(
                connectionResult.getErrorCode(), this, 0).show();
        }
    }

    // ::-----------------------------------------------------------------------
    // :: Data Members

    // Client used to connect to the Google Drive service.
    private GoogleApiClient _googleApiClient;

    // ::-----------------------------------------------------------------------
    // :: Private Constants

    // Tag used for identification purposes for all log entries.
    private static final String TAG = "GoogleDriveUploader";

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 0;
}