package com.barelycool.googledriveuploader;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MainActivity extends Activity implements ConnectionCallbacks,
    OnConnectionFailedListener
{
    // ::-----------------------------------------------------------------------
    // :: Activity Interface

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient =
            new GoogleApiClient.Builder(this).addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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
                    mGoogleApiClient.connect();
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
        // TODO Auto-generated method stub
        Log.i("GoogleDriveUploader", "Connected!");
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

    private GoogleApiClient mGoogleApiClient;

    // ::-----------------------------------------------------------------------
    // :: Private Constants

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 0;
}