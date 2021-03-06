package com.example.yeelin.homework.weatherberry.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.json.CurrentWeatherJsonReader;
import com.example.yeelin.homework.weatherberry.networkUtils.FetchDataUtils;
import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class CurrentWeatherDataHelper {
    private static final String TAG = CurrentWeatherDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_CURRENT_WEATHER = "weather";
    private static final String QUERY_CITY_ID = "id";
    private static final String QUERY_CITY_LATITUDE = "lat";
    private static final String QUERY_CITY_LONGITUDE = "lon";

    /**
     * Retrieves data from the API by first building the url, calling the API, and then
     * processing the response into content values, and persisting them.
     * @param context
     * @param cityId
     * @param userFavorite
     * @return
     */
    @Nullable
    public static boolean getDataForCityId(Context context,
                                                            long cityId,
                                                            boolean userFavorite) {
        Log.d(TAG, "getDataForCityId:" + cityId);
        ArrayList<ContentValues> valuesArrayList = null;
        try {
            final URL url = buildUrl(cityId);
            final HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            if (urlConnection == null) {
                return false;
            }

            valuesArrayList = buildContentValues(urlConnection);
            if (valuesArrayList != null && valuesArrayList.size() > 0) {
                augmentData(valuesArrayList, userFavorite);
                if (userFavorite) {
                    //this is a user favorite, so use insert with replace on conflict
                    persistData(context, valuesArrayList);
                }
                else {
                    //this is current location, so use update except for the first time where we'll use insert
                    updateData(context, valuesArrayList);
                }
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "getDataForCityId: Unexpected MalformedURLException:", e);
            return false;
        }
        catch (IOException e) {
            Log.e(TAG, "getDataForCityId: Unexpected IOException:", e);
            return false;
        }

        return true;
    }

    /**
     * Builds a url for querying the weather api by city id
     * Format: Current weather by City Id:
     * http://api.openweathermap.org/data/2.5/weather?id=5128638&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param cityId
     * @return
     * @throws MalformedURLException
     */
    @NonNull
    public static URL buildUrl(long cityId) throws MalformedURLException {
        Log.d(TAG, "buildUrl: CityId:" + cityId);

        //header
        Uri.Builder uriBuilder = FetchDataUtils.getHeaderForUriBuilder();

        //middle
        appendMiddleToUriBuilder(uriBuilder, cityId);

        //footer
        uriBuilder = FetchDataUtils.appendFooterToUriBuilder(uriBuilder);

        //convert uri builder into a URL
        return FetchDataUtils.buildUrl(uriBuilder);
    }

    /**
     * Builds a url for querying the weather api by lat/long.
     * Format: Current weather by Lat Long:
     * http://api.openweathermap.org/data/2.5/weather?lat=47.610377&lon=-122.2006786&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param latitude
     * @param longitude
     * @return
     * @throws MalformedURLException
     */
    @NonNull
    public static URL buildUrl(double latitude, double longitude) throws MalformedURLException {
        Log.d(TAG, "buildUrl: LatLong: " + latitude + ", " + longitude);

        //header
        Uri.Builder uriBuilder = FetchDataUtils.getHeaderForUriBuilder();

        //middle
        appendMiddleToUriBuilder(uriBuilder, latitude, longitude);

        //footer
        uriBuilder = FetchDataUtils.appendFooterToUriBuilder(uriBuilder);

        //convert uri builder into a URL
        return FetchDataUtils.buildUrl(uriBuilder);
    }

    /**
     * Appends the middle part to the given uri builder and returns it
     * @param uriBuilder
     * @param cityId
     * @return
     */
    @NonNull
    private static Uri.Builder appendMiddleToUriBuilder(@NonNull Uri.Builder uriBuilder, long cityId) {
        uriBuilder.appendPath(PATH_CURRENT_WEATHER)
                .appendQueryParameter(QUERY_CITY_ID, Long.toString(cityId));
        return uriBuilder;
    }

    /**
     * Appends the middle part to the given uri builder and returns it
     * @param uriBuilder
     * @param latitude
     * @param longitude
     * @return
     */
    @NonNull
    private static Uri.Builder appendMiddleToUriBuilder(@NonNull Uri.Builder uriBuilder, double latitude, double longitude) {
        uriBuilder.appendPath(PATH_CURRENT_WEATHER)
                .appendQueryParameter(QUERY_CITY_LATITUDE, Double.toString(latitude))
                .appendQueryParameter(QUERY_CITY_LONGITUDE, Double.toString(longitude));
        return uriBuilder;
    }

    /**
     * Processes the response from the API into content values for insertion into current_weather table.
     * @param urlConnection
     * @return
     * @throws IOException
     */
    public static ArrayList<ContentValues> buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        try {
            CurrentWeatherJsonReader currentWeatherJsonReader = new CurrentWeatherJsonReader(
                    urlConnection.getInputStream(), //input stream
                    FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
            return currentWeatherJsonReader.process();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Augments data before inserting into the current_weather table.
     * @param valuesArrayList
     * @param userFavorite
     */
    public static void augmentData(@NonNull ArrayList<ContentValues> valuesArrayList,
                                    boolean userFavorite) {
        Log.d(TAG, "augmentData: userFavorite:" + userFavorite);
        for (ContentValues values : valuesArrayList) {
            //add user_favorite value
            values.put(CurrentWeatherContract.Columns.USER_FAVORITE,
                        userFavorite ? BaseWeatherContract.USER_FAVORITE_YES : BaseWeatherContract.USER_FAVORITE_NO);

            //add unit as imperial
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);

            //add current timestamp as the db insertion timestamp by overwriting feed timestamp
            long currentTimeMillis = new Date().getTime();
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, currentTimeMillis);
        }
    }

    /**
     * Inserts data into current_weather table.
     * @param context
     * @param valuesArrayList
     */
    public static void persistData(Context context, @NonNull ArrayList<ContentValues> valuesArrayList) {
        Log.d(TAG, "persistData");

        //warn if size is > 1
        if (valuesArrayList.size() > 1) {
            Log.w(TAG, "persistData: There should not be more than 1 content values object");
        }

//        context.getContentResolver().bulkInsert(
//                CurrentWeatherContract.URI,
//                valuesArrayList.toArray(new ContentValues[valuesArrayList.size()]));
        context.getContentResolver().insert(CurrentWeatherContract.URI, valuesArrayList.get(0));
    }

    /**
     * Updates data in current_weather table.
     * @param context
     * @param valuesArrayList
     */
    public static void updateData(Context context, @NonNull ArrayList<ContentValues> valuesArrayList) {
        Log.d(TAG, "updateData");

        //warn if size is > 1
        if (valuesArrayList.size() > 1) {
            Log.w(TAG, "updateData: There should not be more than 1 content values object");
        }

        //update non-user favorite from current weather table
        int rowsUpdated = context.getContentResolver().update(
                CurrentWeatherContract.URI,
                valuesArrayList.get(0),
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.USER_FAVORITE),
                BaseWeatherContract.whereArgs(BaseWeatherContract.USER_FAVORITE_NO));

        if (rowsUpdated == 0) {
            Log.d(TAG, "updateData: No rows updated, so trying to insert instead");
            context.getContentResolver().insert(CurrentWeatherContract.URI, valuesArrayList.get(0));
        }
    }

    /**
     * Helper method to purge old data from the current_weather table.
     * Purge is needed for the current_weather table because the unique index is based on both city_id and user_favorite.
     * Because of this combined index, a non-favorite will not be purged when the user changes current location.
     * Current weather table needs to be have non user favorites cleaned out every time a new current location is added.
     *
     * Notes:
     * Current weather table: Purge all data that is not a user favorite
     * Daily forecast table:  Purge all data that is earlier than 12:01 AM today.
     * Tri hour forecast table: Purge all data earlier than current time.
     * @param context
     */
    public static void purgeOldData(Context context) {
        Log.d(TAG, "purgeOldData");

        //purge old non user favorites from current weather table
        context.getContentResolver().delete(
                CurrentWeatherContract.URI,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.USER_FAVORITE),
                BaseWeatherContract.whereArgs(BaseWeatherContract.USER_FAVORITE_NO));
    }
}
