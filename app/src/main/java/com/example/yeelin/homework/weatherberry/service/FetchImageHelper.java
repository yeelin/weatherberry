package com.example.yeelin.homework.weatherberry.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.DailyForecastContract;
import com.example.yeelin.homework.weatherberry.provider.TriHourForecastContract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class FetchImageHelper {
    private static final String TAG = FetchImageHelper.class.getCanonicalName();
    private static final String[] PROJECTION_ICON = new String[]{BaseWeatherContract.Columns.ICON};

    /**
     * Gets unique icon names from all the tables.
     * TODO: I don't like how I'm using a hashmap to get the unique names.  Need to find a way to return distinct rows.
     *
     * @param context
     * @return
     */
    @NonNull
    public static Collection<String> getUniqueIconNames(Context context) {
        HashMap<String, String> iconNameMap = new HashMap<>();

        //retrieve all icons from all the tables
        Cursor currentWeatherIconCursor = context.getContentResolver().query(CurrentWeatherContract.URI, PROJECTION_ICON, null, null, null);
        Cursor dailyForecastIconCursor = context.getContentResolver().query(DailyForecastContract.URI, PROJECTION_ICON, null, null, null);
        Cursor triHourForecastIconCursor = context.getContentResolver().query(TriHourForecastContract.URI, PROJECTION_ICON, null, null, null);

        getUniqueIconNamesHelper(currentWeatherIconCursor, iconNameMap);
        getUniqueIconNamesHelper(dailyForecastIconCursor, iconNameMap);
        getUniqueIconNamesHelper(triHourForecastIconCursor, iconNameMap);

        return iconNameMap.values();
    }

    /**
     * Given a cursor, it puts all the icon names into the hashmap
     * @param cursor
     * @param iconNameMap
     */
    private static void getUniqueIconNamesHelper(@Nullable Cursor cursor, @NonNull HashMap<String, String> iconNameMap) {
        if (cursor == null) return;
        try {
            while (cursor.moveToNext()) {
                String iconName = cursor.getString(0);
                iconNameMap.put(iconName, iconName);
            }
        } finally {
            cursor.close();
        }
    }

    /**
     * Get unique icon names from all the tables for a particular cityId.
     * @param context
     * @param cityId
     * @return
     */
    public static Collection<String> getUniqueIconNames(Context context, long cityId, boolean userFavorite) {
        HashMap<String, String> iconNameMap = new HashMap<>();

        //create uri
        Uri currentWeatherUri = BaseWeatherContract.buildUri(CurrentWeatherContract.URI, cityId, BaseWeatherContract.IdType.CITY_ID);
        Uri dailyForecastUri = BaseWeatherContract.buildUri(DailyForecastContract.URI, cityId, BaseWeatherContract.IdType.CITY_ID);
        Uri triHourForecastUri = BaseWeatherContract.buildUri(TriHourForecastContract.URI, cityId, BaseWeatherContract.IdType.CITY_ID);

        //create selection and selection args
        String selection = BaseWeatherContract.whereClauseEquals(BaseWeatherContract.Columns.USER_FAVORITE);
        String[] selectionArgs = BaseWeatherContract.whereArgs(userFavorite ? BaseWeatherContract.USER_FAVORITE_YES : BaseWeatherContract.USER_FAVORITE_NO);

        //retrieve all icons from all the tables
        Cursor currentWeatherIconCursor = context.getContentResolver().query(currentWeatherUri, PROJECTION_ICON, selection, selectionArgs, null);
        Cursor dailyForecastIconCursor = context.getContentResolver().query(dailyForecastUri, PROJECTION_ICON, selection, selectionArgs, null);
        Cursor triHourForecastIconCursor = context.getContentResolver().query(triHourForecastUri, PROJECTION_ICON, selection, selectionArgs, null);

        getUniqueIconNamesHelper(currentWeatherIconCursor, iconNameMap);
        getUniqueIconNamesHelper(dailyForecastIconCursor, iconNameMap);
        getUniqueIconNamesHelper(triHourForecastIconCursor, iconNameMap);

        return iconNameMap.values();
    }


    /**
     * Loops over all the values inserted into the database and retrieves the unique icons
     * that need to be fetched.
     * @deprecated
     * @param currentWeatherValues
     * @param dailyForecastValues
     * @param triHourForecastValues
     * @return
     */
    @NonNull
    public static Collection<String> getUniqueIconNames(@Nullable ArrayList<ContentValues> currentWeatherValues,
                                                        @Nullable ArrayList<ContentValues> dailyForecastValues,
                                                        @Nullable ArrayList<ContentValues> triHourForecastValues) {
        HashMap<String, String> iconNameMap = new HashMap<>();

        //get unique icon names
        if (currentWeatherValues != null) {
            for (ContentValues values : currentWeatherValues) {
                String iconName = (String) values.get(CurrentWeatherContract.Columns.ICON);
                if (!iconNameMap.containsKey(iconName)) {
                    //Log.d(TAG, "Adding to iconmap:" + iconName);
                    iconNameMap.put(iconName, iconName);
                }
            }
        }

        if (dailyForecastValues != null) {
            for (ContentValues values : dailyForecastValues) {
                String iconName = (String) values.get(DailyForecastContract.Columns.ICON);
                if (!iconNameMap.containsKey(iconName)) {
                    //Log.d(TAG, "Adding to iconmap:" + iconName);
                    iconNameMap.put(iconName, iconName);
                }
            }
        }

        if (triHourForecastValues != null) {
            for (ContentValues values : triHourForecastValues) {
                String iconName = (String) values.get(TriHourForecastContract.Columns.ICON);
                if (!iconNameMap.containsKey(iconName)) {
                    //Log.d(TAG, "Adding to iconmap:" + iconName);
                    iconNameMap.put(iconName, iconName);
                }
            }
        }

        //Log.d(TAG, String.format("getUniqueIconNamesToFetch: Count:%d, Contents:%s", iconNameMap.size(), iconNameMap.toString()));
        return iconNameMap.values();
    }
}
