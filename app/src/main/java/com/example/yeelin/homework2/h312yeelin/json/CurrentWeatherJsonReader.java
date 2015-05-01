package com.example.yeelin.homework2.h312yeelin.json;

import android.content.ContentValues;
import android.support.annotation.Nullable;

import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

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
public class CurrentWeatherJsonReader extends BaseWeatherJsonReader {
    private static final String TAG = CurrentWeatherJsonReader.class.getCanonicalName();


    /**
     * Base constructor
     *
     * @param stream
     * @throws java.io.UnsupportedEncodingException
     */
    public CurrentWeatherJsonReader(InputStream stream) throws UnsupportedEncodingException {
        super(stream);
    }

    /**
     * Constructor
     *
     * @param stream
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public CurrentWeatherJsonReader(InputStream stream, @Nullable String encoding) throws UnsupportedEncodingException {
        super(stream, encoding);
    }

    @Override
    public ContentValues[] process() throws IOException {
        //Log.d(TAG, "process");

        ArrayList<ContentValues> valuesArrayList = new ArrayList<>();
        ContentValues values = new ContentValues();

        try {
            jsonReader.beginObject(); //consume {

            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                //Log.d(TAG, "process: name:" + name);
                switch (name) {
                    //coord
                    case CurrentWeatherContract.Json.COORD_OBJECT:
                        HashMap<String, Double> coordMap = processCoordinatesObject();
                        values.put(CurrentWeatherContract.Columns.CITY_LATITUDE, coordMap.get(CurrentWeatherContract.Columns.CITY_LATITUDE));
                        values.put(CurrentWeatherContract.Columns.CITY_LONGITUDE, coordMap.get(CurrentWeatherContract.Columns.CITY_LONGITUDE));
                        break;

                    //weather
                    case CurrentWeatherContract.Json.WEATHER_ARRAY:
                        HashMap<String, String> weatherMap = processWeatherArray();
                        values.put(CurrentWeatherContract.Columns.SUMMARY, weatherMap.get(CurrentWeatherContract.Columns.SUMMARY));
                        values.put(CurrentWeatherContract.Columns.DESCRIPTION, weatherMap.get(CurrentWeatherContract.Columns.DESCRIPTION));
                        break;

                    //main
                    case CurrentWeatherContract.Json.MAIN_OBJECT:
                        HashMap<String, Double> mainMap = processMainObject();
                        values.put(CurrentWeatherContract.Columns.TEMPERATURE, mainMap.get(CurrentWeatherContract.Columns.TEMPERATURE));
                        values.put(CurrentWeatherContract.Columns.HUMIDITY, mainMap.get(CurrentWeatherContract.Columns.HUMIDITY));
                        break;

                    //wind
                    case CurrentWeatherContract.Json.WIND_OBJECT:
                        HashMap<String, Double> windMap = processWindObject();
                        values.put(CurrentWeatherContract.Columns.WIND_SPEED, windMap.get(CurrentWeatherContract.Columns.WIND_SPEED));
                        break;

                    //dt
                    case CurrentWeatherContract.Json.TIMESTAMP:
                        long dateMillis = jsonReader.nextLong() * 1000;
                        values.put(CurrentWeatherContract.Columns.TIMESTAMP, dateMillis);
                        //utc date
                        Date dateUTC = new Date(dateMillis);
                        //Log.d(TAG, "process: DateUTC:" + dateUTC);
                        //formatted date
                        SimpleDateFormat formatter = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mmZ", Locale.US);
                        //Log.d(TAG, "process: Formatted date: " + formatter.format(dateUTC));
                        break;

                    //id
                    case CurrentWeatherContract.Json.CITY_ID:
                        values.put(CurrentWeatherContract.Columns.CITY_ID, jsonReader.nextLong());
                        break;

                    //name
                    case CurrentWeatherContract.Json.CITY_NAME:
                        values.put(CurrentWeatherContract.Columns.CITY_NAME, jsonReader.nextString());
                        break;

                    //sys, base, clouds, cod
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

        valuesArrayList.add(values);
        ContentValues[] valuesArray = new ContentValues[valuesArrayList.size()];
        return valuesArrayList.toArray(valuesArray);
    }

    @Override
    protected ContentValues processListObject(long cityId) throws IOException {
        //do nothing since we don't have a list object in current weather response
        return null;
    }

    /**
     * Helper method for processing city coordinates
     * @return
     * @throws IOException
     */
    private HashMap<String, Double> processCoordinatesObject() throws IOException {
        //Log.d(TAG, "processCoordinatesObject");
        HashMap<String, Double> map = new HashMap<>();

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processCoordinatesObject: name:" + name);
            switch (name) {
                //lat
                case CurrentWeatherContract.Json.CITY_LATITUDE:
                    map.put(CurrentWeatherContract.Columns.CITY_LATITUDE, jsonReader.nextDouble());
                    break;

                //lon
                case CurrentWeatherContract.Json.CITY_LONGITUDE:
                    map.put(CurrentWeatherContract.Columns.CITY_LONGITUDE, jsonReader.nextDouble());
                    break;

                //nothing
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processCoordinatesObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject();

        return map;
    }

    /**
     * Helper method for processing Wind conditions
     * @return
     * @throws IOException
     */
    private HashMap<String, Double> processWindObject() throws IOException {
        //Log.d(TAG, "processWindObject");
        HashMap<String, Double> map = new HashMap<>();

        jsonReader.beginObject();
        while(jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processWindObject: name:" + name);
            switch (name) {
                //speed
                case CurrentWeatherContract.Json.WIND_SPEED:
                    map.put(CurrentWeatherContract.Columns.WIND_SPEED, jsonReader.nextDouble());
                    break;
                //deg
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processWindObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject();

        return map;
    }
}
