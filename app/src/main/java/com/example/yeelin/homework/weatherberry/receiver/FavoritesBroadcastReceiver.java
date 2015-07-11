package com.example.yeelin.homework.weatherberry.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.lang.ref.WeakReference;


/**
 * Created by ninjakiki on 7/10/15.
 */
public class FavoritesBroadcastReceiver extends BroadcastReceiver {
    //logcat
    private static final String TAG = FavoritesBroadcastReceiver.class.getCanonicalName();

    //broadcast intent
    private static final String ACTION_FAVORITE_SUCCESS = FavoritesBroadcastReceiver.class.getCanonicalName() + ".action.FAVORITE_SUCCESS";
    private static final String ACTION_FAVORITE_FAILURE = FavoritesBroadcastReceiver.class.getCanonicalName() + ".action.FAVORITE_FAILURE";
    private static final String EXTRA_CITY_NAME = FavoritesBroadcastReceiver.class.getSimpleName() + ".cityName";
    private static final String EXTRA_CITY_ID = FavoritesBroadcastReceiver.class.getSimpleName() + ".cityId";
    private static final String EXTRA_POSITION = FavoritesBroadcastReceiver.class.getSimpleName() + ".position";

    //member variables
    private final Context applicationContext;
    private final WeakReference<FavoritesListener> listenerWeakReference;

    public interface FavoritesListener {
        public void onFavoriteAddSuccess(final String cityName, final long cityId, final int position);
        public void onFavoriteAddFailure(final String cityName);
    }

    public static void broadcastFavoriteAddSuccess(Context context, String cityName, long cityId, int position) {
        //create an intent with the cityname and city id that was successfully added as a favorite
        Intent intent = new Intent(ACTION_FAVORITE_SUCCESS);
        intent.putExtra(EXTRA_CITY_NAME, cityName);
        intent.putExtra(EXTRA_CITY_ID, cityId);
        intent.putExtra(EXTRA_POSITION, position);

        //broadcast the intent
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    public static void broadcastFavoriteAddFailure(Context context, String cityName) {
        //create an intent with the cityname that failed to be added
        Intent intent = new Intent(ACTION_FAVORITE_FAILURE);
        intent.putExtra(EXTRA_CITY_NAME, cityName);

        //broadcast the intent
        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent);
    }

    /**
     * Constructor
     * @param context
     * @param listener
     */
    public FavoritesBroadcastReceiver(Context context, FavoritesListener listener) {
        //initialize member variables
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(listener);

        //create intent filter
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_FAVORITE_SUCCESS);
        intentFilter.addAction(ACTION_FAVORITE_FAILURE);

        //inform the local broadcast manager that we are interested in this intent filter
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
        localBroadcastManager.registerReceiver(this, intentFilter);
    }

    /**
     * Unregister favorite add success and failures.
     */
    public void unregister() {
        //inform the local broadcast manager that we are no longer interested
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(applicationContext);
        localBroadcastManager.unregisterReceiver(this);
    }

    /**
     * Required override for BroadcastReceiver
     * This method is called when the BroadcastReceiver is receiving an Intent broadcast. During this time you can use the other methods on BroadcastReceiver to view/modify the current result values.
     * This method is called on the main thread.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        FavoritesListener favoritesListener = listenerWeakReference.get();
        if (favoritesListener == null) {
            Log.d(TAG, "onReceive: Listener is null");
            return;
        }

        final String action = intent.getAction();
        if (ACTION_FAVORITE_SUCCESS.equals(action)) {
            final String cityName = intent.getStringExtra(EXTRA_CITY_NAME);
            final long cityId = intent.getLongExtra(EXTRA_CITY_ID, 0);
            final int position = intent.getIntExtra(EXTRA_POSITION, 0);

            Log.d(TAG, "onReceive: Favorite add success. Notifying listener");
            favoritesListener.onFavoriteAddSuccess(cityName, cityId, position);
        }
        else if (ACTION_FAVORITE_FAILURE.equals(action)){
            final String cityName = intent.getStringExtra(EXTRA_CITY_NAME);

            Log.d(TAG, "onReceive: Failure add failure. Notifying listener");
            favoritesListener.onFavoriteAddFailure(cityName);
        }
        else {
            Log.d(TAG, "onReceive: Unknown action:" + action);
        }
    }
}
