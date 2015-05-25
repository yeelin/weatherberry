package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.CurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class CurrentWeatherDataHelper {
    private static final String TAG = CurrentWeatherDataHelper.class.getCanonicalName();

    /**
     * Processes the response from the API into content values for insertion into current_weather table.
     * @param urlConnection
     * @return
     * @throws IOException
     */
    public static ContentValues[] buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        CurrentWeatherJsonReader currentWeatherJsonReader = new CurrentWeatherJsonReader(
                urlConnection.getInputStream(), //input stream
                FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
        return currentWeatherJsonReader.process();
    }

    /**
     * Augments data before inserting into the current_weather table.
     * @param valuesArray
     * @param userFavorite
     */
    public static void augmentData(@NonNull ContentValues[] valuesArray,
                                    boolean userFavorite) {
        Log.d(TAG, "augmentData");

        //add unit and current timestamp
        for (ContentValues values : valuesArray) {
            //TODO: have to fix this when we have current location
            //add user_favorite value
            values.put(CurrentWeatherContract.Columns.USER_FAVORITE,
                    userFavorite ? CurrentWeatherContract.USER_FAVORITE_YES : CurrentWeatherContract.USER_FAVORITE_NO);

            //TODO: add setting for user to choose unit type
            //add unit as imperial
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);

            //inspect feed timestamp
            //note: we are only inspecting. not using this as the insertion timestamp.
            long feedTimeMillis = values.getAsLong(CurrentWeatherContract.Columns.TIMESTAMP);
            long currentTimeMillis = new Date().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ", Locale.US);
            Log.d(TAG, String.format("Feed timestamp:" + feedTimeMillis + " Formatted:" + formatter.format(new Date(feedTimeMillis))));
            Log.d(TAG, String.format("Curr timestamp:" + currentTimeMillis + " Formatted:" + formatter.format(new Date(currentTimeMillis))));

            //add current timestamp as the insertion timestamp by overwriting feed timestamp
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, currentTimeMillis);
        }
    }

    /**
     * Inserts data into current_weather table.
     * @param context
     * @param valuesArray
     */
    public static void persistData(Context context, @NonNull ContentValues[] valuesArray) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(CurrentWeatherContract.URI, valuesArray);
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
                BaseWeatherContract.whereArgs(CurrentWeatherContract.USER_FAVORITE_NO));
    }
}
