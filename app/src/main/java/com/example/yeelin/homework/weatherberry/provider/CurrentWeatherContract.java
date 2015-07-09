package com.example.yeelin.homework.weatherberry.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ninjakiki on 4/9/15.
 */
public final class CurrentWeatherContract {
    /**
     * JsonReader fields
     */
    public interface Json extends BaseWeatherContract.Json {
        //public String CITY_ID = BaseWeatherContract.Json.CITY_ID; //city id used to query api
        public String CITY_NAME = "name"; //friendly name

        public String COORD_OBJECT = "coord";
        public String CITY_LATITUDE = "lat"; //latitude
        public String CITY_LONGITUDE = "lon"; //longitude

        //public String WEATHER_ARRAY = BaseWeatherContract.Json.WEATHER_ARRAY;
        //public String SUMMARY = BaseWeatherContract.Json.SUMMARY; //weather summary string
        //public String DESCRIPTION = BaseWeatherContract.Json.DESCRIPTION; //more descriptive string

        //public String MAIN_OBJECT = BaseWeatherContract.Json.MAIN_OBJECT;
        //public String TEMPERATURE = BaseWeatherContract.Json.TEMPERATURE;
        //public String HUMIDITY = BaseWeatherContract.Json.HUMIDITY;

        public String WIND_OBJECT = "wind";
        public String WIND_SPEED = "speed";

        public String UNIT = "unit"; //imperial or metric
        public String TIMESTAMP = "dt"; //when this entry was created
    }

    /**
     * Database columns
     */
    public interface Columns extends BaseWeatherContract.Columns {
        //public String CITY_ID = BaseWeatherContract.Columns.CITY_ID; //city id used to query api
        public String CITY_NAME = "city_name"; //friendly name
        public String CITY_LATITUDE = "city_latitude"; //latitude
        public String CITY_LONGITUDE = "city_longitude"; //longitude
        //public String USER_FAVORITE = "user_favorite"; //indicates if this city was favorited by the user

        //public String SUMMARY = BaseWeatherContract.Columns.SUMMARY; //weather summary string
        //public String DESCRIPTION = BaseWeatherContract.Columns.DESCRIPTION; //more descriptive string

        //public String TEMPERATURE = BaseWeatherContract.Columns.TEMPERATURE;
        //public String HUMIDITY = BaseWeatherContract.Columns.HUMIDITY;

        public String WIND_SPEED = "wind_speed";

        public String UNIT = "unit"; //imperial or metric
        public String TIMESTAMP = "timestamp"; //when this entry was created
    }

    //table name
    static final String TABLE = "current_weather";

    //possible units for Columns.UNIT
    public static final int UNIT_IMPERIAL = 0;
    public static final int UNIT_METRIC = 1;

    /**
     * Database statements
     */
    //create table
    static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE +
            " ( " +
            BaseColumns._ID + " INTEGER PRIMARY KEY, " +
            Columns.CITY_ID + " INTEGER NOT NULL, " +
            Columns.CITY_NAME + " TEXT NOT NULL, " +
            Columns.CITY_LATITUDE + " REAL NOT NULL, " +
            Columns.CITY_LONGITUDE + " REAL NOT NULL, " +
            Columns.USER_FAVORITE + " INTEGER NOT NULL, " +
            Columns.SUMMARY + " TEXT NOT NULL, " +
            Columns.DESCRIPTION + " TEXT NOT NULL, " +
            Columns.TEMPERATURE + " REAL NOT NULL, " +
            Columns.HUMIDITY + " REAL NOT NULL, " +
            Columns.WIND_SPEED + " REAL NOT NULL, " +
            Columns.UNIT + " INTEGER NOT NULL, " +
            Columns.ICON + " TEXT NOT NULL, " +
            Columns.TIMESTAMP + " INTEGER NOT NULL" +
            " )";

    //index name
    static final String INDEX_NAME = "city_id_user_favorite_index";
    //create index
    static final String CREATE_INDEX =
            "CREATE UNIQUE INDEX IF NOT EXISTS " + INDEX_NAME +
                    " ON " + TABLE +
                    " ( " + Columns.CITY_ID + ", " + Columns.USER_FAVORITE + " )";

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
