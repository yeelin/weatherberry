package com.example.yeelin.homework2.h312yeelin.json;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.JsonReader;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ninjakiki on 4/16/15.
 */
public abstract class BaseWeatherJsonReader {
    //logcat
    private static final String TAG = BaseWeatherJsonReader.class.getCanonicalName();

    protected static final String ENCODING = "UTF-8";
    //member variable
    protected JsonReader jsonReader;

    /**
     * Base constructor
     * @param stream
     * @throws UnsupportedEncodingException
     */
    public BaseWeatherJsonReader(InputStream stream) throws UnsupportedEncodingException {
        //create a new json reader from the input stream
        jsonReader = new JsonReader(new InputStreamReader(stream, ENCODING));
    }

    /**
     * Base constructor
     * @param stream
     * @throws UnsupportedEncodingException
     */
    public BaseWeatherJsonReader(InputStream stream, @Nullable String encoding) throws UnsupportedEncodingException {
        //create a new json reader from the input stream
        if (encoding == null) {
            encoding = ENCODING;
            Log.d(TAG, "BaseWeatherJsonReader: Encoding is null so using default:" + ENCODING);
        }
        jsonReader = new JsonReader(new InputStreamReader(stream, encoding));
    }

    @NonNull
    public abstract ContentValues[] process() throws IOException;

    /**
     * ProcessCityObject is used by daily and trihour json readers.
     * @return
     * @throws IOException
     */
    @NonNull
    protected HashMap<String, Long> processCityObject() throws IOException {
        //Log.d(TAG, "processCityObject");
        HashMap<String, Long> map = new HashMap<>(1);

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processCityObject: name:" + name);

            switch (name) {
                //id
                case BaseWeatherContract.Json.CITY_ID:
                    map.put(BaseWeatherContract.Columns.CITY_ID, jsonReader.nextLong());
                    break;

                //name, coord, country, population
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processCityObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject();

        return map;
    }

    /**
     * ProcessListArray is used by daily and trihour json readers to process the list array.
     *
     * @param cityId
     * @return
     * @throws IOException
     */
    @NonNull
    protected ArrayList<ContentValues> processListArray(long cityId) throws IOException {
        //Log.d(TAG, "processListArray");
        ArrayList<ContentValues> list = new ArrayList<>();

        jsonReader.beginArray(); //consume [
        while (jsonReader.hasNext()) {
            list.add(processListObject(cityId));
        }
        jsonReader.endArray(); //consume ]

        return list;
    }

    @NonNull
    protected abstract ContentValues processListObject(long cityId) throws IOException;

    /**
     * ProcessMainObject is used by current and trihour json readers.
     * @return
     * @throws IOException
     */
    @NonNull
    protected HashMap<String, Double> processMainObject() throws IOException {
        //Log.d(TAG, "processMainObject");
        HashMap<String, Double> map = new HashMap<>(2);

        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processMainObject: name:" + name);

            switch (name) {
                //temp
                case BaseWeatherContract.Json.TEMPERATURE:
                    map.put(BaseWeatherContract.Columns.TEMPERATURE, jsonReader.nextDouble());
                    break;

                //humidity
                case BaseWeatherContract.Json.HUMIDITY:
                    map.put(BaseWeatherContract.Columns.HUMIDITY, jsonReader.nextDouble());
                    break;

                //temp_min, temp_max, pressure, sea_level, grnd_level, temp_kif
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processMainObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject();
        return map;
    }

    /**
     * ProcessWeatherArray is used for summary, description, and icon names for now by
     * current, daily, and trihour json readers.
     * *
     * @return
     * @throws IOException
     */
    @Nullable
    protected HashMap<String, String> processWeatherArray() throws IOException {
        //Log.d(TAG, "processWeatherArray");
        HashMap<String, String> map = null;

        jsonReader.beginArray(); //consume [
        while(jsonReader.hasNext()) {
            //there is only 1 weather object so this is safe
            map = processWeatherObject();
        }
        jsonReader.endArray(); //consume ]

        return map;
    }

    /**
     * Helper method called by processWeatherArray.
     * @return
     * @throws IOException
     */
    @NonNull
    private HashMap<String, String> processWeatherObject() throws IOException {
        //Log.d(TAG, "processWeatherObject");
        HashMap<String, String> map = new HashMap<>(3);

        jsonReader.beginObject(); //consume {
        while(jsonReader.hasNext()) {
            String name = jsonReader.nextName();
            //Log.d(TAG, "processWeatherObject: name:" + name);
            switch (name) {
                //main
                case BaseWeatherContract.Json.SUMMARY:
                    map.put(BaseWeatherContract.Columns.SUMMARY, jsonReader.nextString());
                    break;

                //description
                case BaseWeatherContract.Json.DESCRIPTION:
                    map.put(BaseWeatherContract.Columns.DESCRIPTION, jsonReader.nextString());
                    break;

                //icon
                case BaseWeatherContract.Json.ICON:
                    map.put(BaseWeatherContract.Columns.ICON, jsonReader.nextString());
                    break;

                //id
                default:
                    jsonReader.skipValue();
                    //Log.d(TAG, "processWeatherObject: Skipping value for name:" + name);
                    break;
            }
        }
        jsonReader.endObject(); //consume }

        return map;
    }
}
