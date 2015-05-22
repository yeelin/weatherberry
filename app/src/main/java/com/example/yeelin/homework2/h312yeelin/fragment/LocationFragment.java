package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.fragmentUtils.LocationUtils;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


/**
 * Created by ninjakiki on 5/21/15.
 * This fragment has no UI.  It's sole purpose is to get the current location from
 * google play services, and the make a network call to fetch the data, and save it
 * to the database.
 */
public class LocationFragment
        extends BasePlayServicesFragment
        implements LocationListener {
    //logcat
    private static final String TAG = LocationFragment.class.getCanonicalName();

    //constants
    private static final float ONE_KILOMETER = 1000f; //1000 meters
    private static final long ONE_MINUTE = 60 * 1000; //milliseconds
    private static final long THREE_MINUTES = 3 * ONE_MINUTE;
    private static final long THIRTY_SECONDS = 30 * 1000; //milliseconds

    //other constants
    private static final int RESOLVE_LOCATION_SETTINGS_REQUEST = 115;

    //member variables
    private Location currentBestLocation;
    private LocationRequest locationRequest;

    //listener member variable
    private LocationFragmentListener locationListener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface LocationFragmentListener extends BasePlayServicesFragmentListener {
        public void onNewLocation(Location location);
    }

    /**
     * Required empty public constructor
     */
    public LocationFragment() {}

    /**
     * Creates a new google api client builder that gets the current location
     * @return
     */
    @Override
    @NonNull
    public GoogleApiClient.Builder buildGoogleApiClient() {
        return new GoogleApiClient
                .Builder(getActivity())
                .addApi(LocationServices.API);
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
            locationListener = (LocationFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName() + " must implement LocationFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read the current best location from shared preferences
        currentBestLocation = LocationUtils.getSavedCurrentLocation(getActivity());
        //create a location request, will be used in onConnected
        locationRequest = createLocationRequest();
    }

    /**
     * Unsubscribe from location updates before stopping
     */
    @Override
    public void onStop() {
        //unsubscribe from location updates
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        super.onStop();
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
        locationListener = null;
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

        LocationSettingsRequest locationSettingsRequest = new LocationSettingsRequest
                .Builder()
                .addLocationRequest(locationRequest)
                .build();
        PendingResult<LocationSettingsResult> locationSettingsPendingResult = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest);
        locationSettingsPendingResult.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();
                switch (status.getStatusCode()) {

                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here.
                        requestLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), RESOLVE_LOCATION_SETTINGS_REQUEST);
                        }
                        catch (IntentSender.SendIntentException e) {
                            //ignore the error, since there is nothing we can do
                            requestLastKnownLocation();
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        requestLastKnownLocation();
                        break;

                    default:
                        requestLastKnownLocation();
                }
            }
        });

    }

    /**
     * LocationListener implementation
     * Updates the current best location if the candidateLocation is better than the current best.
     *
     * @param candidateLocation
     */
    @Override
    public void onLocationChanged(Location candidateLocation) {
        Log.d(TAG, "onLocationChanged: Candidate location:" + candidateLocation);

        if (LocationUtils.isBetterLocation(candidateLocation, currentBestLocation)) {
            Log.d(TAG, "onLocationChanged: Candidate location is better");
            updateLocation(candidateLocation);
        }
    }

    /**
     * Creates a new LocationRequest object
     * @return
     */
    private LocationRequest createLocationRequest() {
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(ONE_KILOMETER)
            .setInterval(ONE_MINUTE)
            .setFastestInterval(THIRTY_SECONDS)
            .setMaxWaitTime(THREE_MINUTES);
    }

    /**
     * Refreshes the last location by requesting the last known location first so that the user
     * doesn't wait.  Then requests for updates from Fused api using LocationRequest object created earlier.
     */
    private void requestLocationUpdates() {
        //request last known so that user doesn't wait
        requestLastKnownLocation();

        //request updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this);
    }

    /**
     * Requests the last location from the fused API, checks to see if it's better than the current best location,
     * and returns the better of the two.
     */
    private void requestLastKnownLocation() {
        Location candidateLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (LocationUtils.isBetterLocation(candidateLocation, currentBestLocation)) {
            Log.d(TAG, "requestLastKnownLocation: Using candidate location:" + candidateLocation);
            updateLocation(candidateLocation);
        }
        else {
            Log.d(TAG, "requestLastKnownLocation: Using current best location:" + currentBestLocation);
            updateLocation(currentBestLocation);
        }
    }

    /**
     * Call this method after you have verified that the new location is better than the current best location.
     * This method does 3 things:
     * 1. Updates the current best location to the new location
     * 2. Notifies the listener of the new location
     * 3. Saves the new location to shared preferences
     *
     * @param location
     */
    private void updateLocation (Location location) {
        //update the current best to the new location
        currentBestLocation = location;

        //notify listener
        locationListener.onNewLocation(currentBestLocation);

        //save it in shared preferences
        LocationUtils.saveCurrentLocation(getActivity(), currentBestLocation);
    }
}
