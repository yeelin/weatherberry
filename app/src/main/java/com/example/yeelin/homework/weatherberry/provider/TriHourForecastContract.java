package com.example.yeelin.homework.weatherberry.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ninjakiki on 4/17/15.
 */
public final class TriHourForecastContract {
    /**
     * JsonReader fields
     */
    public interface Json extends BaseWeatherContract.Json {
        //public String CITY_OBJECT = BaseWeatherContract.Json.CITY_OBJECT;
        //public String CITY_ID = BaseWeatherContract.Json.CITY_ID;

        public String LIST_ARRAY = "list";
        public String FORECAST_DATETIME = "dt";

        //public String MAIN_OBJECT = BaseWeatherContract.Json.MAIN_OBJECT;
        //public String TEMPERATURE = BaseWeatherContract.Json.TEMPERATURE;

        //public String WEATHER_ARRAY = BaseWeatherContract.Json.WEATHER_ARRAY;
        //public String DESCRIPTION = BaseWeatherContract.Json.DESCRIPTION;

        //public String FORECAST_DATETIME_TXT = "dt_txt";
    }

    /**
     * Database columns
     */
    public interface Columns extends BaseWeatherContract.Columns {
        //public String CITY_ID = BaseWeatherContract.Columns.CITY_ID;
        //public String TEMPERATURE = BaseWeatherContract.Columns.TEMPERATURE;
        //public String DESCRIPTION = BaseWeatherContract.Columns.DESCRIPTION;
        public String FORECAST_DATETIME = "forecast_datetime";
        //public String FORECAST_DATETIME_TXT = "forecast_datetime_txt";
    }

    //table name
    static final String TABLE = "tri_hour_forecast";

    /**
     * Database statements
     */
    //create table
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE +
            " ( " +
            BaseColumns._ID + " INTEGER PRIMARY KEY, " +
            Columns.CITY_ID + " INTEGER NOT NULL, " +
            Columns.USER_FAVORITE + " INTEGER NOT NULL, " +
            Columns.FORECAST_DATETIME + " INTEGER NOT NULL, " +
            Columns.TEMPERATURE + " REAL NOT NULL, " +
            Columns.DESCRIPTION + " TEXT NOT NULL, " +
            Columns.ICON + " TEXT NOT NULL" +
            " )";

    //index name
    static final String INDEX_NAME = "city_id_trihour_date_index";
    //create index
    static final String CREATE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_NAME +
            " ON " + TABLE +
            " ( " + Columns.CITY_ID + ", " + Columns.USER_FAVORITE + ", " + Columns.FORECAST_DATETIME + " )";

    /**
     * Content Provider related
     */
    //Uri definition: scheme://authority/table/path/query
    public static final Uri URI = BaseWeatherContract.URI
            .buildUpon()
            .appendPath(TABLE)
            .build();

    //uri matcher and patterns - defined in WeatherContentProvider

    //types
    //content type: multiple items returned
    static final String CONTENT_TYPE = BaseWeatherContract.CONTENT_TYPE + "/" + TABLE;

    //item type: specific item returned
    static final String CONTENT_ITEM_TYPE = BaseWeatherContract.CONTENT_ITEM_TYPE + "/" + TABLE;

    //methods - defined in WeatherContentProvider

    //manifest - defined in AndroidManifest.xml
}

