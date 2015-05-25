package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.TriHourForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class TriHourForecastDataHelper {
    private static final String TAG = TriHourForecastDataHelper.class.getCanonicalName();

    /**
     * Processes the response from the API into content values for insertion into tri_hour_forecast table.
     * @param urlConnection
     * @return
     * @throws java.io.IOException
     */
    public static ContentValues[] buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        TriHourForecastJsonReader triHourForecastJsonReader = new TriHourForecastJsonReader(
                urlConnection.getInputStream(), //input stream
                FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
        return triHourForecastJsonReader.process();
    }

    /**
     * Inserts data into tri_hour_forecast table.
     * @param context
     * @param valuesArray
     */
    public static void persistData(Context context, @NonNull ContentValues[] valuesArray) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(TriHourForecastContract.URI, valuesArray);
    }

    /**
     * Helper method to purge old data from the tri_hour_forecast table.
     * Purge is needed for the tri hour forecast table because the unique index is based on both city id and forecast time.
     * Because of the combined city id and forecast time index, some rows are never replaced because the time has passed and the service
     * no longer returns forecast for those times.
     *
     * Current weather table: Purge all data that is not a user favorite
     * Daily forecast table:  Purge all data that is earlier than 12:01 AM today.
     * Tri hour forecast table: Purge all data earlier than current time.
     * @param context
     */
    public static void purgeOldData(Context context) {
        Log.d(TAG, "purgeOldData");

        //purge old data from tri hour forecast table
        long currentTimeMillis = System.currentTimeMillis();

        //for debugging purposes only
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String currentTimeString = simpleDateFormat.format(new Date(currentTimeMillis));
        Log.d(TAG, String.format("purgeOldData: Current Time String:%s:", currentTimeString));

        context.getContentResolver().delete(
                TriHourForecastContract.URI,
                BaseWeatherContract.whereClauseLessThan(TriHourForecastContract.Columns.FORECAST_DATETIME),
                BaseWeatherContract.whereArgs(currentTimeMillis));
    }
}
