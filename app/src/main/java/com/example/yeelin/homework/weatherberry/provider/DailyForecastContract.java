package com.example.yeelin.homework.weatherberry.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ninjakiki on 4/16/15.
 */
public final class DailyForecastContract {
    /**
     * JsonReader fields
     */
    public interface Json extends BaseWeatherContract.Json {
        //public String CITY_OBJECT = BaseWeatherContract.Json.CITY_OBJECT;
        //public String CITY_ID = BaseWeatherContract.Json.CITY_ID;

        public String LIST_ARRAY = "list";
        public String FORECAST_DATETIME = "dt";

        public String TEMPERATURE_OBJECT = "temp";
        public String TEMPERATURE_LOW = "min";
        public String TEMPERATURE_HIGH = "max";

        //public String WEATHER_ARRAY = BaseWeatherContract.Json.WEATHER_ARRAY;
    }

    /**
     * Database columns
     */
    public interface Columns extends BaseWeatherContract.Columns {
        //public String CITY_ID = BaseWeatherContract.Columns.CITY_ID;
        public String FORECAST_DATETIME = "forecast_datetime";
        public String TEMPERATURE_LOW = "temperature_low";
        public String TEMPERATURE_HIGH = "temperature_high";
    }

    //table name
    static final String TABLE = "daily_forecast";

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
            Columns.TEMPERATURE_LOW + " REAL NOT NULL, " +
            Columns.TEMPERATURE_HIGH + " REAL NOT NULL, " +
            Columns.ICON + " TEXT NOT NULL" +
            " )";
    //index name
    static final String INDEX_NAME = "city_id_daily_date_index";
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
