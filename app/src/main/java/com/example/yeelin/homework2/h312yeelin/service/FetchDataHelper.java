package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.CurrentWeatherJsonReader;
import com.example.yeelin.homework2.h312yeelin.json.DailyForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.json.TriHourForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.CacheUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ConnectivityUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    private static final String FORECAST = "forecast";
    private static final String DAILY = "daily";

    private static final String QUERY_CITYID = "id";
    private static final String QUERY_UNIT = "units";
    private static final String QUERY_COUNT = "cnt";
    private static final String QUERY_APPID = "APPID";

    //TODO: remove these hardcoded values
    private static final String CITY_ID = "5809844"; //city id for Seattle
    private static final String UNIT_IMPERIAL = "imperial";
    private static final int DAILY_FORECAST_COUNT = 5;
    private static final String APP_ID = "3284992e5bfef187c44863ce0f31ad30";

    private enum WeatherDataType {
        CURRENT_WEATHER,
        DAILY_FORECAST,
        TRIHOUR_FORECAST
    }

    /**
     * Helper method that handles action load on the background thread.
     * Called from onHandleIntent.
     */
    public static void handleActionLoad(Context context) {
        Log.d(TAG, "handleActionLoad");

        //check if we have the latest SSL, and if this fails, exit
        if (!ensureLatestSSL(context)) {
            return;
        }

        //no network, so do nothing and return
        if (ConnectivityUtils.isNotConnected(context)) {
            Log.d(TAG, "handleActionLoad: Not connected to network");
            return;
        }

        //initialize the cache
        //if already exists, then the existing one is used
        CacheUtils.initializeCache(context);

        //check last fetch time before fetching again
        long lastFetchMillis = determineLastFetch(context);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm Z", Locale.US);
        if (System.currentTimeMillis() - lastFetchMillis < TEN_MINUTES_MILLIS) {
            //last fetch < 10 minutes ago so skip
            Log.d(TAG, String.format("handleActionLoad: Last fetch was less than 10 minutes ago. Current time:%s, Last fetch:%s",
                    formatter.format(new Date(System.currentTimeMillis())),
                    formatter.format(new Date(lastFetchMillis))));
            return;
        }

        //last fetch > 10 minutes ago so continue
        Log.d(TAG, String.format("handleActionLoad: Last fetch was more than 10 minutes ago. Current time:%s, Last fetch:%s",
                formatter.format(new Date(System.currentTimeMillis())),
                formatter.format(new Date(lastFetchMillis))));

        try {
            //retrieve weather data and return in format ready for persistent storage
            ContentValues[] currentWeatherValues = getData(WeatherDataType.CURRENT_WEATHER);
            ContentValues[] dailyForecastValues = getData(WeatherDataType.DAILY_FORECAST);
            ContentValues[] triHourForecastValues = getData(WeatherDataType.TRIHOUR_FORECAST);

            //persist the weather data
            persistData(context, currentWeatherValues, WeatherDataType.CURRENT_WEATHER);
            persistData(context, dailyForecastValues, WeatherDataType.DAILY_FORECAST);
            persistData(context, triHourForecastValues, WeatherDataType.TRIHOUR_FORECAST);

            //purge anything that is too old i.e. anything earlier than today at 12:00 AM
            purgeOldData(context, WeatherDataType.DAILY_FORECAST);
            purgeOldData(context, WeatherDataType.TRIHOUR_FORECAST);
        }
        catch (Exception e) {
            Log.d(TAG, "handleActionLoad: Unexpected error", e);
        }

        CacheUtils.logCache();
    }

    /**
     * Helper method that checks the device has the latest security updates.
     * @return
     */
    private static boolean ensureLatestSSL(Context context) {
        try {
            //ensure the latest SSL per
            //http://developer.android.com/training/articles/security-gms-provider.html
            ProviderInstaller.installIfNeeded(context);
            return true;
        }
        catch (GooglePlayServicesRepairableException e) {
            //since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.getConnectionStatusCode(), context);
            Log.d(TAG, "ensureLatestSSL: Repairable error updating SSL");
            return false;
        }
        catch (GooglePlayServicesNotAvailableException e) {
            //since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.errorCode, context);
            Log.d(TAG, "ensureLatestSSL: Missing play servers updating SSL");
            return false;
        }
    }

    /**
     * Helper method that checks the current_weather table for the timestamp of
     * the last call to the API.
     * @return
     */
    private static long determineLastFetch(Context context) {
        //retrieve the timestamp from the current_weather table
        String[] projection = new String[] {
                "max(" + CurrentWeatherContract.Columns.TIMESTAMP + ")"
        };
        Cursor cursor = context.getContentResolver().query(
                CurrentWeatherContract.URI,
                projection,
                null,
                null,
                null);

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
     * Retrieves data from the API by first building the url, calling the API, and then
     * processing the response into content values.
     *
     * @param weatherDataType
     * @return
     */
    @Nullable
    private static ContentValues[] getData(WeatherDataType weatherDataType) {
        Log.d(TAG, "getData: weatherDataType: " + weatherDataType);
        ContentValues[] valuesArray = null;

        try {
            URL url;

            switch (weatherDataType) {
                case CURRENT_WEATHER:
                    url = buildUrl(WeatherDataType.CURRENT_WEATHER);
                    valuesArray = performGet(url, WeatherDataType.CURRENT_WEATHER);

                    if (valuesArray != null && valuesArray.length > 0) {
                        Log.d(TAG, "Adding unit hack");
                        //TODO: fix unit hack
                        valuesArray[0].put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);

                        long feedTimeMillis = valuesArray[0].getAsLong(CurrentWeatherContract.Columns.TIMESTAMP);
                        SimpleDateFormat formatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ", Locale.US);
                        Log.d(TAG, String.format("Feed timestamp:" + feedTimeMillis + " Formatted:" + formatter.format(new Date(feedTimeMillis))));

                        //insert timestamp
                        long currentTimeMillis = new Date().getTime();
                        Log.d(TAG, String.format("Curr timestamp:" + currentTimeMillis + " Formatted:" + formatter.format(new Date(currentTimeMillis))));
                        valuesArray[0].put(CurrentWeatherContract.Columns.TIMESTAMP, currentTimeMillis);
                    }
                    //logContentValuesArray(valuesArray, WeatherDataType.CURRENT_WEATHER);
                    break;

                case DAILY_FORECAST:
                    url = buildUrl(WeatherDataType.DAILY_FORECAST);
                    valuesArray = performGet(url, WeatherDataType.DAILY_FORECAST);
                    //logContentValuesArray(valuesArray, WeatherDataType.DAILY_FORECAST);
                    break;

                case TRIHOUR_FORECAST:
                    url = buildUrl(WeatherDataType.TRIHOUR_FORECAST);
                    valuesArray = performGet(url, WeatherDataType.TRIHOUR_FORECAST);
                    //logContentValuesArray(valuesArray, WeatherDataType.TRIHOUR_FORECAST);
                    break;

            }
        }
        catch (MalformedURLException e) {
            Log.d(TAG, "Unexpected error:", e);
        }
        catch (IOException e) {
            Log.d(TAG, "Unexpected error:", e);
        }

        //Log.d(TAG, "getData: values:" + valuesArray);
        return valuesArray;
    }

    /**
     * Persists the data into the database.
     * @param valuesArray
     * @param weatherDataType
     */
    private static void persistData(Context context, @Nullable ContentValues[] valuesArray, WeatherDataType weatherDataType) {
        Log.d(TAG, "persistData: weatherDataType: " + weatherDataType);
        if (valuesArray != null) {
            //insert
            switch (weatherDataType) {
                case CURRENT_WEATHER:
                    //TODO: using values[0] right now. need to fix hack
                    context.getContentResolver().insert(CurrentWeatherContract.URI, valuesArray[0]);
                    break;

                case DAILY_FORECAST:
                    context.getContentResolver().bulkInsert(DailyForecastContract.URI, valuesArray);
                    break;

                case TRIHOUR_FORECAST:
                    context.getContentResolver().bulkInsert(TriHourForecastContract.URI, valuesArray);
                    break;
            }
        }
    }

    /**
     * Builds a url for querying the open weather api.
     * Format depends on the weather data type:
     * Current weather:
     * http://api.openweathermap.org/data/2.5/weather?id=5128638&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * Daily forecast:
     * http://api.openweathermap.org/data/2.5/forecast/daily?id=5809844&units=imperial&cnt=5&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * Trihour forecast:
     * http://api.openweathermap.org/data/2.5/forecast?id=5809844&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param weatherDataType
     * @return
     * @throws MalformedURLException
     */
    private static URL buildUrl(WeatherDataType weatherDataType) throws MalformedURLException {
        Log.d(TAG, "buildUrl: weatherDataType: " + weatherDataType);
        Uri.Builder uriBuilder = new Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(DATA)
                .appendPath(API_VERSION);

        switch (weatherDataType) {
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

        //append the required query parameters for every weather data type
        uriBuilder.appendQueryParameter(QUERY_CITYID, CITY_ID)
                .appendQueryParameter(QUERY_UNIT, UNIT_IMPERIAL)
                .appendQueryParameter(QUERY_APPID, APP_ID);

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
                Log.d(TAG, "performGet: Http status OK");

                String encoding = getEncodingFromHeader(urlConnection);

                return buildContentValues(
                        urlConnection.getInputStream(),
                        encoding,
                        weatherDataType);
            }

            //if we reached this, it means we have an error
            logErrorStream(urlConnection.getErrorStream());
            return null;
        }
        finally {
            //always disconnect regardless of success or failure
            urlConnection.disconnect();
        }
    }

    /**
     * Helper method that tries to get the content encoding from the response header
     * @param urlConnection
     * @return
     */
    @Nullable
    private static String getEncodingFromHeader(HttpURLConnection urlConnection) {
        String encoding = null;

        //first try to get it from content encoding
        encoding = urlConnection.getContentEncoding();
        if (encoding != null) {
            return encoding;
        }

        //next try to get it from content type
        String contentType = urlConnection.getContentType();
        if (contentType == null) {
            return null;
        }

        //parse content type
        //content type is likely of the form: application/json; charset=utf-8
        String[] values = contentType.split(";");

        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                encoding = value.substring("charset=".length());
            }
        }

        Log.d(TAG, "getEncodingFromContentTypeHeader: Encoding:" + encoding);
        return (encoding == null || encoding.equals("")) ? null : encoding;
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
        Log.d(TAG, "buildContentValues: weatherDataType:" + weatherDataType);

        switch (weatherDataType) {
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
     *
     * Daily forecast table:  Purge all data that is earlier than 12:01 AM today.
     * Tri hour forecast table: Purge all data earlier than current time.
     */
    private static void purgeOldData(Context context, WeatherDataType weatherDataType) {
        Log.d(TAG, "purgeOldData: weatherDataType: " + weatherDataType);

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

        switch (weatherDataType) {
            case CURRENT_WEATHER:
                //nothing to purge
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

    /**
     * Helper method that just logs the values in the valuesArray.
     * @param valuesArray
     * @param weatherDataType
     */
    private static void logContentValuesArray(@Nullable ContentValues[] valuesArray, WeatherDataType weatherDataType) {
        Log.d(TAG, "logContentValuesArray: weatherDataType:" + weatherDataType);
        if (valuesArray != null) {
            for (ContentValues values : valuesArray) {
                Log.d(TAG, "Values:" + values);
            }
        }
        Log.d(TAG, "logContentValuesArray: Done");
    }

    /**
     * Log the error stream when something goes wrong with the http connection.
     * @param errorStream
     * @throws IOException
     */
    private static void logErrorStream(@Nullable InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
        try {
            String result;
            while ((result = reader.readLine()) != null) {
                builder.append(result);
            }
        }
        finally {
            reader.close();
        }

        Log.w(TAG, builder.toString());
    }

}
