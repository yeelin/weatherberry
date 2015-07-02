package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.DailyForecastJsonReader;
import com.example.yeelin.homework2.h312yeelin.networkUtils.FetchDataUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class DailyForecastDataHelper {
    private static final String TAG = DailyForecastDataHelper.class.getCanonicalName();

    //middle uri parts
    private static final String PATH_FORECAST = "forecast";
    private static final String PATH_DAILY = "daily";
    private static final String QUERY_CITY_ID = "id";
    private static final String QUERY_COUNT = "cnt";
    private static final int QUERY_DAILY_FORECAST_COUNT = 5;

    /**
     * Retrieves data from the API by first building the url, calling the API, and then
     * processing the response into content values, and persisting them.
     * @param context
     * @param cityId
     * @param userFavorite
     * @return
     */
    @Nullable
    public static ArrayList<ContentValues> getDataForCityId(Context context,
                                                            long cityId,
                                                            boolean userFavorite) {
        Log.d(TAG, "getDataForCityId:" + cityId);
        ArrayList<ContentValues> valuesArrayList = null;
        try {
            final URL url = buildUrl(cityId);
            final HttpURLConnection urlConnection = FetchDataUtils.performGet(url);
            if (urlConnection == null) {
                return null;
            }

            valuesArrayList = buildContentValues(urlConnection);
            if (valuesArrayList != null && valuesArrayList.size() > 0) {
                persistData(context, valuesArrayList);
            }
        }
        catch (MalformedURLException e) {
            Log.e(TAG, "getDataForCityId: Unexpected MalformedURLException:", e);
        }
        catch (IOException e) {
            Log.e(TAG, "getDataForCityId: Unexpected IOException:", e);
        }
        return valuesArrayList;
    }

    /**
     * Builds a url for querying the forecast daily api by city id
     * Format: Daily forecast by city id:
     * http://api.openweathermap.org/data/2.5/forecast/daily?id=5809844&cnt=5&units=imperial&APPID=3284992e5bfef187c44863ce0f31ad30
     *
     * @param cityId
     * @return
     * @throws java.net.MalformedURLException
     */
    @NonNull
    public static URL buildUrl(long cityId) throws MalformedURLException {
        Log.d(TAG, "buildUrl: CityId:" + cityId);

        //header
        Uri.Builder uriBuilder = FetchDataUtils.getHeaderForUriBuilder();

        //middle
        appendMiddleToUriBuilder(uriBuilder, cityId);

        //footer
        uriBuilder = FetchDataUtils.appendFooterToUriBuilder(uriBuilder);

        //convert uri builder into a URL
        return FetchDataUtils.buildUrl(uriBuilder);
    }

    /**
     * Appends the middle part to the given uri builder and returns it
     * @param uriBuilder
     * @param cityId
     * @return
     */
    @NonNull
    private static Uri.Builder appendMiddleToUriBuilder(@NonNull Uri.Builder uriBuilder, long cityId) {
        uriBuilder.appendPath(PATH_FORECAST)
                .appendPath(PATH_DAILY)
                .appendQueryParameter(QUERY_CITY_ID, Long.toString(cityId))
                .appendQueryParameter(QUERY_COUNT, Integer.toString(QUERY_DAILY_FORECAST_COUNT));

        return uriBuilder;
    }

    /**
     * Processes the response from the API into content values for insertion into daily_forecast table.
     * @param urlConnection
     * @return
     * @throws IOException
     */
    public static ArrayList<ContentValues> buildContentValues(@NonNull HttpURLConnection urlConnection) throws IOException {
        Log.d(TAG, "buildContentValues");
        try {
            DailyForecastJsonReader dailyForecastJsonReader = new DailyForecastJsonReader(
                    urlConnection.getInputStream(), //input stream
                    FetchDataUtils.getEncodingFromHeader(urlConnection)); //encoding
            return dailyForecastJsonReader.process();
        }
        finally {
            urlConnection.disconnect();
        }
    }

    /**
     * Inserts data into the daily_forecast table.
     * @param context
     * @param valuesArrayList
     */
    public static void persistData(Context context, @NonNull ArrayList<ContentValues> valuesArrayList) {
        Log.d(TAG, "persistData");
        context.getContentResolver().bulkInsert(
                DailyForecastContract.URI,
                valuesArrayList.toArray(new ContentValues[valuesArrayList.size()]));
    }

    /**
     * Helper method to purge old data from daily_forecast table
     * Purge is needed for the daily forecast table because the unique index is based on both city id and forecast time.
     * Because of the combined city id and forecast time index, some rows are never replaced because the time has passed and the service
     * no longer returns forecast for those times.
     *
     * Current weather table: Purge all data that is not a user favorite
     * Daily forecast table:  Purge all data that is earlier than 12:01 AM today.
     * Tri hour forecast table: Purge all data earlier than current time.
     * @param context
     */
    public static void purgeOldData(Context context) {
        Log.d(TAG, "purgeOldData");

        //calculate what is 12:01 AM today
        TimeZone timeZone = TimeZone.getDefault();
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 1);
        long purgeTimeMillis = calendar.getTimeInMillis();

        //for debugging purposes only
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String purgeTimeString = simpleDateFormat.format(new Date(purgeTimeMillis));
        Log.d(TAG, String.format("purgeOldData: Purge Time String:%s:", purgeTimeString));

        //purge old data from daily forecast table
        context.getContentResolver().delete(
                DailyForecastContract.URI,
                BaseWeatherContract.whereClauseLessThan(DailyForecastContract.Columns.FORECAST_DATETIME),
                BaseWeatherContract.whereArgs(purgeTimeMillis));
    }
}
