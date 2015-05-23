package com.example.yeelin.homework2.h312yeelin.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

/**
 * Created by ninjakiki on 4/15/15.
 */
public class NetworkIntentService
        extends IntentService
        implements FetchDataHelper.FetchDataHelperCallback {
    //logcat
    private static final String TAG = NetworkIntentService.class.getCanonicalName();

    //action in intent
    private static final String ACTION_LOAD = NetworkIntentService.class.getSimpleName() + ".action.load";
    private static final String ACTION_SINGLE_LOAD = NetworkIntentService.class.getSimpleName() + "action.singleLoad";
    private static final String ACTION_WAKEFUL_LOAD = NetworkIntentService.class.getSimpleName() + ".action.wakefulLoad";

    //extras in intent
    private static final String EXTRA_CITY_ID = NetworkIntentService.class.getSimpleName() + ".cityId";
    private static final String EXTRA_CITY_NAME = NetworkIntentService.class.getSimpleName() + ".cityName";
    private static final String EXTRA_CITY_LATITUDE = NetworkIntentService.class.getSimpleName() + ".cityLatitude";
    private static final String EXTRA_CITY_LONGITUDE = NetworkIntentService.class.getSimpleName() + ".cityLongitude";
    private static final String EXTRA_USER_FAVORITE = NetworkIntentService.class.getSimpleName() + ".isFavorite";

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
     * Start this service. Deprecated. Instead of this method, use buildIntent and call context.startService()
     * on the intent.
     * @deprecated
     * @param context
     */
    public static void startService(Context context) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action and extras
        intent.setAction(ACTION_LOAD);

        context.startService(intent);
    }

    /**
     * Builds a regular intent to start this service. Pass this intent to
     * context.startService(). This is the regular counterpart to buildWakefulIntent.
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_LOAD);

        return intent;
    }

    /**
     * Builds an intent to load data for a single city. Pass this intent to
     * context.startService().
     * @param context
     * @return
     */
    public static Intent buildIntentForSingleCityLoad(Context context, @Nullable String cityName, double latitude, double longitude, boolean userFavorite) {
        Intent intent = new Intent(context, NetworkIntentService.class);

        //set action
        intent.setAction(ACTION_SINGLE_LOAD);
        //set extras
        if (cityName != null) {
            intent.putExtra(EXTRA_CITY_NAME, cityName);
        }
        intent.putExtra(EXTRA_CITY_LATITUDE, latitude);
        intent.putExtra(EXTRA_CITY_LONGITUDE, longitude);
        intent.putExtra(EXTRA_USER_FAVORITE, userFavorite);

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

        if (ACTION_LOAD.equals(action)) {
            //app is in the foreground, user is interacting
            //loads data for all cities
            FetchDataHelper.handleActionLoad(this.getApplicationContext(), this);
        }
        else if (ACTION_SINGLE_LOAD.equals(action)) {
            //loads data for a single city
            FetchDataHelper.handleActionSingleLoad(
                    this.getApplicationContext(),
                    intent.getStringExtra(EXTRA_CITY_NAME),
                    intent.getDoubleExtra(EXTRA_CITY_LATITUDE, 0),
                    intent.getDoubleExtra(EXTRA_CITY_LONGITUDE, 0),
                    intent.getBooleanExtra(EXTRA_USER_FAVORITE, false));
        }
        else if (ACTION_WAKEFUL_LOAD.equals(action)) {
            //called by alarm service
            try {
                FetchDataHelper.handleActionLoad(this.getApplicationContext(), this);
            }
            finally {
                //always release the wakeful log regardless
                WakefulBroadcastReceiver.completeWakefulIntent(intent);
                Log.d(TAG, "onHandleIntent: Wakeful lock released");
            }
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
