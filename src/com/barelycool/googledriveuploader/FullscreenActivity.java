package com.barelycool.googledriveuploader;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

import com.barelycool.googledriveuploader.util.SystemUiHider;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.drive.Drive;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements
    ConnectionCallbacks, OnConnectionFailedListener
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

        setContentView(R.layout.activity_fullscreen);

        final View controlsView =
            findViewById(R.id.fullscreen_content_controls);
        final View contentView = findViewById(R.id.fullscreen_content);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider =
            SystemUiHider.getInstance(this, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
            .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener()
            {
                // Cached values.
                int mControlsHeight;
                int mShortAnimTime;

                @Override
                @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                public void onVisibilityChange(final boolean visible)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2)
                    {
                        // If the ViewPropertyAnimator API is available
                        // (Honeycomb MR2 and later), use it to animate the
                        // in-layout UI controls at the bottom of the
                        // screen.
                        if (mControlsHeight == 0)
                        {
                            mControlsHeight = controlsView.getHeight();
                        }
                        if (mShortAnimTime == 0)
                        {
                            mShortAnimTime =
                                getResources().getInteger(
                                    android.R.integer.config_shortAnimTime);
                        }
                        controlsView.animate()
                            .translationY(visible ? 0 : mControlsHeight)
                            .setDuration(mShortAnimTime);
                    }
                    else
                    {
                        // If the ViewPropertyAnimator APIs aren't
                        // available, simply show or hide the in-layout UI
                        // controls.
                        controlsView.setVisibility(visible ? View.VISIBLE
                            : View.GONE);
                    }

                    if (visible && AUTO_HIDE)
                    {
                        // Schedule a hide().
                        delayedHide(AUTO_HIDE_DELAY_MILLIS);
                    }
                }
            });

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                if (TOGGLE_ON_CLICK)
                {
                    mSystemUiHider.toggle();
                }
                else
                {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(
            mDelayHideTouchListener);

        mGoogleApiClient =
            new GoogleApiClient.Builder(this).addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onPostCreate(final Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
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
        }
    }

    // ::-----------------------------------------------------------------------
    // :: ConnectionCallback Interface

    @Override
    public void onConnected(final Bundle arg0)
    {
        // TODO Auto-generated method stub
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
    // :: Helpers

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(final int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    // ::-----------------------------------------------------------------------
    // :: Data Members

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private GoogleApiClient mGoogleApiClient;

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener =
        new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(final View view,
                final MotionEvent motionEvent)
            {
                if (AUTO_HIDE)
                {
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }
                return false;
            }
        };

    private final Handler mHideHandler = new Handler();

    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            mSystemUiHider.hide();
        }
    };

    // ::-----------------------------------------------------------------------
    // :: Private Constants

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    private static final int RESOLVE_CONNECTION_REQUEST_CODE = 0;
}