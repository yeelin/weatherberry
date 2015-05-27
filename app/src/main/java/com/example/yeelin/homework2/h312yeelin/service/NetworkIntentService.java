package com.example.yeelin.homework2.h312yeelin.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 4/15/15.
 */
public class NetworkIntentService
        extends IntentService
        implements FetchDataHelper.FetchDataHelperCallback {
    //logcat
    private static final String TAG = NetworkIntentService.class.getCanonicalName();

    //action in intent
    private static final String ACTION_MULTI_CITY_LOAD = NetworkIntentService.class.getSimpleName() + ".action.multiCityload";
    private static final String ACTION_FAVORITE_CITY_LOAD = NetworkIntentService.class.getSimpleName() + ".action.favoriteCityLoad";
    private static final String ACTION_CURRENT_LOCATION_LOAD = NetworkIntentService.class.getSimpleName() + ".action.currentLocationLoad";
    private static final String ACTION_WAKEFUL_LOAD = NetworkIntentService.class.getSimpleName() + ".action.wakefulLoad";
    private static final String ACTION_FAVORITE_CITIES_PURGE = NetworkIntentService.class.getSimpleName() + ".action.favoriteCitiesPurge";

    //extras in intent
    private static final String EXTRA_CITY_NAME = NetworkIntentService.class.getSimpleName() + ".cityName";
    private static final String EXTRA_CITY_LATITUDE = NetworkIntentService.class.getSimpleName() + ".cityLatitude";
    private static final String EXTRA_CITY_LONGITUDE = NetworkIntentService.class.getSimpleName() + ".cityLongitude";
    private static final String EXTRA_CITY_IDS = NetworkIntentService.class.getSimpleName() + ".cityIds";

    /**
     * Required by the manifest.
     */
    public NetworkIntentService() {
        super(NetworkIntentService.class.getSimpleName());
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public NetworkIntentService(String name) {
        super(name);
    }

    /**
     * Builds a regular intent to start this service. Pass this intent to
     * context.startService(). This is the regular counterpart to buildWakefulIntent.
     * @param context
     * @return
     */
    public static Intent buildIntentForMultiCityLoad(Context context) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_MULTI_CITY_LOAD);
        return intent;
    }

    /**
     * Builds an intent to load data for a favorite city (found via search). Pass this intent to
     * context.startService().
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     * @return
     */
    public static Intent buildIntentForFavoriteCityLoad(Context context, @Nullable String cityName, double latitude, double longitude) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_FAVORITE_CITY_LOAD);
        //set extras
        intent.putExtra(EXTRA_CITY_LATITUDE, latitude);
        intent.putExtra(EXTRA_CITY_LONGITUDE, longitude);
        if (cityName != null) {
            intent.putExtra(EXTRA_CITY_NAME, cityName);
        }
        return intent;
    }

    /**
     * Builds an intent to load data for a current location city (not favorited by the user). Pass this intent to
     * context.startService().
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     * @return
     */
    public static Intent buildIntentForCurrentLocationLoad(Context context, @Nullable String cityName, double latitude, double longitude) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_CURRENT_LOCATION_LOAD);
        //set extras
        intent.putExtra(EXTRA_CITY_LATITUDE, latitude);
        intent.putExtra(EXTRA_CITY_LONGITUDE, longitude);
        if (cityName != null) {
            intent.putExtra(EXTRA_CITY_NAME, cityName);
        }
        return intent;
    }

    /**
     * Builds an intent to start a wakeful version of this service.
     * When processing is done, it will call the WakefulBroadcastReceiver to release the wakeful lock.
     * @param context
     * @return
     */
    public static Intent buildWakefulIntent(Context context) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_WAKEFUL_LOAD);
        return intent;
    }

    /**
     * Builds an intent to delete favorite cities.  Pass this intent to context.startService().
     * @param context
     * @param cityIdsArrayList
     * @return
     */
    public static Intent buildIntentForFavoriteCitiesPurge(Context context, @NonNull ArrayList<Long> cityIdsArrayList) {
        Intent intent = new Intent(context, NetworkIntentService.class);
        //set action
        intent.setAction(ACTION_FAVORITE_CITIES_PURGE);
        //set extras
        intent.putExtra(EXTRA_CITY_IDS, cityIdsArrayList);
        return intent;
    }

    /**
     * Required override from IntentService.  Handles the requested action in the intent by calling the
     * appropriate helper method.  This method runs on a background thread.
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: Starting");

        if (intent == null) {
            Log.w(TAG, "onHandleIntent: Intent was null");
            return;
        }

        final String action = intent.getAction();

        if (ACTION_MULTI_CITY_LOAD.equals(action)) {
            //loads data for all cities while app is in the foreground
            FetchDataHelper.handleActionMultiCityLoad(this.getApplicationContext(), this);
        }
        else if (ACTION_FAVORITE_CITY_LOAD.equals(action)) {
            //loads data for a favorite city
            FetchDataHelper.handleActionFavoriteCityLoad(
                    this.getApplicationContext(),
                    intent.getStringExtra(EXTRA_CITY_NAME),
                    intent.getDoubleExtra(EXTRA_CITY_LATITUDE, 0),
                    intent.getDoubleExtra(EXTRA_CITY_LONGITUDE, 0));
        }
        else if (ACTION_CURRENT_LOCATION_LOAD.equals(action)) {
            //loads data for current location
            FetchDataHelper.handleActionCurrentLocationLoad(
                    this.getApplicationContext(),
                    intent.getStringExtra(EXTRA_CITY_NAME),
                    intent.getDoubleExtra(EXTRA_CITY_LATITUDE, 0),
                    intent.getDoubleExtra(EXTRA_CITY_LONGITUDE, 0));
        }
        else if (ACTION_WAKEFUL_LOAD.equals(action)) {
            //called by alarm service while app is in the background
            try {
                FetchDataHelper.handleActionMultiCityLoad(this.getApplicationContext(), this);
            }
            finally {
                //always release the wakeful log regardless
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
                Log.d(TAG, "onHandleIntent: Wakeful lock released");
            }
        }
        else if (ACTION_FAVORITE_CITIES_PURGE.equals(action)) {
            //deletes data for given city ids
            PurgeDataHelper.handleActionFavoriteCityPurge(
                    this.getApplicationContext(),
                    (ArrayList<Long>) intent.getSerializableExtra(EXTRA_CITY_IDS));
        }
        else {
            Log.d(TAG, "onHandleIntent: Unknown action:" + action);
        }

        Log.d(TAG, "onHandleIntent: Done");
    }

    /**
     * FetchDataHelperCallback method implementation.
     * @return false, always, since we don't want to cancel fetch ever from
     * this class.
     */
    @Override
    public boolean shouldCancelFetch() {
        return false;
    }
}
