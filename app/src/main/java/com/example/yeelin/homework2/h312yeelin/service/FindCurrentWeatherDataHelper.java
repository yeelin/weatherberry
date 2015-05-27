package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/26/15.
 */
public class FindCurrentWeatherDataHelper {
    private static final String TAG = FindCurrentWeatherDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_FIND = "find";
    private static final String QUERY_CITY_LATITUDE = "lat";
    private static final String QUERY_CITY_LONGITUDE = "lon";
    private static final String QUERY_COUNT = "cnt";
    private static final int QUERY_CITY_COUNT = 10;

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

