package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/26/15.
 */
public class FindCityDataHelper {
    private static final String TAG = FindCityDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_FIND = "find";
    private static final String QUERY_CITY_LATITUDE = "lat";
    private static final String QUERY_CITY_LONGITUDE = "lon";
    private static final String QUERY_COUNT = "cnt";
    private static final int QUERY_CITY_COUNT = 10;

    /**
     * Uses the given lat/long to query open weather's find api.  The cityName is used to match against the
     * city name in the response to get a more accurate city.
     * @param cityName
     * @param latitude
     * @param longitude
     * @return cityId
     */
    public static long findCityId(@Nullable String cityName, double latitude, double longitude) {
        try {
            final URL url = buildUrl(latitude, longitude);
            final HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            final ArrayList<ContentValues> valuesArrayList = buildContentValues(urlConnection);

            if (valuesArrayList != null && valuesArrayList.size() > 0) {
                //check each ContentValues map to see if there's a match for city name
                for (ContentValues values : valuesArrayList) {
                    final String candidateCityName = values.getAsString(CurrentWeatherContract.Columns.CITY_NAME);
                    if (cityName.equalsIgnoreCase(candidateCityName)) {
                        //we found a match
                        Log.d(TAG, "findCityId: We found a match for cityName:" + candidateCityName);
                        return values.getAsLong(CurrentWeatherContract.Columns.CITY_ID);
                    }
                }
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "findCityId: Unexpected error:", e);
        }
        catch (IOException e) {
            Log.e(TAG, "findCityId: Unexpected error:", e);
        }
        Log.d(TAG, String.format("findCityId: Could not find cityId for cityName:%s (%f, %f)", cityName, latitude, longitude));
        return BaseWeatherContract.NO_ID;
    }

    /**
     * Builds a url for querying the find api by lat/long.
     * Format: Find by Lat Long:
     * http://api.openweathermap.org/data/2.5/find?lat=47.8600971&lon=-122.2042966&cnt=20&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     * @param latitude
     * @param longitude
     * @return
     * @throws java.net.MalformedURLException
     */
    @NonNull
    public static URL buildUrl(double latitude, double longitude) throws MalformedURLException {
        Log.d(TAG, "buildUrl: LatLong: " + latitude + ", " + longitude);

        //header
        Uri.Builder uriBuilder = FetchDataUtils.getHeaderForUriBuilder();

        //middle
        appendMiddleToUriBuilder(uriBuilder, latitude, longitude);

        //footer
        uriBuilder = FetchDataUtils.appendFooterToUriBuilder(uriBuilder);

        //convert uri builder into a URL
        return FetchDataUtils.buildUrl(uriBuilder);
    }

    /**
     * Appends the middle part to the given uri builder and returns it
     * @param uriBuilder
     * @param latitude
     * @param longitude
     * @return
     */
    @NonNull
    private static Uri.Builder appendMiddleToUriBuilder(@NonNull Uri.Builder uriBuilder, double latitude, double longitude) {
        uriBuilder.appendPath(PATH_FIND)
                .appendQueryParameter(QUERY_CITY_LATITUDE, Double.toString(latitude))
                .appendQueryParameter(QUERY_CITY_LONGITUDE, Double.toString(longitude))
                .appendQueryParameter(QUERY_COUNT, Integer.toString(QUERY_CITY_COUNT));
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
        return GroupCurrentWeatherDataHelper.buildContentValues(urlConnection);
    }
}

