package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.GroupCurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 5/25/15.
 */
public class GroupCurrentWeatherDataHelper {
    private static final String TAG = GroupCurrentWeatherDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_GROUP_CURRENT_WEATHER = "group";
    private static final String QUERY_CITY_ID = "id";
    private static final String QUERY_CITY_ID_SEPARATOR = ",";


    /**
     * Retrieves multi-city data from the API by first building the url, calling the API, and then
     * processing the response into content values, and persisting them.
     * @param context
     * @param cityIds
     * @param userFavorite
     * @return
     */
    @Nullable
    public static ContentValues[] getDataForMultipleCityIds(Context context,
                                                             Long[] cityIds,
                                                             boolean userFavorite) {
        Log.d(TAG, "getDataForMultipleCityIds:" + cityIds);
        ContentValues[] valuesArray = null;
        try {
            URL url = buildUrl(cityIds);
            HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            if (urlConnection == null) {
                return null;
            }

            valuesArray = buildContentValues(urlConnection);
            if (valuesArray != null && valuesArray.length > 0) {
                augmentData(valuesArray, userFavorite);
                persistData(context, valuesArray);
            }
        }
        catch (MalformedURLException e) {
            Log.d(TAG, "getDataForMultipleCityIds: Unexpected error:", e);
        }
        catch (IOException e) {
            Log.d(TAG, "getDataForMultipleCityIds: Unexpected error:", e);
        }
        return valuesArray;
    }

    /**
     * Not implemented since this class is all about getting data for multiple cities.
     * @param context
     * @param cityId
     * @param userFavorite
     * @return
     */
    @Nullable
    public static ContentValues[] getDataForCityId(Context context, long cityId, boolean userFavorite) {
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
        Log.d(TAG, "buildUrl: CityIds:" + cityIds);

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
    public static ContentValues[] buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
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
