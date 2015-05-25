package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.DailyForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class DailyForecastDataHelper {
    private static final String TAG = DailyForecastDataHelper.class.getCanonicalName();

    /**
     * Processes the response from the API into content values for insertion into daily_forecast table.
     * @param stream
     * @param encoding
     * @return
     * @throws IOException
     */
    public static ContentValues[] buildContentValues(InputStream stream,
                                                     String encoding) throws IOException {
        Log.d(TAG, "buildContentValues");
        DailyForecastJsonReader dailyForecastJsonReader = new DailyForecastJsonReader(stream, encoding);
        return dailyForecastJsonReader.process();
    }

    /**
     * Inserts data into the daily_forecast table.
     * @param context
     * @param valuesArray
     */
    public static void persistData(Context context, @NonNull ContentValues[] valuesArray) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(DailyForecastContract.URI, valuesArray);
    }

    /**
     * Helper method to purge old data from daily_forecast table
     * Purge is needed for the daily forecast table because the unique index is based on both city id and forecast time.
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

        //calculate what is 12:01 AM today
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        long purgeTimeMillis = calendar.getTimeInMillis();

        //for debugging purposes only
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String purgeTimeString = simpleDateFormat.format(new Date(purgeTimeMillis));
        Log.d(TAG, String.format("purgeOldData: Purge Time String:%s:", purgeTimeString));

        //purge old data from daily forecast table
        context.getContentResolver().delete(
                DailyForecastContract.URI,
                BaseWeatherContract.whereClauseLessThan(DailyForecastContract.Columns.FORECAST_DATETIME),
                BaseWeatherContract.whereArgs(purgeTimeMillis));
    }
}
