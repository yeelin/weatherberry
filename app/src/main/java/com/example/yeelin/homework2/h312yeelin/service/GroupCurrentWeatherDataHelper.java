package com.example.yeelin.homework2.h312yeelin.service;

import android.content.ContentValues;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.json.GroupCurrentWeatherJsonReader;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ninjakiki on 5/25/15.
 */
public class GroupCurrentWeatherDataHelper {
    private static final String TAG = GroupCurrentWeatherDataHelper.class.getCanonicalName();

    /**
     * Processes the multi city response from the group API into content values for insertion into current_weather table.
     * @param stream
     * @param encoding
     * @return
     * @throws java.io.IOException
     */
    public static ContentValues[] buildContentValues(InputStream stream,
                                                     String encoding) throws IOException {
        Log.d(TAG, "buildContentValuesFromMultiCityResponse");
        GroupCurrentWeatherJsonReader groupCurrentWeatherJsonReader = new GroupCurrentWeatherJsonReader(stream, encoding);
        return groupCurrentWeatherJsonReader.process();
    }
}
