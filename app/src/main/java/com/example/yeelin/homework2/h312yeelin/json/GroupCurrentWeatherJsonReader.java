package com.example.yeelin.homework2.h312yeelin.json;

import android.content.ContentValues;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.GroupCurrentWeatherContract;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/15/15.
 * This class reads the group API's response for current weather.  The json it reads is very
 * similar to the json read by CurrentWeatherJsonReader. CurrentWeatherJsonReader reads the
 * current weather response for 1 city, while GroupCurrentWeatherJsonReader reads for multiple
 * cities hence Group extends from Current.
 */
public class GroupCurrentWeatherJsonReader extends CurrentWeatherJsonReader {
    private static final String TAG = GroupCurrentWeatherJsonReader.class.getCanonicalName();

    /**
     * Base constructor
     *
     * @param stream
     * @throws java.io.UnsupportedEncodingException
     */
    public GroupCurrentWeatherJsonReader(InputStream stream) throws UnsupportedEncodingException {
        super(stream);
    }

    /**
     * Constructor
     *
     * @param stream
     * @param encoding
     * @throws java.io.UnsupportedEncodingException
     */
    public GroupCurrentWeatherJsonReader(InputStream stream, @Nullable String encoding) throws UnsupportedEncodingException {
        super(stream, encoding);
    }

    /**
     * Call this method to start processing the json response for current weather (group) queries.
     * Note that there can be more than 1 list object in an array in the response.
     * @return
     * @throws IOException
     */
    @Override
    @NonNull
    //public ContentValues[] process() throws IOException {
    public ArrayList<ContentValues> process() throws IOException {
        //Log.d(TAG, "process");
        ArrayList<ContentValues> valuesArrayList = new ArrayList<>();

        try {
            jsonReader.beginObject(); //consume {

            while (jsonReader.hasNext()) {
                String name = jsonReader.nextName();
                //Log.d(TAG, "process: name:" + name);

                switch (name) {
                    //list
                    case GroupCurrentWeatherContract.Json.LIST_ARRAY:
                        valuesArrayList = processListArray();
                        break;

                    //cnt
                    default:
                        jsonReader.skipValue();
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

    /**
     * Process the list array in the response.  Returns an arraylist of current weather
     * for cities.
     * @return
     * @throws IOException
     */
    @NonNull
    protected ArrayList<ContentValues> processListArray() throws IOException {
        //Log.d(TAG, "processListArray");
        ArrayList<ContentValues> list = new ArrayList<>();

        jsonReader.beginArray(); //consume [
        while (jsonReader.hasNext()) {
            //call CurrentWeatherJsonReader's processListObject on each object in the list array
            list.add(processListObject());
        }
        jsonReader.endArray(); //consume ]

        return list;
    }
}
