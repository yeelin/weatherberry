package com.example.yeelin.homework.weatherberry.service;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.DailyForecastContract;
import com.example.yeelin.homework.weatherberry.provider.TriHourForecastContract;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/27/15.
 */
public class PurgeDataHelper {
    private static final String TAG = PurgeDataHelper.class.getCanonicalName();

    /**
     * Helper method that handles action to purge data for favorite cities that
     * have been unfavorited on a background thread.  This method is called from a bg thread.
     * Called from: Network intent service onHandleIntent().
     *
     * @param context
     * @param cityIdsArrayList
     */
    public static void handleActionFavoriteCityPurge(Context context,
                                                     @Nullable ArrayList<Long> cityIdsArrayList) {
        Log.d(TAG, "handleActionFavoriteCityPurge:" + cityIdsArrayList);

        if (cityIdsArrayList == null || cityIdsArrayList.size() == 0) return;

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(cityIdsArrayList.size());
        for (long cityId : cityIdsArrayList) {

            //delete rows that match on cityId and userFavorite == YES
            String selection = BaseWeatherContract.whereClauseEquals(
                    BaseWeatherContract.Columns.CITY_ID,
                    BaseWeatherContract.Columns.USER_FAVORITE);
            String[] selectionArgs = BaseWeatherContract.whereArgs(
                    cityId,
                    BaseWeatherContract.USER_FAVORITE_YES);

            //delete from 3 tables: current_weather, daily_forecast, tri_hour_forecast
            ContentProviderOperation currentWeatherPurgeOp = ContentProviderOperation.newDelete(CurrentWeatherContract.URI)
                    .withSelection(selection, selectionArgs)
                    .build();
            ContentProviderOperation dailyForecastPurgeOp = ContentProviderOperation.newDelete(DailyForecastContract.URI)
                    .withSelection(selection, selectionArgs)
                    .build();
            ContentProviderOperation triHourForecastPurgeOp = ContentProviderOperation.newDelete(TriHourForecastContract.URI)
                    .withSelection(selection, selectionArgs)
                    .build();

            operations.add(currentWeatherPurgeOp);
            operations.add(dailyForecastPurgeOp);
            operations.add(triHourForecastPurgeOp);
        }

        try {
            context.getContentResolver().applyBatch(BaseWeatherContract.AUTHORITY, operations);
            Log.d(TAG, "handleActionFavoriteCityPurge: Done");
        }
        catch (RemoteException e) {
            Log.e(TAG, "handleActionFavoriteCityPurge: Unexpected RemoteException: ", e);
        }
        catch (OperationApplicationException e) {
            Log.e(TAG, "handleActionFavoriteCityPurge: Unexpected OperationApplicationException: ", e);
        }
    }
}
