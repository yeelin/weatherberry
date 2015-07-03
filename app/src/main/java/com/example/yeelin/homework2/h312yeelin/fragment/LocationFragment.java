package com.example.yeelin.homework2.h312yeelin.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.fragmentUtils.LocationUtils;
import com.example.yeelin.homework2.h312yeelin.service.NetworkIntentService;
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

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;


/**
 * Created by ninjakiki on 5/21/15.
 * This fragment has no UI.  It's sole purpose is to get the current location from
 * google play services, and the make a network call to fetch the data, and save it
 * to the database.
 */
public class LocationFragment
        extends BasePlayServicesFragment
        implements LocationListener,
        Handler.Callback {

    //logcat
    private static final String TAG = LocationFragment.class.getCanonicalName();

    //location service constants
    private static final float ONE_KILOMETER = 1000f; //1000 meters
    private static final long ONE_MINUTE = 60 * 1000; //milliseconds
    private static final long THREE_MINUTES = 3 * ONE_MINUTE;
    private static final long FIVE_MINUTES = 5 * ONE_MINUTE;
    private static final long TEN_MINUTES = 10 * ONE_MINUTE;
    private static final long THIRTY_SECONDS = 30 * 1000; //milliseconds

    //handler message
    private static final int MESSAGE_GET_SAVED_LOCATION = 100;

    //member variables
    private Location currentBestLocation;
    private String currentBestLocationName;
    private LocationRequest locationRequest;
    //private boolean hasRequestedLocationUpdates = false;

    //listener member variable
    //private LocationFragmentListener locationListener;

    //handler member variable for reading shared preferences
    private Handler handler;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
//    public interface LocationFragmentListener extends BasePlayServicesFragmentListener {
//        public void onNewLocation(@Nullable Location location, @Nullable String locationName);
//    }

    /**
     * Returns a new instance of the Location fragment. Use this instead of calling the constructor
     * directly.
     * @return
     */
    public static LocationFragment newInstance() {
        return new LocationFragment();
    }

    /**
     * Required empty public constructor
     */
    public LocationFragment() {}

    /**
     * Called from activity when location setting is enabled by the user when
     * presented with the dialog.
     * Starts the process of getting location updates.
     */
    public void onLocationSettingEnabled() {
        Log.d(TAG, "onLocationSettingEnabled: Requesting location updates now");
        requestLocationUpdates();
    }

    /**
     * Called from activity when the user does not enable the location setting when
     * presented with the dialog.
     * Starts the process of getting the last known location (as requested by other apps).
     */
    public void onLocationSettingNotEnabled() {
        Log.d(TAG, "onLocationSettingNotEnabled: Requesting last known location");
        requestLastKnownLocation();
    }

    /**
     * BasePlayServicesFragment override
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

//    /**
//     * Make sure the hosting activity or fragment implements the listener interface.
//     * @param activity
//     */
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        Fragment parent = getParentFragment();
//        Object objectToCast = parent != null ? parent : activity;
//        try {
//            locationListener = (LocationFragmentListener) objectToCast;
//        }
//        catch (ClassCastException e) {
//            throw new ClassCastException(objectToCast.getClass().getSimpleName()
//                    + " must implement LocationFragmentListener");
//        }
//    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //create a handler thread for the handler
        HandlerThread handlerThread = new HandlerThread("SharedPreferencesThread");
        handlerThread.start();
        //create a handler using the provided looper and this class will handle the messages
        handler = new Handler(handlerThread.getLooper(), this);
        handler.sendMessage(handler.obtainMessage(MESSAGE_GET_SAVED_LOCATION));

        //create a location request, will be used in onConnected
        locationRequest = createLocationRequest();
    }

    /**
     * Override BasePlayServicesFragment
     * Unsubscribe from location updates before stopping
     */
    @Override
    public void onStop() {
        //check if we have asked for location updates, and then unsubscribe
        //if (hasRequestedLocationUpdates) {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        super.onStop();
    }

    /**
     * Terminate the handler's looper without processing any more messages in the queue.
     * Doing this in onDestroy since we created the handler in onCreate
     */
    @Override
    public void onDestroy() {
        handler.getLooper().quit();
        handler = null;

        super.onDestroy();
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
//        locationListener = null;
        super.onDetach();
    }

    /**
     * Handler.Callback implementation
     * This callback happens when the handler receives a message.
     * Read the current best location and the city name from shared preferences.  Doing this
     * on a background thread to avoid strict mode violation.
     * @param msg
     * @return
     */
    @Override
    public boolean handleMessage(Message msg) {
        Log.d(TAG, "handleMessage");
        if (msg.what == MESSAGE_GET_SAVED_LOCATION) {
            currentBestLocation = LocationUtils.getSavedCurrentLocation(getActivity());
            currentBestLocationName = LocationUtils.getSavedCurrentLocationName(getActivity());
            //return true since we handled the message
            return true;
        }
        return false;
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
                        //all location settings are satisfied. The client can initialize location requests.
                        Log.d(TAG, "onConnected: LocationSettingsStatusCodes.SUCCESS");
                        requestLocationUpdates();
                        break;

                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        //location settings are not satisfied, but could be fixed by showing the user a dialog.
                        Log.d(TAG, "onConnected: LocationSettingsStatusCodes.RESOLUTION_REQUIRED");
                        try {
                            //show dialog by calling startResolutionForResult(),
                            //check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CODE_LOCATION_SETTINGS_RESOLUTION);
                        }
                        catch (IntentSender.SendIntentException e) {
                            //ignore the error, since there is nothing we can do
                            //request last known location
                            requestLastKnownLocation();
                        }
                        break;

                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        Log.d(TAG, "onConnected: LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE");
                        //request last known location
                        requestLastKnownLocation();
                        break;

                    default:
                        Log.d(TAG, "onConnected: LocationSettingsStatusCodes.default");
                        //request last known location
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
    @NonNull
    private LocationRequest createLocationRequest() {
        Log.d(TAG, "createLocationRequest");
        return LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setSmallestDisplacement(ONE_KILOMETER)
            .setInterval(FIVE_MINUTES)
            .setFastestInterval(THREE_MINUTES)
            .setMaxWaitTime(TEN_MINUTES);
    }

    /**
     * Refreshes the last location by requesting the last known location first so that the user
     * doesn't wait.  Then requests for updates from Fused api using LocationRequest object created earlier.
     */
    private void requestLocationUpdates() {
        Log.d(TAG, "requestLocationUpdates");
        //request last known location so that user doesn't wait
        requestLastKnownLocation();

        //request fresh location updates
        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient,
                locationRequest,
                this);

        //hasRequestedLocationUpdates = true;
    }

    /**
     * Requests the last location from the fused API, checks to see if it's better than the current best location,
     * and updates the location data for the location it deems the best.
     */
    private void requestLastKnownLocation() {
        Log.d(TAG, "requestLastKnownLocation");
        Location candidateLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (LocationUtils.isBetterLocation(candidateLocation, currentBestLocation)) {
            //candidate location is better
            Log.d(TAG, "requestLastKnownLocation: Using candidate location:" + candidateLocation);
            updateLocation(candidateLocation);
        }
        else {
            //candidate is not better, so use current best
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
    private void updateLocation (@Nullable Location location) {
        Log.d(TAG, "updateLocation: Location: " + location);
        //update the current best to the new location
        currentBestLocation = location;

        if (currentBestLocation != null) {
            //attempt to geocode the new location
            geocodeCurrentLocation();

            //notify listener later when geocoder task returns
            //locationListener.onNewLocation(currentBestLocation);

            //save location in shared preferences
            //LocationUtils.saveCurrentLocation(getActivity(), currentBestLocation);
        }
    }

    /**
     * If the geocoder is present, try to geocode the current location before calling
     * the open weather api.  Otherwise, just call the open weather api.
     *
     * Note: currentBestLocation should not be null when this method is called.
     */
    private void geocodeCurrentLocation() {
        if (Geocoder.isPresent()) {
            //try to geocode the lat/long before calling open weather api
            new GeocodeTask(getActivity(), this, currentBestLocation).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
        else {
            //no geocoder present, so just call open weather api
            fetchDataForCurrentLocation(false, null);
        }
    }

    /**
     * Starts an intent service to load the current location's weather data. This is called by the Geocode
     * async task when it's done geocoding the current best location.
     *
     * Note: currentBestLocation should not be null when this method is called.
     * @param isLocationGeocoded
     * @param cityName
     */
    private void fetchDataForCurrentLocation(boolean isLocationGeocoded, @Nullable String cityName) {
        //update the current best location's name
        currentBestLocationName = cityName;

        //save current location and it's name in shared preferences
        LocationUtils.saveCurrentLocation(getActivity(), currentBestLocation, currentBestLocationName);

        //fetch weather data for current location from the network
        Intent currentLocationLoadIntent = NetworkIntentService.buildIntentForCurrentLocationLoad(
                getActivity(),
                currentBestLocationName,
                currentBestLocation.getLatitude(),
                currentBestLocation.getLongitude());
        getActivity().startService(currentLocationLoadIntent);

        //notify listener about the new location with city name
        //locationListener.onNewLocation(currentBestLocation, currentBestLocationName);
    }

    /**
     * Async task for geocoding a location.
     */
    private class GeocodeTask extends AsyncTask<Void, Void, String> {
        //logcat
        private final String TAG = GeocodeTask.class.getCanonicalName();
        //constants
        private final int MAX_GEOCODING_RESULTS = 1;
        //member variables
        private final WeakReference<LocationFragment> locationFragmentWeakReference;
        private final Context applicationContext;
        private final Location location;

        GeocodeTask(Context context, LocationFragment locationFragment, Location location) {
            locationFragmentWeakReference = new WeakReference<>(locationFragment);
            applicationContext = context.getApplicationContext();
            this.location = location;
        }

        /**
         * Calls geocoder.getFromLocation and returns the cityName for the given location.
         * @param params
         * @return
         */
        @Nullable
        @Override
        protected String doInBackground(Void... params) {
            Geocoder geocoder = new Geocoder(applicationContext);
            try {
                //geocoder.getFromLocation is a synchronous call
                //for simplicity, we are requesting max results of 1
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), MAX_GEOCODING_RESULTS);
                if (addressList == null || addressList.isEmpty()) {
                    Log.d(TAG, "doInBackground: No address was found");
                }
                else {
                    Address address = addressList.get(0);
                    String cityName = address.getLocality();
                    Log.d(TAG, "doInBackground: City name: " + cityName);
                    return cityName;
                }
            }
            catch (IOException e) {
                //catch network or other i/o problems
                Log.e(TAG, "doInBackground: Geocoder is unavailable", e);
            }
            catch (IllegalArgumentException e) {
                //catch invalid latitude or longitude values
                Log.e(TAG, String.format("doInBackground: Invalid location values: %f, %f", location.getLatitude(), location.getLongitude()), e);
            }

            //no address was found, or we hit an exception so return null
            return null;
        }

        /**
         * The string result from doInBackground is passed in as parameter.
         * Calls location fragment's on
         * @param s city name (as geocoded from the lat/long)
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            LocationFragment locationFragment = locationFragmentWeakReference.get();
            if (locationFragment == null) {
                Log.d(TAG, "onPostExecute: Location fragment has gone away");
                return;
            }

            Log.d(TAG, "onPostExecute: City name: " + s);
            locationFragment.fetchDataForCurrentLocation(s != null, s);
        }
    }
}
