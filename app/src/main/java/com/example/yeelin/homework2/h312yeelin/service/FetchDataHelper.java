package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.networkUtils.CacheUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/30/15.
 */
public class FetchDataHelper {
    //logcat
    private static final String TAG = FetchDataHelper.class.getCanonicalName();

    //minimum interval between fetches
    private static final int TEN_MINUTES_MILLIS = 10 * 60 * 1000;

    //projections
    private static final String[] MAX_TIMESTAMP_PROJECTION = new String[] {"max(" + CurrentWeatherContract.Columns.TIMESTAMP + ")"};
    private static final String[] CITY_ID_PROJECTION = new String[] { CurrentWeatherContract.Columns.CITY_ID };

    public interface FetchDataHelperCallback {
        public boolean shouldCancelFetch();
    }

    /**
     * Helper method that handles action single load on the background thread.
     * Should be called from a background thread.
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
                                                    double longitude,
                                                    boolean userFavorite) {

        Log.d(TAG, "handleActionFavoriteCityLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            long cityId = findCityId(cityName, latitude, longitude);
            if (cityId == 0) {
                Log.d(TAG, "handleActionFavoriteCityLoad: Could not find cityId for: " + cityName);
                return;
            }
            //get data
            ContentValues[] currentWeatherValues = CurrentWeatherDataHelper.getDataForCityId(context, cityId, true);
            ContentValues[] dailyForecastValues = DailyForecastDataHelper.getDataForCityId(context, cityId, true);
            ContentValues[] triHourForecastValues = TriHourForecastDataHelper.getDataForCityId(context, cityId, true);

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
        Log.d(TAG, "handleActionCurrentLocationLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //find city id
            long cityId = findCityId(cityName, latitude, longitude);
            if (cityId == 0) {
                Log.d(TAG, "handleActionCurrentLocationLoad: Could not find cityId for: " + cityName);
                return;
            }

            //delete previous entry for current location
            CurrentWeatherDataHelper.purgeOldData(context);

            //get current weather, daily forecast, and tri hour for city id
            ContentValues[] currentWeatherValues = CurrentWeatherDataHelper.getDataForCityId(context, cityId, false);
            ContentValues[] dailyForecastValues = DailyForecastDataHelper.getDataForCityId(context, cityId, false);
            ContentValues[] triHourForecastValues = TriHourForecastDataHelper.getDataForCityId(context, cityId, false);

            //get images
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(currentWeatherValues, dailyForecastValues, triHourForecastValues));
        }
        catch (Exception e) {
            Log.e(TAG, "handleActionFavoriteCityLoad: Unexpected error", e);
        }
        CacheUtils.logCache();
    }

    /**
     * Helper method that handles action load on the background thread.
     * Should be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     * 2. Network Job Service: doInBackground()
     */
    public static void handleActionLoad(Context context, FetchDataHelperCallback helperCallback) {
        Log.d(TAG, "handleActionLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        if (!isDataStale(context)) {
            Log.d(TAG, "handleActionLoad: Data is still fresh");
            return;
        }

        try {
            //check if we should exit early
            if (helperCallback.shouldCancelFetch()) {
                Log.d(TAG, "handleActionLoad: Fetch was cancelled before getData.");
                return;
            }

            //get all the city ids
            Long[] cityIds = getCityIds(context);
            if (cityIds == null) {
                Log.d(TAG, "handleActionLoad: No cities in the db so nothing to load");
                return;
            }
            //get current weather data using group query
            GroupCurrentWeatherDataHelper.getDataForMultipleCityIds(context, cityIds, true);

            //loop through all city ids and get forecast data since forecast API doesn't support group queries
            for (Long cityId : cityIds) {
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
            Log.e(TAG, "handleActionLoad: Unexpected error", e);
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
        long lastFetchMillis = determineLastFetch(context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm Z", Locale.US);
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
        //retrieve the timestamp from the current_weather table
        Cursor cursor = context.getContentResolver().query(CurrentWeatherContract.URI, MAX_TIMESTAMP_PROJECTION, null, null, null);
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

    /**
     *
     * @param cityName
     * @param latitude
     * @param longitude
     * @return
     */
    //TODO: Fix this implementation of findCityId by switching to the other API
    private static long findCityId(@Nullable String cityName, double latitude, double longitude) {
        ContentValues[] valuesArray;
        try {
            URL url = CurrentWeatherDataHelper.buildUrl(latitude, longitude);
            HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            valuesArray = CurrentWeatherDataHelper.buildContentValues(urlConnection);

            if (valuesArray != null && valuesArray.length > 0) {
                ContentValues values = valuesArray[0];
                return values.getAsLong(CurrentWeatherContract.Columns.CITY_ID);
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "findCityId: Unexpected error:", e);
        }
        catch (IOException e) {
            Log.e(TAG, "findCityId: Unexpected error:", e);
        }
        return 0;
    }

    /**
     * Gets all the city ids from the current_weather table.  Guaranteed unique since cityId is the unique key for the table.
     * @param context
     * @return
     */
    @Nullable
    private static Long[] getCityIds (Context context) {
        //retrieve all the city ids from the current_weather table
        Cursor cursor = context.getContentResolver().query(CurrentWeatherContract.URI, CITY_ID_PROJECTION, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            Log.d(TAG, "getCityIds: No cities in the db");
            return null;
        }

        ArrayList<Long> cityIdsList = new ArrayList<>(cursor.getCount());
        try {
            while(cursor.moveToNext()) {
                cityIdsList.add(cursor.getLong(0));
            }
        }
        finally {
            cursor.close();
        }

        Log.d(TAG, "getCityIds: CityIds: " + cityIdsList);
        Long[] cityIdsArray = new Long[cityIdsList.size()];
        return cityIdsList.toArray(cityIdsArray);
    }
}
