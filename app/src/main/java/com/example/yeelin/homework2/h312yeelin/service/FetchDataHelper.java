package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.networkUtils.CacheUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/30/15.
 */
public class FetchDataHelper {
    //logcat
    private static final String TAG = FetchDataHelper.class.getCanonicalName();

    //constants
    private static final int TEN_MINUTES_MILLIS = 10 * 60 * 1000; //minimum interval between multi-city fetches

    //projections
    private static final String[] PROJECTION_MIN_TIMESTAMP = new String[] {"min(" + CurrentWeatherContract.Columns.TIMESTAMP + ")"};

    public interface FetchDataHelperCallback {
        public boolean shouldCancelFetch();
    }

    /**
     * Helper method that handles action to load data for a newly favorited city (user_favorite = true)
     * on the background thread. This method must be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     *
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     */
    public static void handleActionFavoriteCityLoad(Context context,
                                                    @Nullable String cityName,
                                                    double latitude,
                                                    double longitude) {

        Log.d(TAG, String.format("handleActionFavoriteCityLoad: City: %s (%f, %f)", cityName, latitude, longitude));
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch city id corresponding to city name and coordinates
            final long cityId = FindCityDataHelper.findCityId(cityName, latitude, longitude);
            if (cityId == BaseWeatherContract.NO_ID) return;

            //fetch current weather, daily forecast, and tri hour for city id
            final ArrayList<ContentValues> currentWeatherValues = CurrentWeatherDataHelper.getDataForCityId(context, cityId, true);
            final ArrayList<ContentValues> dailyForecastValues = DailyForecastDataHelper.getDataForCityId(context, cityId, true);
            final ArrayList<ContentValues> triHourForecastValues = TriHourForecastDataHelper.getDataForCityId(context, cityId, true);

            //fetch images
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(currentWeatherValues, dailyForecastValues, triHourForecastValues));
        }
        catch (Exception e) {
            Log.e(TAG, "handleActionFavoriteCityLoad: Unexpected error", e);
        }

        CacheUtils.logCache();
    }

    /**
     * Helper method that handles action to load data for the current location (user_favorite = false)
     * on the background thread.  This method must be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     */
    public static void handleActionCurrentLocationLoad(Context context,
                                                       @Nullable String cityName,
                                                       double latitude,
                                                       double longitude) {
        Log.d(TAG, String.format("handleActionCurrentLocationLoad: City: %s (%f, %f)", cityName, latitude, longitude));
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //fetch city id corresponding to city name and coordinates
            final long cityId = FindCityDataHelper.findCityId(cityName, latitude, longitude);
            if (cityId == BaseWeatherContract.NO_ID) return;

            //delete previous entry for current location
            CurrentWeatherDataHelper.purgeOldData(context);
            //TODO: purge from daily forecast and tri hour forecast tables as well

            //fetch current weather, daily forecast, and tri hour for city id
            final ArrayList<ContentValues> currentWeatherValues = CurrentWeatherDataHelper.getDataForCityId(context, cityId, false);
            final ArrayList<ContentValues> dailyForecastValues = DailyForecastDataHelper.getDataForCityId(context, cityId, false);
            final ArrayList<ContentValues> triHourForecastValues = TriHourForecastDataHelper.getDataForCityId(context, cityId, false);

            //fetch images
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(currentWeatherValues, dailyForecastValues, triHourForecastValues));
        }
        catch (Exception e) {
            Log.e(TAG, "handleActionCurrentLocationLoad: Unexpected error", e);
        }
        CacheUtils.logCache();
    }

    /**
     * Helper method that handles action to refetch data for all existing cities in the db
     * on the background thread.  This method must be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     * 2. Network Job Service: doInBackground()
     */
    public static void handleActionMultiCityLoad(Context context, FetchDataHelperCallback helperCallback) {
        Log.d(TAG, "handleActionMultiCityLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        if (!isDataStale(context)) {
            Log.d(TAG, "handleActionMultiCityLoad: Data is still fresh");
            return;
        }

        try {
            //check if we should exit early
            if (helperCallback.shouldCancelFetch()) {
                Log.d(TAG, "handleActionMultiCityLoad: Fetch was cancelled before getData.");
                return;
            }

            //get all the city ids and favorites
            final HashMap<Long, ArrayList<Integer>> cityIdsToFavoritesMap = GroupCurrentWeatherDataHelper.getCityIdsAndFavorites(context);
            if (cityIdsToFavoritesMap == null || cityIdsToFavoritesMap.size() == 0) {
                Log.d(TAG, "handleActionMultiCityLoad: No cities in the db so nothing to load");
                return;
            }

            //get current weather data using group query
            GroupCurrentWeatherDataHelper.getDataForMultipleCityIds(context,
                    cityIdsToFavoritesMap.keySet().toArray(new Long[cityIdsToFavoritesMap.size()]),
                    cityIdsToFavoritesMap);

            //loop through all city ids and get forecast data since forecast API doesn't support group queries
            for (Long cityId : cityIdsToFavoritesMap.keySet()) {
                //get daily forecast data
                DailyForecastDataHelper.getDataForCityId(context, cityId, true);

                //get tri hour forecast data
                TriHourForecastDataHelper.getDataForCityId(context, cityId, true);
            }

            //purge anything that is too old i.e. anything earlier than today at 12:00 AM
            DailyForecastDataHelper.purgeOldData(context);
            TriHourForecastDataHelper.purgeOldData(context);

            //fetch weather icons to pre-warm the cache
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(context));
        }
        catch (Exception e) {
            Log.e(TAG, "handleActionMultiCityLoad: Unexpected error", e);
        }
        CacheUtils.logCache();
    }

    /**
     * Helper method that checks if data is stale
     * @param context
     * @return
     */
    private static boolean isDataStale(Context context) {
        //check last fetch time before fetching again
        final long lastFetchMillis = determineLastFetch(context);
        final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm Z", Locale.US);
        if (System.currentTimeMillis() - lastFetchMillis < TEN_MINUTES_MILLIS) {
            //last fetch < 10 minutes ago so skip
            Log.d(TAG, String.format("isDataStale: Last fetch was less than 10 minutes ago. Current time:%s, Last fetch:%s",
                    formatter.format(new Date(System.currentTimeMillis())),
                    formatter.format(new Date(lastFetchMillis))));
            return false;
        }

        //last fetch > 10 minutes ago so yes data is stale
        Log.d(TAG, String.format("isDataStale: Last fetch was more than 10 minutes ago. Current time:%s, Last fetch:%s",
                formatter.format(new Date(System.currentTimeMillis())),
                formatter.format(new Date(lastFetchMillis))));
        return true;
    }

    /**
     * Helper method that checks the current_weather table for the timestamp of
     * the last call to the API.
     * @return
     */
    private static long determineLastFetch(Context context) {
        //retrieve the minimum timestamp from the current_weather table
        Cursor cursor = context.getContentResolver().query(CurrentWeatherContract.URI, PROJECTION_MIN_TIMESTAMP, null, null, null);
        long lastFetchMillis = 0;
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                lastFetchMillis = cursor.getLong(0);
            }
        }
        finally {
            cursor.close();
        }

        //return the timestamp
        return lastFetchMillis;
    }
}
