package com.example.yeelin.homework.weatherberry.loader;

import android.content.ContentUris;
import android.net.Uri;

import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;

/**
 * Created by ninjakiki on 4/20/15.
 */
public abstract class BaseWeatherLoaderCallbacks {
    public enum IdType {
        ROW_ID,
        CITY_ID
    }

    public static Uri buildUri(Uri uri, long id, IdType idType) {
        switch (idType) {
            case ROW_ID:
                return ContentUris.withAppendedId(uri, id);

            case CITY_ID:
                Uri uriWithCityIdPath = uri.buildUpon()
                        .appendPath(BaseWeatherContract.PATH_CITYID)
                        .build();
                return ContentUris.withAppendedId(uriWithCityIdPath, id);

            default:
                return uri;
        }
    }
}
