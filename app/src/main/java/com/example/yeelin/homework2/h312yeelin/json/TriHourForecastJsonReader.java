package com.example.yeelin.homework2.h312yeelin.json;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 4/17/15.
 */
public class TriHourForecastJsonReader extends BaseWeatherJsonReader {
    //logcat
    private static final String TAG = TriHourForecastJsonReader.class.getCanonicalName();

    /**
     * Base constructor
     *
     * @param stream
     * @throws java.io.UnsupportedEncodingException
     */
    public TriHourForecastJsonReader(InputStream stream) throws UnsupportedEncodingException {
        super(stream);
    }

    /**
     * Constructor
     *
     * @param stream
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public TriHourForecastJsonReader(InputStream stream, @Nullable String encoding) throws UnsupportedEncodingException {
        super(stream, encoding);
    }

    @Override
    @NonNull
    //public ContentValues[] process() throws IOException {
    public ArrayList<ContentValues> process() throws IOException {
        //Log.d(TAG, "process");
        ArrayList<ContentValues> valuesArrayList = new ArrayList<>();
        long cityId = 0;

        try {
            jsonReader.beginObject(); //consume {
            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                //Log.d(TAG, "process: name:" + name);

                switch (name) {
                    //city
                    case TriHourForecastContract.Json.CITY_OBJECT:
                        HashMap<String, Long> cityMap = processCityObject();
                        cityId = cityMap.get(TriHourForecastContract.Columns.CITY_ID);
                        break;

                    //list
                    case TriHourForecastContract.Json.LIST_ARRAY:
                        valuesArrayList = processListArray(cityId);
                        break;

                    //cod, message, cnt
                    default:
                        jsonReader.skipValue();
                        //Log.d(TAG, "process: Skipping value for name:" + name);
                        break;
                }
            }
            jsonReader.endObject(); //consume }
        }
        finally {
            jsonReader.close();
        }

        //ContentValues[] valuesArray = new ContentValues[valuesArrayList.size()];
        //return valuesArrayList.toArray(valuesArray);
        return valuesArrayList;
    }

    @Override
    @NonNull
    protected ContentValues processListObject(long cityId) throws IOException {
        //Log.d(TAG, "processListObject");
        ContentValues values = new ContentValues();

        //add city id to content values
        values.put(TriHourForecastContract.Columns.CITY_ID, cityId);

        jsonReader.beginObject(); //consume {
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processListObject: name:" + name);

            switch (name) {
                //dt
                case TriHourForecastContract.Json.FORECAST_DATETIME:
                    long utcDateMillis = jsonReader.nextLong() * 1000;
                    values.put(TriHourForecastContract.Columns.FORECAST_DATETIME, utcDateMillis);

                    //utc date
                    //Date utcDate = new Date(utcDateMillis);
                    //SimpleDateFormat utcDateFormatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ");
                    //utcDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    //Log.d(TAG,"processListObject: dt: DateUTC:" + utcDateFormatter.format(utcDate));

                    //formatted date
                    //SimpleDateFormat usDateFormatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ", Locale.US);
                    //Log.d(TAG, "processListObject: dt: Formatted date: " + usDateFormatter.format(utcDate));
                    break;

                //main
                case TriHourForecastContract.Json.MAIN_OBJECT:
                    HashMap<String, Double> mainMap = processMainObject();
                    values.put(TriHourForecastContract.Columns.TEMPERATURE, mainMap.get(TriHourForecastContract.Columns.TEMPERATURE));
                    break;

                //weather
                case TriHourForecastContract.Json.WEATHER_ARRAY:
                    HashMap<String, String> weatherMap = processWeatherArray();
                    if (weatherMap != null) {
                        values.put(TriHourForecastContract.Columns.DESCRIPTION, weatherMap.get(TriHourForecastContract.Columns.DESCRIPTION));
                        values.put(TriHourForecastContract.Columns.ICON, weatherMap.get(TriHourForecastContract.Columns.ICON));
                    }
                    break;

                /*
                //dt_txt
                case TriHourForecastContract.Json.FORECAST_DATETIME_TXT:
                    String utcDateString = jsonReader.nextString();

                    //parse the utc date string to get time in milliseconds for storage
                    SimpleDateFormat utcDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    utcDateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Log.d(TAG, "processListObject: dt_txt: Formatted date: " + utcDateString);
                    try {
                        //utc date
                        Date utcDateTxt = utcDateFormatter.parse(utcDateString);
                        Log.d(TAG, "processListObject: dt_txt: DateUTC: " + utcDateTxt);
                        values.put(TriHourForecastContract.Columns.FORECAST_DATETIME_TXT, utcDateTxt.getTime());
                    }
                    catch (ParseException e) {
                        Log.d(TAG, "processListObject: Failed to parse dt_txt.", e);
                    }
                    break;
                */

                //clouds, wind, sys, dt_txt
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processListObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject(); //consume }

        return values;
    }


}
