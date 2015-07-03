package com.example.yeelin.homework.weatherberry.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by ninjakiki on 4/17/15.
 */
public final class BaseWeatherContract {
    /**
     * JsonReader common fields
     */
    public interface Json {
        public String CITY_OBJECT = "city";
        public String CITY_ID = "id";

        public String MAIN_OBJECT = "main";
        public String TEMPERATURE = "temp";
        public String HUMIDITY = "humidity";

        public String WEATHER_ARRAY = "weather";
        public String SUMMARY = "main"; //weather summary string
        public String DESCRIPTION = "description"; //more descriptive string
        public String ICON = "icon"; //name of the icon
    }

    /**
     * Database common columns
     */
    public interface Columns extends BaseColumns {
        //_ID provided by base columns
        public String CITY_ID = "city_id";

        public String TEMPERATURE = "temperature";
        public String HUMIDITY = "humidity";

        public String SUMMARY = "summary"; //weather summary string
        public String DESCRIPTION = "description"; //more descriptive string
        public String ICON = "icon"; //name of the icon
    }

    //helper strings for drop table
    static String dropTable (String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    //helper strings for selection and selectionArgs
    public static String whereClauseEquals(String columnName) {
        return columnName + "=?";
    }
    public static String whereClauseEquals(String column1, String column2) {
        return column1 + "=?" + " AND " + column2 + "=?";
    }

    public static String whereClauseLessThan(String columnName) {
        return columnName + "<?";
    }

    public static String whereClauseBetween(String columnName) {
        return columnName + " BETWEEN ? AND ?";
    }

    public static String[] whereArgs(long i, long j) {
        return new String[] { String.valueOf(i), String.valueOf(j) };
    }

    public static String[] whereArgs(long i) {
        return new String[] {String.valueOf(i)};
    }

    /**
     * Content Provider related
     */
    //Uri definition: scheme://authority/
    public static final String AUTHORITY = WeatherContentProvider.class.getCanonicalName();
    public static final Uri URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(BaseWeatherContract.AUTHORITY)
            .build();

    //types
    //content type: multiple items returned
    static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
            AUTHORITY;

    //item type: specific item returned
    static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
            AUTHORITY;

    /**
     * Used by Content Provider and Loader
     */
    public static String PATH_CITYID = "cityId";
    public static String PATH_TABLE = "*";
    public static String PATH_TABLE_ROWID = PATH_TABLE + "/#";
    public static String PATH_TABLE_CITYID = PATH_TABLE + "/" + PATH_CITYID + "/#";

    /**
     * Definition of no_id. Used by WeatherOverviewFragment
     */
    public static final int NO_ID = -1;
}
