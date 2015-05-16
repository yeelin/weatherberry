package com.example.yeelin.homework2.h312yeelin.json;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/16/15.
 */
public class DailyForecastJsonReader extends BaseWeatherJsonReader {
    private static final String TAG = DailyForecastJsonReader.class.getCanonicalName();

    /**
     * Base constructor
     *
     * @param stream
     * @throws java.io.UnsupportedEncodingException
     */
    public DailyForecastJsonReader(InputStream stream) throws UnsupportedEncodingException {
        super(stream);
    }

    /**
     * Constructor
     *
     * @param stream
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public DailyForecastJsonReader(InputStream stream, @Nullable String encoding) throws UnsupportedEncodingException {
        super(stream, encoding);
    }

    @Override
    @NonNull
    public ContentValues[] process() throws IOException {
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
                    case DailyForecastContract.Json.CITY_OBJECT:
                        HashMap<String, Long> cityMap = processCityObject();
                        cityId = cityMap.get(DailyForecastContract.Columns.CITY_ID);
                        break;

                    //list
                    case DailyForecastContract.Json.LIST_ARRAY:
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

        ContentValues[] valuesArray = new ContentValues[valuesArrayList.size()];
        return valuesArrayList.toArray(valuesArray);
    }

    @Override
    @NonNull
    protected ContentValues processListObject(long cityId) throws IOException {
        //Log.d(TAG, "processListObject");
        ContentValues values = new ContentValues();

        //add city id to content values
        values.put(DailyForecastContract.Columns.CITY_ID, cityId);

        jsonReader.beginObject(); //consume {
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processListObject: name:" + name);

            switch (name) {
                //dt
                case DailyForecastContract.Json.FORECAST_DATETIME:
                    long dateMillis = jsonReader.nextLong() * 1000;
                    values.put(DailyForecastContract.Columns.FORECAST_DATETIME, dateMillis);

                    //utc date
                    //Date dateUTC = new Date(dateMillis);
                    //Log.d(TAG,"processListObject: DateUTC:" + dateUTC);
                    //formatted date
                    //SimpleDateFormat formatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ", Locale.US);
                    //Log.d(TAG, "processListObject: Formatted date: " + formatter.format(dateUTC));
                    break;

                //temp
                case DailyForecastContract.Json.TEMPERATURE_OBJECT:
                    HashMap<String, Double> tempMap = processTemperatureObject();
                    values.put(DailyForecastContract.Columns.TEMPERATURE_LOW, tempMap.get(DailyForecastContract.Columns.TEMPERATURE_LOW));
                    values.put(DailyForecastContract.Columns.TEMPERATURE_HIGH, tempMap.get(DailyForecastContract.Columns.TEMPERATURE_HIGH));
                    break;

                //weather
                case DailyForecastContract.Json.WEATHER_ARRAY:
                    HashMap<String, String> weatherMap = processWeatherArray();
                    if (weatherMap != null) {
                        values.put(DailyForecastContract.Columns.ICON, weatherMap.get(DailyForecastContract.Columns.ICON));
                    }
                    break;

                //pressure, humidity, speed, deg, clouds
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processListObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject(); //consume }

        return values;
    }

    @NonNull
    private HashMap<String, Double> processTemperatureObject() throws IOException {
        //Log.d(TAG, "processTemperatureObject");
        HashMap<String, Double> map = new HashMap<>(2);

        jsonReader.beginObject(); //consume {
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processTemperatureObject: name:" + name);
            switch (name) {
                //min
                case DailyForecastContract.Json.TEMPERATURE_LOW:
                    map.put(DailyForecastContract.Columns.TEMPERATURE_LOW, jsonReader.nextDouble());
                    break;

                //max
                case DailyForecastContract.Json.TEMPERATURE_HIGH:
                    map.put(DailyForecastContract.Columns.TEMPERATURE_HIGH, jsonReader.nextDouble());
                    break;

                //day, night, eve, morn
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processTemperatureObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject(); //consume }

        return map;
    }
}
