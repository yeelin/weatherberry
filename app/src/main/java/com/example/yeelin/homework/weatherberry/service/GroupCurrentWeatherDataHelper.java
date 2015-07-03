package com.example.yeelin.homework.weatherberry.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.json.GroupCurrentWeatherJsonReader;
import com.example.yeelin.homework.weatherberry.networkUtils.FetchDataUtils;
import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ninjakiki on 5/25/15.
 */
public class GroupCurrentWeatherDataHelper {
    private static final String TAG = GroupCurrentWeatherDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_GROUP_CURRENT_WEATHER = "group";
    private static final String QUERY_CITY_ID = "id";
    private static final String QUERY_CITY_ID_SEPARATOR = ",";

    //projection
    private static final String[] PROJECTION_CITY_ID_AND_FAVORITES = new String[]{
            CurrentWeatherContract.Columns.CITY_ID,
            CurrentWeatherContract.Columns.USER_FAVORITE
    };

    //enums
    private enum CityIdAndFavoritesCursorPosition {
        CITY_ID_POS(0),
        USER_FAV_POS(1);
        private int value;
        CityIdAndFavoritesCursorPosition(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    /**
     * Gets all the city ids and favorites from the current_weather table.
     * Note: A city id may map to two favorite values (yes and no) hence the long to arraylist hashmap.
     *
     * @param context
     * @return
     */
    @Nullable
    public static HashMap<Long, ArrayList<Integer>> getCityIdsAndFavorites(Context context) {
        //retrieve all the city ids and favorites from the current_weather table
        Cursor cursor = context.getContentResolver().query(
                CurrentWeatherContract.URI,
                PROJECTION_CITY_ID_AND_FAVORITES,
                null,
                null,
                CurrentWeatherContract.Columns.USER_FAVORITE + " asc, " + CurrentWeatherContract.Columns.CITY_NAME + " asc");

        if (cursor == null) {
            Log.d(TAG, "getCityIdsAndFavorites: Cursor is null");
            return null;
        }
        if (cursor.getCount() == 0) {
            Log.d(TAG, "getCityIdsAndFavorites: 0 cities in the db");
            cursor.close();
            return null;
        }

        //create a map of city ids to favorites
        HashMap<Long, ArrayList<Integer>> cityIdsToFavoritesMap = new HashMap<>(cursor.getCount());
        try {
            while (cursor.moveToNext()) {
                long cityId = cursor.getLong(CityIdAndFavoritesCursorPosition.CITY_ID_POS.getValue());
                int userFavorite = cursor.getInt(CityIdAndFavoritesCursorPosition.USER_FAV_POS.getValue());

                if (cityIdsToFavoritesMap.containsKey(cityId)) {
                    //map already contains the city id, so just add to the favorites arraylist
                    ArrayList<Integer> userFavoritesArrayList = cityIdsToFavoritesMap.get(cityId);
                    userFavoritesArrayList.add(userFavorite);
                }
                else {
                    //map doesn't contain the city id, so allocate a new arraylist and add to it
                    ArrayList<Integer> userFavoritesArrayList = new ArrayList<>(2);
                    userFavoritesArrayList.add(userFavorite);
                    cityIdsToFavoritesMap.put(cityId, userFavoritesArrayList);
                }
            }
        }
        finally {
            cursor.close();
        }
        Log.d(TAG, "getCityIdsAndFavorites:" + cityIdsToFavoritesMap);
        return cityIdsToFavoritesMap;
    }

    /**
     * Retrieves multi-city data from the API by first building the url, calling the API, and then
     * processing the response into content values, and persisting them.
     * @param context
     * @param cityIds
     * @param cityIdsToFavoritesMap
     * @return
     */
    @Nullable
    public static ArrayList<ContentValues> getDataForMultipleCityIds(Context context,
                                                                     Long[] cityIds,
                                                                     HashMap<Long, ArrayList<Integer>> cityIdsToFavoritesMap) {
        Log.d(TAG, "getDataForMultipleCityIds:" + Arrays.toString(cityIds));
        ArrayList<ContentValues> valuesArrayList = null;
        try {
            final URL url = buildUrl(cityIds);
            final HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            if (urlConnection == null) {
                return null;
            }

            valuesArrayList = buildContentValues(urlConnection);
            if (valuesArrayList != null && valuesArrayList.size() > 0) {
                augmentData(valuesArrayList, cityIdsToFavoritesMap);
                persistData(context, valuesArrayList);
            }
        }
        catch (MalformedURLException e) {
            Log.d(TAG, "getDataForMultipleCityIds: Unexpected MalformedURLException:", e);
        }
        catch (IOException e) {
            Log.d(TAG, "getDataForMultipleCityIds: Unexpected IOException:", e);
        }
        return valuesArrayList;
    }

    /**
     * Not implemented since this class is all about getting data for multiple cities.
     * @param context
     * @param cityId
     * @param userFavorite
     * @return
     */
    @Nullable
    public static ArrayList<ContentValues> getDataForCityId(Context context, long cityId, boolean userFavorite) {
        return null;
    }

    /**
     * Builds a url for querying the group api by multiple city ids
     * Format: Group current weather by City Id:
     * http://api.openweathermap.org/data/2.5/group?id=5809844,5786882&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param cityIds
     * @return
     * @throws java.net.MalformedURLException
     */
    @NonNull
    public static URL buildUrl(@NonNull Long[] cityIds) throws MalformedURLException {
        Log.d(TAG, "buildUrl: CityIds:" + Arrays.toString(cityIds));

        //header
        Uri.Builder uriBuilder = FetchDataUtils.getHeaderForUriBuilder();

        //middle
        appendMiddleToUriBuilder(uriBuilder, cityIds);

        //footer
        uriBuilder = FetchDataUtils.appendFooterToUriBuilder(uriBuilder);

        //convert uri builder into a URL
        return FetchDataUtils.buildUrl(uriBuilder);
    }

    /**
     * Appends the middle part to the given uri builder and returns it
     * @param uriBuilder
     * @param cityIds
     * @return
     */
    @NonNull
    private static Uri.Builder appendMiddleToUriBuilder(@NonNull Uri.Builder uriBuilder, @NonNull Long[] cityIds) {
        uriBuilder.appendPath(PATH_GROUP_CURRENT_WEATHER);

        //build the list of city ids separated by commas
        StringBuilder cityIdsBuilder = new StringBuilder();
        for (int i=0; i<cityIds.length; i++) {
            cityIdsBuilder.append(Long.toString(cityIds[i]));
            if (i != cityIds.length-1)
                cityIdsBuilder.append(QUERY_CITY_ID_SEPARATOR);
        }
        uriBuilder.appendQueryParameter(QUERY_CITY_ID, cityIdsBuilder.toString());
        return uriBuilder;
    }

    /**
     * Processes the multi city response from the group API into content values for insertion into current_weather table.
     * @param urlConnection
     * @return
     * @throws java.io.IOException
     */
   public static ArrayList<ContentValues> buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        try {
            GroupCurrentWeatherJsonReader groupCurrentWeatherJsonReader = new GroupCurrentWeatherJsonReader(
                    urlConnection.getInputStream(), //input stream
                    FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
            return groupCurrentWeatherJsonReader.process();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Augments multi-city data before inserting into the current_weather table.
     * @param valuesArrayList
     * @param cityIdsToFavoritesMap
     */
    public static void augmentData(@NonNull ArrayList<ContentValues> valuesArrayList,
                                   HashMap<Long, ArrayList<Integer>> cityIdsToFavoritesMap) {
        Log.d(TAG, "augmentData: cityIdsToFavoritesMap:" + cityIdsToFavoritesMap);
        ContentValues duplicateValues = null;

        for (ContentValues values : valuesArrayList) {
            //add user_favorite value
            final long cityId = values.getAsLong(CurrentWeatherContract.Columns.CITY_ID);

            ArrayList<Integer> userFavoritesArrayList = cityIdsToFavoritesMap.get(cityId);
            int userFavorite = userFavoritesArrayList.remove(0);
            values.put(CurrentWeatherContract.Columns.USER_FAVORITE,
                    (userFavorite == 1) ? CurrentWeatherContract.USER_FAVORITE_YES : CurrentWeatherContract.USER_FAVORITE_NO);

            //add unit as imperial
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);

            //add current timestamp as the db insertion timestamp by overwriting feed timestamp
            final long currentTimeMillis = new Date().getTime();
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, currentTimeMillis);

            //if userFavoritesArrayList.size is greater than 0, it means that we have a case where a city is both
            //a current location and a favorite. Duplicate the row with a different userFavorite value.
            if (userFavoritesArrayList.size() > 0) {
                duplicateValues = new ContentValues(values);
                userFavorite = userFavoritesArrayList.remove(0);
                duplicateValues.put(CurrentWeatherContract.Columns.USER_FAVORITE,
                        (userFavorite == 1) ? CurrentWeatherContract.USER_FAVORITE_YES : CurrentWeatherContract.USER_FAVORITE_NO);
            }
        }

        //add the duplicated row if it's not null to the valuesArrayList
        if (duplicateValues != null) {
            valuesArrayList.add(duplicateValues);
        }
    }

    /**
     * Augments single city data before inserting into the current_weather table. Not implemented
     * @param valuesArrayList
     * @param userFavorite
     */
    public static void augmentData(@NonNull ArrayList<ContentValues> valuesArrayList,
                                   boolean userFavorite) {
        Log.d(TAG, "augmentData: Not implemented");
    }

    /**
     * Inserts data into current_weather table.
     * @param context
     * @param valuesArrayList
     */
    public static void persistData(Context context, @NonNull ArrayList<ContentValues> valuesArrayList) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(
                CurrentWeatherContract.URI,
                valuesArrayList.toArray(new ContentValues[valuesArrayList.size()]));
    }
}
