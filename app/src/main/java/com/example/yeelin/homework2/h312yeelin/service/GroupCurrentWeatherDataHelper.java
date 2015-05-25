package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.GroupCurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 5/25/15.
 */
public class GroupCurrentWeatherDataHelper {
    private static final String TAG = GroupCurrentWeatherDataHelper.class.getCanonicalName();

    /**
     * Processes the multi city response from the group API into content values for insertion into current_weather table.
     * @param urlConnection
     * @return
     * @throws java.io.IOException
     */
    public static ContentValues[] buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        GroupCurrentWeatherJsonReader groupCurrentWeatherJsonReader = new GroupCurrentWeatherJsonReader(
                urlConnection.getInputStream(), //input stream
                FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
        return groupCurrentWeatherJsonReader.process();
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
}
