package com.example.yeelin.homework.weatherberry.fragment;

import android.app.Activity;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by ninjakiki on 5/21/15.
 * This is the base fragment for all fragments that wish to use google play services.
 */
public abstract class BasePlayServicesFragment
        extends Fragment
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //logcat
    private static final String TAG = BasePlayServicesFragment.class.getCanonicalName();

    //states for SaveInstanceState
    private static final String EXTRA_RESOLVING_ERROR = SearchFragment.class.getSimpleName() + ".resolvingError";

    //request code constants
    public static final int REQUEST_CODE_PLAY_SERVICES_RESOLUTION = 100;
    public static final int REQUEST_CODE_LOCATION_SETTINGS_RESOLUTION = 115;

    //member variables
    protected GoogleApiClient googleApiClient;
    protected boolean resolvingError;

    //listener member variable
    private BasePlayServicesFragmentListener baseListener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface BasePlayServicesFragmentListener {
        public void showPlayServicesErrorDialog(int errorCode);
    }

    /**
     * Required empty public constructor
     */
    public BasePlayServicesFragment() {

    }

    /**
     * Abstract method.  Build the google api client that uses the APIs that the implementing class needs.
     * This method is called in onCreate and must be implemented by child classes.
     */
    @NonNull
    public abstract GoogleApiClient.Builder buildGoogleApiClient();

    /**
     * The hosting Activity uses this method to notify this fragment that play services error
     * has been resolved. If not connected, or connecting, it will restart the connection process.
     */
    public void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable");

        //we are done resolving the error so set to false
        resolvingError = false;

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
            Log.d(TAG, "onPlayServicesAvailable: Connect called");
        }
    }

    /**
     * Make sure the hosting activity or fragment implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;

        try {
            baseListener = (BasePlayServicesFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement BasePlayServicesFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read the saved instance state if it's not null
        resolvingError = savedInstanceState != null &&
                savedInstanceState.getBoolean(EXTRA_RESOLVING_ERROR, false);

        GoogleApiClient.Builder builder = buildGoogleApiClient();
        googleApiClient = builder
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Call connect on the google api client
     */
    @Override
    public void onStart() {
        super.onStart();

        //if resolving error, don't start the client. It is already started.
        if (!resolvingError) {
            googleApiClient.connect();
        }
    }

    /**
     * Save out the resolving error in case we get destroyed
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_RESOLVING_ERROR, resolvingError);
    }

    /**
     * Disconnect the google api client
     */
    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Unregister callbacks
     */
    @Override
    public void onDestroy() {
        googleApiClient.unregisterConnectionCallbacks(this);
        googleApiClient.unregisterConnectionFailedListener(this);
        googleApiClient = null;
        super.onDestroy();
    }

    /**
     * Nullify the listener
     */
    @Override
    public void onDetach() {
        baseListener = null;
        super.onDetach();
    }

    /**
     * GoogleApiClient.ConnectionCallbacks implementation
     * This callback happens when we are connected to Google Play Services.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: Connected to Google Play Services!");
    }

    /**
     * GoogleApiClient.ConnectionCallbacks implementation
     * This callback happens when our connection is interrupted.
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: Connection to Google Play Services suspended");
    }

    /**
     * GoogleApiClient.OnConnectionFailedListener implementation
     * Handle errors that occurred while attempting to connect with Google.
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed: Failed to connect to Google Play Services");
        if (resolvingError) {
            //already attempting to resolve the error
            return;
        }

        if (connectionResult.hasResolution()) {
            //If it returns true, you can request the user take immediate action to resolve the error
            try {
                resolvingError = true;
                //check for the result in onActivityResult() in the activity class
                connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_PLAY_SERVICES_RESOLUTION);

            }
            catch (IntentSender.SendIntentException e) {
                //there was an error with the resolution. try again.
                googleApiClient.connect();
                resolvingError = false;
            }
        }
        else {
            // Show error dialog using GooglePlayServicesUtil.getErrorDialog()
            baseListener.showPlayServicesErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }
}
