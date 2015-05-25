package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class TriHourForecastDataHelper {
    private static final String TAG = TriHourForecastDataHelper.class.getCanonicalName();

    /**
     * Inserts data into tri_hour_forecast table.
     * @param context
     * @param valuesArray
     */
    public static void persistData(Context context, @NonNull ContentValues[] valuesArray) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(TriHourForecastContract.URI, valuesArray);
    }
}
