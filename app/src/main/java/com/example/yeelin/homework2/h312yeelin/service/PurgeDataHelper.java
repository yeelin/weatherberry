package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

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

        //purge data from current weather table
//        for (long cityId : cityIds) {
//            context.getContentResolver().delete(
//                    CurrentWeatherContract.URI,
//                    BaseWeatherContract.whereClauseEquals(
//                            CurrentWeatherContract.Columns.CITY_ID,
//                            CurrentWeatherContract.Columns.USER_FAVORITE),
//                    BaseWeatherContract.whereArgs(
//                            cityId,
//                            CurrentWeatherContract.USER_FAVORITE_YES));
//        }

        ArrayList<ContentProviderOperation> operations = new ArrayList<>(cityIdsArrayList.size());
        for (long cityId : cityIdsArrayList) {

            String selection = BaseWeatherContract.whereClauseEquals(
                    CurrentWeatherContract.Columns.CITY_ID,
                    CurrentWeatherContract.Columns.USER_FAVORITE);
            String[] selectionArgs = BaseWeatherContract.whereArgs(
                    cityId,
                    CurrentWeatherContract.USER_FAVORITE_YES);

            ContentProviderOperation op = ContentProviderOperation.newDelete(CurrentWeatherContract.URI)
                    .withSelection(selection, selectionArgs)
                    .build();
            operations.add(op);
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
