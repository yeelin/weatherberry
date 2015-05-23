package com.example.yeelin.homework2.h312yeelin.fragmentUtils;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by ninjakiki on 5/21/15.
 */
public class LocationUtils {
    //logcat
    public static final String TAG = LocationUtils.class.getCanonicalName();

    //constants for shared preferences
    private static final String LOCATION_SHARED_PREFS = "locationSharedPreferences";
    private static final String LOCATION_PROVIDER = "sharedPrefs.locationProvider";
    private static final String LOCATION_LATITUDE = "sharedPrefs.locationLatitude";
    private static final String LOCATION_LONGITUDE = "sharedPrefs.locationLongitude";
    private static final String LOCATION_TIME = "sharedPrefs.locationTime";
    private static final String LOCATION_ACCURACY = "sharedPrefs.locationAccuracy";
    private static final String LOCATION_NAME = "sharedPrefs.locationName";

    //constants for checking if candidate location is better
    private static final long ONE_MINUTE_MILLIS = 60 * 1000;
    private static final long THIRTY_MINUTES_MILLIS = 30 * ONE_MINUTE_MILLIS;
    private static final float ONE_KILOMETER_METERS = 1000;
    private static final float HALF_KILOMETER_METERS = 500;

    /**
     * Returns true if the candidateLocation is better than the currentBestLocation.
     *
     * @param candidateLocation the candidate location to be evaluated
     * @param currentBestLocation the current location fix which is to be compared to the candidate location
     * @return
     */
    public static boolean isBetterLocation(@Nullable Location candidateLocation, @Nullable Location currentBestLocation) {
        //if the candidate is null, it cannot be better
        if (candidateLocation == null) {
            Log.d(TAG, "isBetterLocation: Candidate is null");
            return false;
        }
        //if current best is null, then anything is better than nothing
        if (currentBestLocation == null) {
            Log.d(TAG, "isBetterLocation: Current best is null, so candidate is fine");
            return true;
        }

        //check whether the candidate is newer or older
        long timeDelta = candidateLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > THIRTY_MINUTES_MILLIS;
        boolean isSignificantlyOlder = timeDelta < -THIRTY_MINUTES_MILLIS;
        boolean isNewer = timeDelta > 0;

        //If it's been more than thirty minutes, use the candidate location because the user has likely moved
        if (isSignificantlyNewer) {
            Log.d(TAG, "isBetterLocation: Candidate is significantly newer");
            return true;
        }
        //if candidate is so much older, it must be worse
        else if (isSignificantlyOlder) {
            Log.d(TAG, "isBetterLocation: Candidate is significantly older.");
            return false;
        }

        /* At the point, the candidate could be slightly newer or older, but not significantly newer or older... */

        //check distance moved
        float distanceDelta = candidateLocation.distanceTo(currentBestLocation);
        boolean isSignificantlyMoved = Math.abs(distanceDelta) > ONE_KILOMETER_METERS;

        //check accuracy (in meters) - radius of 68% confidence
        //the smaller the accuracy, the better
        int accuracyDelta = (int) (candidateLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > HALF_KILOMETER_METERS;

        //check provider
        boolean isFromSameProvider = isSameLocationProvider(candidateLocation.getProvider(), currentBestLocation.getProvider());

        //now for the final verdict...
        if (isMoreAccurate) {
            Log.d(TAG, "isBetterLocation: Candidate is newer and more accurate");
            return true;
        }
        else if (isNewer && !isLessAccurate) { //i.e. accuracyDelta was 0
            Log.d(TAG, "isBetterLocation: Candidate is newer and not less accurate");
            return true;
        }
        else if (isNewer && isSignificantlyMoved) {
            Log.d(TAG, "isBetterLocation: Candidate is newer and has significantly moved");
            return true;
        }
        else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            Log.d(TAG, "isBetterLocation: Candidate is newer, not significantly less accurate, and from the same provider");
            return true;
        }

        Log.d(TAG, "isBetterLocation: Candidate is not better");
        return false;
    }

    /**
     * Checks to see if two provider strings are the same, taking into account that they can be null.
     * @param candidateProvider
     * @param currentBestProvider
     * @return
     */
    private static boolean isSameLocationProvider (@Nullable String candidateProvider,@Nullable String currentBestProvider) {
        if (candidateProvider == null)
            return currentBestProvider == null;
        return candidateProvider.equals(currentBestProvider);
    }

    /**
     * Saves current location to shared preferences.  Does not save null locations or location names.
     * @param context
     * @param location
     * @param locationName
     */
    public static void saveCurrentLocation(Context context, @Nullable Location location, @Nullable String locationName) {
        if (location == null) {
            Log.d(TAG, "saveCurrentLocation: Not saving null location");
            return;
        }

        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(LOCATION_PROVIDER, location.getProvider());
        //doubles have to written as long bits
        editor.putLong(LOCATION_LATITUDE, Double.doubleToLongBits(location.getLatitude()));
        editor.putLong(LOCATION_LONGITUDE, Double.doubleToLongBits(location.getLongitude()));
        editor.putLong(LOCATION_TIME, location.getTime());
        editor.putFloat(LOCATION_ACCURACY, location.getAccuracy());

        if (locationName != null) {
            editor.putString(LOCATION_NAME, locationName);
        }

        //asynchronous write
        editor.apply();
    }

    /**
     * Returns the saved current location from shared preferences
     * @param context
     * @return
     */
    @Nullable
    public static Location getSavedCurrentLocation(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_SHARED_PREFS, Context.MODE_PRIVATE);

        String locationProvider = sharedPreferences.getString(LOCATION_PROVIDER, null);
        if (locationProvider == null) {
            //if the location provider is null, no need to check other fields; they must not be there.
            //return null for location
            Log.d(TAG, "getSavedCurrentLocation: Saved location is null");
            return null;
        }

        //reconstruct the location object using saved values
        Location savedLocation = new Location(locationProvider);
        savedLocation.setLatitude(Double.longBitsToDouble(sharedPreferences.getLong(LOCATION_LATITUDE, 0)));
        savedLocation.setLongitude(Double.longBitsToDouble(sharedPreferences.getLong(LOCATION_LONGITUDE, 0)));
        savedLocation.setTime(sharedPreferences.getLong(LOCATION_TIME, 0));
        savedLocation.setAccuracy(sharedPreferences.getFloat(LOCATION_ACCURACY, 0));

        Log.d(TAG, "getSavedCurrentLocation: Saved location:" + savedLocation);
        return savedLocation;
    }

    /**
     * Returns the saved current location name from shared preferences
     * @param context
     * @return
     */
    @Nullable
    public static String getSavedCurrentLocationName(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(LOCATION_SHARED_PREFS, Context.MODE_PRIVATE);

        String locationName = sharedPreferences.getString(LOCATION_NAME, null);
        return locationName;
    }
}
