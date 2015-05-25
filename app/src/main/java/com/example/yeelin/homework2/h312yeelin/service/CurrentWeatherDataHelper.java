package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class CurrentWeatherDataHelper {
    private static final String TAG = CurrentWeatherDataHelper.class.getCanonicalName();

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
