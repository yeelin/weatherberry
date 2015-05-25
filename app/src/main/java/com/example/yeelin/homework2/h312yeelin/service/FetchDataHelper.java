package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.json.CurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.json.DailyForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.json.GroupCurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.json.TriHourForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.CacheUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ConnectivityUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.PlayServicesUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 4/30/15.
 */
public class FetchDataHelper {
    //logcat
    private static final String TAG = FetchDataHelper.class.getCanonicalName();

    //minimum interval between fetches
    private static final int TEN_MINUTES_MILLIS = 10 * 60 * 1000;

    //http connection related
    private static final String HTTP_REQUEST_METHOD = "GET";
    private static final int HTTP_CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int HTTP_READ_TIMEOUT_MILLIS = 15000;

    //uri parts
    private static final String SCHEME = "http";
    private static final String AUTHORITY = "api.openweathermap.org";
    private static final String DATA = "data";
    private static final String API_VERSION = "2.5";

    private static final String CURRENT_WEATHER = "weather";
    private static final String GROUP = "group";
    private static final String FORECAST = "forecast";
    private static final String DAILY = "daily";

    private static final String QUERY_CITY_ID = "id";
    private static final String QUERY_CITY_LATITUDE = "lat";
    private static final String QUERY_CITY_LONGITUDE = "lon";
    private static final String QUERY_UNIT = "units";
    private static final String QUERY_COUNT = "cnt";
    private static final String QUERY_APP_ID = "APPID";
    private static final String CITY_ID_SEPARATOR = ",";

    //projections
    private static final String[] MAX_TIMESTAMP_PROJECTION = new String[] {"max(" + CurrentWeatherContract.Columns.TIMESTAMP + ")"};
    private static final String[] CITY_ID_PROJECTION = new String[] { CurrentWeatherContract.Columns.CITY_ID };
    private static final String[] ICON_PROJECTION = new String[] { BaseWeatherContract.Columns.ICON };

    //TODO: remove these hardcoded values
    private static final long SEATTLE_CITY_ID = 5809844; //city id for Seattle
    private static final String UNIT_IMPERIAL = "imperial";
    private static final int DAILY_FORECAST_COUNT = 5;
    private static final String APP_ID = "3284992e5bfef187c44863ce0f31ad30";

    private enum WeatherDataType {
        GROUP_CURRENT_WEATHER,
        CURRENT_WEATHER,
        DAILY_FORECAST,
        TRIHOUR_FORECAST
    }

    private enum WeatherQueryType {
        CITY_ID,
        CITY_LATLNG,
        CITY_MULTI
    }

    /**
     * Convenience inner class that handles latlong of cities
     */
    private static class LatLng {
        final double latitude;
        final double longitude;

        LatLng(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }

    public interface FetchDataHelperCallback {
        public boolean shouldCancelFetch();
    }

    /**
     * Helper method that handles action single load on the background thread.
     * Should be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     *
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     */
    public static void handleActionFavoriteCityLoad(Context context,
                                                    @Nullable String cityName,
                                                    double latitude,
                                                    double longitude,
                                                    boolean userFavorite) {

        Log.d(TAG, "handleActionFavoriteCityLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            long cityId = findCityId(cityName, new LatLng(latitude, longitude));
            if (cityId == 0) {
                Log.d(TAG, "handleActionFavoriteCityLoad: Could not find cityId for: " + cityName);
                return;
            }
            //get data
            //getData(context, WeatherDataType.CURRENT_WEATHER, WeatherQueryType.CITY_LATLNG, null, new LatLng(latitude, longitude));
            ContentValues[] currentWeatherValues = getData(context, WeatherDataType.CURRENT_WEATHER, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, userFavorite);
            ContentValues[] dailyForecastValues = getData(context, WeatherDataType.DAILY_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, userFavorite);
            ContentValues[] triHourForecastValues = getData(context, WeatherDataType.TRIHOUR_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, userFavorite);

            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(currentWeatherValues, dailyForecastValues, triHourForecastValues));
        }
        catch (Exception e) {
            Log.d(TAG, "handleActionFavoriteCityLoad: Unexpected error", e);
        }

        CacheUtils.logCache();
    }

    /**
     *
     * @param cityName
     * @param latLng
     * @return
     */
    //TODO: Fix this implementation of findCityId by switching to the other API
    private static long findCityId(@Nullable String cityName, LatLng latLng) {
        ContentValues[] valuesArray;
        try {
            URL url = buildUrl(WeatherDataType.CURRENT_WEATHER, WeatherQueryType.CITY_LATLNG, null, latLng);
            valuesArray = performGet(url, WeatherDataType.CURRENT_WEATHER);

            if (valuesArray != null) {
                ContentValues values = valuesArray[0];
                return values.getAsLong(CurrentWeatherContract.Columns.CITY_ID);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    /**
     * Helper method that handles action to load data for the current location (user_favorite = false)
     * on the background thread.  This method must be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     * @param context
     * @param cityName
     * @param latitude
     * @param longitude
     */
    public static void handleActionCurrentLocationLoad(Context context,
                                                       @Nullable String cityName,
                                                       double latitude,
                                                       double longitude) {
        Log.d(TAG, "handleActionCurrentLocationLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        try {
            //find city id
            long cityId = findCityId(cityName, new LatLng(latitude, longitude));
            if (cityId == 0) {
                Log.d(TAG, "handleActionCurrentLocationLoad: Could not find cityId for: " + cityName);
                return;
            }

            //delete previous entry for current location
            purgeOldData(context, WeatherDataType.CURRENT_WEATHER);

            //get current weather, daily forecast, and tri hour for city id
            ContentValues[] currentWeatherValues = getData(context, WeatherDataType.CURRENT_WEATHER, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, false);
            ContentValues[] dailyForecastValues = getData(context, WeatherDataType.DAILY_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, false);
            ContentValues[] triHourForecastValues = getData(context, WeatherDataType.TRIHOUR_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, false);

            //get images
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(currentWeatherValues, dailyForecastValues, triHourForecastValues));
        }
        catch (Exception e) {
            Log.d(TAG, "handleActionFavoriteCityLoad: Unexpected error", e);
        }
        CacheUtils.logCache();
    }

    /**
     * Helper method that handles action load on the background thread.
     * Should be called from a background thread.
     *
     * Called from:
     * 1. Network Intent Service: onHandleIntent()
     * 2. Network Job Service: doInBackground()
     */
    public static void handleActionLoad(Context context, FetchDataHelperCallback helperCallback) {
        Log.d(TAG, "handleActionLoad");
        if(!FetchDataUtils.isPreNetworkCheckSuccessful(context)) return;
        //initialize the cache. if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        if (!isDataStale(context)) {
            Log.d(TAG, "handleActionLoad: Data is still fresh");
            return;
        }

        try {
            //check if we should exit early
            if (helperCallback.shouldCancelFetch()) {
                Log.d(TAG, "handleActionLoad: Fetch was cancelled before getData.");
                return;
            }

            //get all the city ids
            Long[] cityIds = getCityIds(context);
            if (cityIds == null) {
                Log.d(TAG, "handleActionLoad: No cities in the db so nothing to load");
                return;
            }
            //get current weather data using group query
            getData(context, WeatherDataType.GROUP_CURRENT_WEATHER, WeatherQueryType.CITY_MULTI, cityIds, null, true);

            //loop through all city ids and get forecast data since forecast API doesn't support group queries
            for (Long cityId : cityIds) {
                //get daily forecast data
                getData(context, WeatherDataType.DAILY_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, true);
                //get tri hour forecast data
                getData(context, WeatherDataType.TRIHOUR_FORECAST, WeatherQueryType.CITY_ID, new Long[] {cityId}, null, true);
            }

            //purge anything that is too old i.e. anything earlier than today at 12:00 AM
            purgeOldData(context, WeatherDataType.DAILY_FORECAST);
            purgeOldData(context, WeatherDataType.TRIHOUR_FORECAST);

            //fetch weather icons to pre-warm the cache
            ImageUtils.getImages(context, FetchImageHelper.getUniqueIconNames(context));
        }
        catch (Exception e) {
            Log.d(TAG, "handleActionLoad: Unexpected error", e);
        }
        CacheUtils.logCache();
    }

    /**
     * Helper method that checks if data is stale
     * @param context
     * @return
     */
    private static boolean isDataStale(Context context) {
        //check last fetch time before fetching again
        long lastFetchMillis = determineLastFetch(context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm Z", Locale.US);
        if (System.currentTimeMillis() - lastFetchMillis < TEN_MINUTES_MILLIS) {
            //last fetch < 10 minutes ago so skip
            Log.d(TAG, String.format("isDataStale: Last fetch was less than 10 minutes ago. Current time:%s, Last fetch:%s",
                    formatter.format(new Date(System.currentTimeMillis())),
                    formatter.format(new Date(lastFetchMillis))));
            return false;
        }

        //last fetch > 10 minutes ago so yes data is stale
        Log.d(TAG, String.format("isDataStale: Last fetch was more than 10 minutes ago. Current time:%s, Last fetch:%s",
                formatter.format(new Date(System.currentTimeMillis())),
                formatter.format(new Date(lastFetchMillis))));
        return true;
    }

    /**
     * Helper method that checks the current_weather table for the timestamp of
     * the last call to the API.
     * @return
     */
    private static long determineLastFetch(Context context) {
        //retrieve the timestamp from the current_weather table
        Cursor cursor = context.getContentResolver().query(CurrentWeatherContract.URI, MAX_TIMESTAMP_PROJECTION, null, null, null);
        long lastFetchMillis = 0;
        try {
            if (cursor.moveToFirst() && !cursor.isNull(0)) {
                lastFetchMillis = cursor.getLong(0);
            }
        }
        finally {
            cursor.close();
        }

        //return the timestamp
        return lastFetchMillis;
    }

    /**
     * Gets all the city ids from the current_weather table.  Guaranteed unique since cityId is the unique key for the table.
     * @param context
     * @return
     */
    @Nullable
    private static Long[] getCityIds (Context context) {
        //retrieve all the city ids from the current_weather table
        Cursor cursor = context.getContentResolver().query(CurrentWeatherContract.URI, CITY_ID_PROJECTION, null, null, null);

        if (cursor == null || cursor.getCount() == 0) {
            Log.d(TAG, "getCityIds: No cities in the db");
            return null;
        }

        ArrayList<Long> cityIdsList = new ArrayList<>(cursor.getCount());
        try {
            while(cursor.moveToNext()) {
                cityIdsList.add(cursor.getLong(0));
            }
        }
        finally {
            cursor.close();
        }

        Log.d(TAG, "getCityIds: CityIds: " + cityIdsList);
        Long[] cityIdsArray = new Long[cityIdsList.size()];
        return cityIdsList.toArray(cityIdsArray);
    }

    /**
     * Retrieves data from the API by first building the url, calling the API, and then
     * processing the response into content values.
     *
     * @param weatherDataType
     * @return
     */
    @Nullable
    private static ContentValues[] getData(Context context,
                                           @NonNull WeatherDataType weatherDataType,
                                           @NonNull WeatherQueryType weatherQueryType,
                                           @Nullable Long[] cityIds,
                                           @Nullable LatLng latLng,
                                           boolean userFavorite) {
        Log.d(TAG, "getData: " + weatherDataType + ", " + weatherQueryType);

        ContentValues[] valuesArray = null;
        //get data
        try {
            URL url = buildUrl(weatherDataType, weatherQueryType, cityIds, latLng);
            valuesArray = performGet(url, weatherDataType);
        } catch (MalformedURLException e) {
            Log.d(TAG, "getData: Unexpected error:", e);
        } catch (IOException e) {
            Log.d(TAG, "getData: Unexpected error:", e);
        }

        if (valuesArray == null || valuesArray.length == 0) {
            Log.d(TAG, "getData: ValuesArray is null or empty so not persisting");
            return null;
        }

        //persist data
        switch (weatherDataType) {
            case GROUP_CURRENT_WEATHER:
            case CURRENT_WEATHER:
                augmentData(weatherDataType, valuesArray, userFavorite);
                CurrentWeatherDataHelper.persistData(context, valuesArray);
                break;

            case DAILY_FORECAST:
                DailyForecastDataHelper.persistData(context, valuesArray);
                break;

            case TRIHOUR_FORECAST:
                TriHourForecastDataHelper.persistData(context, valuesArray);
                break;
        }

        return valuesArray;
    }

    /**
     * Augments the data before inserting into the database
     * @param weatherDataType
     * @param valuesArray
     * @param userFavorite
     */
    private static void augmentData(@NonNull WeatherDataType weatherDataType,
                                    @NonNull ContentValues[] valuesArray,
                                    boolean userFavorite) {
        //Log.d(TAG, "augmentData");
        switch (weatherDataType) {
            case GROUP_CURRENT_WEATHER:
            case CURRENT_WEATHER:
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
                break;

            case DAILY_FORECAST:
                break;

            case TRIHOUR_FORECAST:
                break;
        }
    }

    /**
     * Builds a url for querying the open weather api.
     * Format depends on the weather data type:
     * Current weather:
     * By City Id:      http://api.openweathermap.org/data/2.5/weather?id=5128638&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     * By LatLng:       http://api.openweathermap.org/data/2.5/weather?lat=47.610377&lon=-122.2006786&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     * By Multi cities: http://api.openweathermap.org/data/2.5/group?id=5809844,5786882&units=imperial&cnt=5&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * Daily forecast:
     * http://api.openweathermap.org/data/2.5/forecast/daily?id=5809844&units=imperial&cnt=5&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * Trihour forecast:
     * http://api.openweathermap.org/data/2.5/forecast?id=5809844&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param weatherDataType
     * @param weatherQueryType
     * @return
     * @throws MalformedURLException
     */
    private static URL buildUrl(@NonNull WeatherDataType weatherDataType, @NonNull WeatherQueryType weatherQueryType,
                                @Nullable Long[] cityIds, @Nullable LatLng latLng) throws MalformedURLException {
        Log.d(TAG, "buildUrl: " + weatherDataType + ", " + weatherQueryType);

        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(DATA)
                .appendPath(API_VERSION);

        switch (weatherDataType) {
            case GROUP_CURRENT_WEATHER:
                uriBuilder.appendPath(GROUP);
                break;

            case CURRENT_WEATHER:
                uriBuilder.appendPath(CURRENT_WEATHER);
                break;

            case DAILY_FORECAST:
                uriBuilder.appendPath(FORECAST)
                        .appendPath(DAILY)
                        .appendQueryParameter(QUERY_COUNT, String.valueOf(DAILY_FORECAST_COUNT));
                break;

            case TRIHOUR_FORECAST:
                uriBuilder.appendPath(FORECAST);
                break;
        }

        //append the appropriate query parameter for city
        switch (weatherQueryType) {
            case CITY_ID:
                if (cityIds == null) {
                    throw new MalformedURLException("CityIds should not be null for weatherQueryType:" + weatherQueryType);
                }
                uriBuilder.appendQueryParameter(QUERY_CITY_ID, String.valueOf(cityIds[0]));
                break;

            case CITY_LATLNG:
                if (latLng == null) {
                    throw new MalformedURLException("LatLng should not be null for weatherQueryType:" + weatherQueryType);
                }
                uriBuilder.appendQueryParameter(QUERY_CITY_LATITUDE, String.valueOf(latLng.latitude));
                uriBuilder.appendQueryParameter(QUERY_CITY_LONGITUDE, String.valueOf(latLng.longitude));
                break;

            case CITY_MULTI:
                if (cityIds == null) {
                    throw new MalformedURLException("CityIds should not be null for weatherQueryType:" + weatherQueryType);
                }
                StringBuilder cityIdsBuilder = new StringBuilder();
                for (int i=0; i<cityIds.length; i++) {
                    cityIdsBuilder.append(String.valueOf(cityIds[i]));
                    if (i != cityIds.length-1)
                        cityIdsBuilder.append(CITY_ID_SEPARATOR);
                }
                uriBuilder.appendQueryParameter(QUERY_CITY_ID, cityIdsBuilder.toString());
                break;
        }

        //append the required query parameters for every weather data type
        uriBuilder.appendQueryParameter(QUERY_UNIT, UNIT_IMPERIAL)
                .appendQueryParameter(QUERY_APP_ID, APP_ID);

        Uri uri = uriBuilder.build();
        Log.d(TAG, "buildUrl: " + uri.toString());

        return new URL(uri.toString());
    }

    /**
     * Calls the weather API, and processes the response into content values.
     *
     * @param url
     * @return
     * @throws IOException
     */
    @Nullable
    private static ContentValues[] performGet(URL url, WeatherDataType weatherDataType) throws IOException {
        Log.d(TAG, "performGet: weatherDataType: " + weatherDataType);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            urlConnection.setRequestMethod(HTTP_REQUEST_METHOD);
            urlConnection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            //check the response code and process accordingly
            int httpStatus = urlConnection.getResponseCode();
            Log.d(TAG, "performGet: HTTP status:" + httpStatus);
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                String encoding = FetchDataUtils.getEncodingFromHeader(urlConnection);

                return buildContentValues(
                        urlConnection.getInputStream(),
                        encoding,
                        weatherDataType);
            }

            //if we reached this, it means we have an error
            FetchDataUtils.logErrorStream(urlConnection.getErrorStream());
            return null;
        }
        finally {
            //always disconnect regardless of success or failure
            urlConnection.disconnect();
        }
    }

    /**
     * Processes the response from the API into content values.
     * @param stream
     * @param encoding
     * @param weatherDataType
     * @return
     * @throws IOException
     */
    private static ContentValues[] buildContentValues(InputStream stream,
                                               String encoding,
                                               WeatherDataType weatherDataType) throws IOException {
        //Log.d(TAG, "buildContentValues: weatherDataType:" + weatherDataType);

        switch (weatherDataType) {
            case GROUP_CURRENT_WEATHER:
                GroupCurrentWeatherJsonReader groupCurrentWeatherJsonReader = new GroupCurrentWeatherJsonReader(stream, encoding);
                return groupCurrentWeatherJsonReader.process();

            case CURRENT_WEATHER:
                CurrentWeatherJsonReader currentWeatherJsonReader = new CurrentWeatherJsonReader(stream, encoding);
                return currentWeatherJsonReader.process();

            case DAILY_FORECAST:
                DailyForecastJsonReader dailyForecastJsonReader = new DailyForecastJsonReader(stream, encoding);
                return dailyForecastJsonReader.process();

            case TRIHOUR_FORECAST:
                TriHourForecastJsonReader triHourForecastJsonReader = new TriHourForecastJsonReader(stream, encoding);
                return triHourForecastJsonReader.process();

            default:
                Log.d(TAG, "buildContentValues: Unknown weatherDataType: " + weatherDataType);
                return null;
        }
    }

    /**
     * Helper method to purge old data from the database.
     * Needed for the daily forecast and tri hour forecast tables because the unique keys are based on both city id and forecast time.
     * Because of the combined city id and forecast time index, some rows are never replaced because the time has passed and the service
     * no longer returns forecast for those times.
     *
     * This is not a problem for the current weather table because the unique key is the city id which means the row is always replaced.
     * However, current weather table needs to be have non user favorites cleaned out every time a new current location is added.
     *
     * Current weather table: Purge all data that is not a user favorite
     * Daily forecast table:  Purge all data that is earlier than 12:01 AM today.
     * Tri hour forecast table: Purge all data earlier than current time.
     */
    private static void purgeOldData(Context context, WeatherDataType weatherDataType) {
        Log.d(TAG, "purgeOldData: weatherDataType: " + weatherDataType);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

        switch (weatherDataType) {
            case GROUP_CURRENT_WEATHER:
                //nothing to purge
                break;
            case CURRENT_WEATHER:
                //purge old non user favorites from current weather table
                context.getContentResolver().delete(
                        CurrentWeatherContract.URI,
                        BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.USER_FAVORITE),
                        BaseWeatherContract.whereArgs(CurrentWeatherContract.USER_FAVORITE_NO));
                break;

            case DAILY_FORECAST:
                //calculate what is 12:01 AM today
                TimeZone timeZone = TimeZone.getDefault();
                Calendar calendar = Calendar.getInstance(timeZone);
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 1);
                long purgeMillis = calendar.getTimeInMillis();

                //for debugging purposes only
                String purgeTimeString = simpleDateFormat.format(new Date(purgeMillis));
                Log.d(TAG, String.format("purgeOldData: Purge Time String:%s:", purgeTimeString));

                //purge old data from daily forecast table
                context.getContentResolver().delete(
                        DailyForecastContract.URI,
                        BaseWeatherContract.whereClauseLessThan(DailyForecastContract.Columns.FORECAST_DATETIME),
                        BaseWeatherContract.whereArgs(purgeMillis));
                break;

            case TRIHOUR_FORECAST:
                //purge old data from tri hour forecast table
                long currentMillis = System.currentTimeMillis();

                //for debugging purposes only
                String currentTimeString = simpleDateFormat.format(new Date(currentMillis));
                Log.d(TAG, String.format("purgeOldData: Current Time String:%s:", currentTimeString));

                context.getContentResolver().delete(
                        TriHourForecastContract.URI,
                        BaseWeatherContract.whereClauseLessThan(TriHourForecastContract.Columns.FORECAST_DATETIME),
                        BaseWeatherContract.whereArgs(currentMillis));
                break;

            default:
                Log.d(TAG, "purgeOldData: Unknown weatherDataType: " + weatherDataType);
        }
    }
}
