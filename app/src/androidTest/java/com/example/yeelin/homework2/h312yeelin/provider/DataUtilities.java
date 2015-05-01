package com.example.yeelin.homework2.h312yeelin.provider;

import android.content.ContentValues;

import java.util.Calendar;

/**
 * Created by ninjakiki on 4/9/15.
 */
public class DataUtilities {
    //city ids for Seattle and New York from OpenWeather api
    static final long SEATTLE_CITY_ID = 5809844;
    static final long NEWYORK_CITY_ID = 5128638;

    static class CurrentWeather {
        /**
         * Current Weather table
         * Insert a row for Seattle
         *
         * @return
         */
        static ContentValues insertValues_Seattle() {
            ContentValues values = new ContentValues();

            //id and name
            values.put(CurrentWeatherContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            values.put(CurrentWeatherContract.Columns.CITY_NAME, "Seattle");

            //lat and long
            values.put(CurrentWeatherContract.Columns.CITY_LATITUDE, 47.61);
            values.put(CurrentWeatherContract.Columns.CITY_LONGITUDE, -122.33);

            //summary and description
            values.put(CurrentWeatherContract.Columns.SUMMARY, "Clear");
            values.put(CurrentWeatherContract.Columns.DESCRIPTION, "Sky is Clear");

            //temperature, humidity
            values.put(CurrentWeatherContract.Columns.TEMPERATURE, 58.39);
            values.put(CurrentWeatherContract.Columns.HUMIDITY, 63);

            //wind speed
            values.put(CurrentWeatherContract.Columns.WIND_SPEED, 1.68);

            //unit and timestamp
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, 1428625213);

            return values;
        }

        /**
         * Current Weather table
         * Insert a row for New York
         *
         * @return
         */
        static ContentValues insertValues_NewYork() {
            ContentValues values = new ContentValues();

            //id and name
            values.put(CurrentWeatherContract.Columns.CITY_ID, NEWYORK_CITY_ID);
            values.put(CurrentWeatherContract.Columns.CITY_NAME, "New York");

            //lat and long
            values.put(CurrentWeatherContract.Columns.CITY_LATITUDE, 43);
            values.put(CurrentWeatherContract.Columns.CITY_LONGITUDE, -75.5);

            //summary and description
            values.put(CurrentWeatherContract.Columns.SUMMARY, "Rain");
            values.put(CurrentWeatherContract.Columns.DESCRIPTION, "light rain");

            //temperature, humidity
            values.put(CurrentWeatherContract.Columns.TEMPERATURE, 44.44);
            values.put(CurrentWeatherContract.Columns.HUMIDITY, 98);

            //wind speed
            values.put(CurrentWeatherContract.Columns.WIND_SPEED, 8.55);

            //unit and timestamp
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, 1428626904);

            return values;
        }

        /**
         * Current Weather table
         * Update the row for Seattle
         * @return
         */
        static ContentValues updateValues_Seattle() {
            ContentValues values = new ContentValues();

            //id and name
            values.put(CurrentWeatherContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            values.put(CurrentWeatherContract.Columns.CITY_NAME, "Seattle");

            //lat and long
            values.put(CurrentWeatherContract.Columns.CITY_LATITUDE, 47.61);
            values.put(CurrentWeatherContract.Columns.CITY_LONGITUDE, -122.33);

            //summary and description
            values.put(CurrentWeatherContract.Columns.SUMMARY, "Clear");
            values.put(CurrentWeatherContract.Columns.DESCRIPTION, "Sky is Clear");

            //temperature, humidity
            values.put(CurrentWeatherContract.Columns.TEMPERATURE, 55.1);
            values.put(CurrentWeatherContract.Columns.HUMIDITY, 60);

            //wind speed
            values.put(CurrentWeatherContract.Columns.WIND_SPEED, 2.1);

            //unit and timestamp
            values.put(CurrentWeatherContract.Columns.UNIT, CurrentWeatherContract.UNIT_IMPERIAL);
            values.put(CurrentWeatherContract.Columns.TIMESTAMP, 1428627095);

            return values;
        }

        /**
         * Current Weather table
         * Returns Seattle and New York's values for bulk insert
         * @return
         */
        static ContentValues[] insertBulkValues() {
            ContentValues[] bulkValues = DataUtilities.insertBulkValues(
                    insertValues_Seattle(),
                    insertValues_NewYork());
            return bulkValues;
        }
    }

    static class DailyForecast {
        /**
         * Daily Forecast table
         * Insert a row for Seattle
         *
         * @return
         */
        static ContentValues insertValues_Seattle1() {
            ContentValues values = new ContentValues();

            //city id
            values.put(DailyForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(DailyForecastContract.Columns.FORECAST_DATETIME, 1429473600);
            //temperature low and high
            values.put(DailyForecastContract.Columns.TEMPERATURE_LOW, 44.19);
            values.put(DailyForecastContract.Columns.TEMPERATURE_HIGH, 65.01);

            return values;
        }

        /**
         * Daily Forecast table
         * Insert a second row for Seattle
         *
         * @return
         */
        static ContentValues insertValues_Seattle2() {
            ContentValues values = new ContentValues();

            //city id
            values.put(DailyForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(DailyForecastContract.Columns.FORECAST_DATETIME, 1429560000);
            //temperature low and high
            values.put(DailyForecastContract.Columns.TEMPERATURE_LOW, 44.4);
            values.put(DailyForecastContract.Columns.TEMPERATURE_HIGH, 67.73);

            return values;
        }

        /**
         * Daily Forecast table
         * Update a row for Seattle
         *
         * @return
         */
        static ContentValues updateValues_Seattle() {
            ContentValues values = new ContentValues();

            //city id
            values.put(DailyForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(DailyForecastContract.Columns.FORECAST_DATETIME, 1429473600);
            //temperature low and high
            values.put(DailyForecastContract.Columns.TEMPERATURE_LOW, 50.1);
            values.put(DailyForecastContract.Columns.TEMPERATURE_HIGH, 70.1);

            return values;
        }

        /**
         * Daily Forecast table
         * Returns Seattle values 1 and 2 for bulk insert
         * @return
         */
        static ContentValues[] insertBulkValues() {
            ContentValues[] bulkValues = DataUtilities.insertBulkValues(
                    insertValues_Seattle1(),
                    insertValues_Seattle2());
            return bulkValues;
        }
    }

    static class TriHourForecast {
        /**
         * Tri Hour Forecast table
         * Insert a row for Seattle
         *
         * @return
         */
        static ContentValues insertValues_Seattle1() {
            ContentValues values = new ContentValues();

            //city id
            values.put(TriHourForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(TriHourForecastContract.Columns.FORECAST_DATETIME, 1429488000);
            //temperature and description
            values.put(TriHourForecastContract.Columns.TEMPERATURE, 60.44);
            values.put(TriHourForecastContract.Columns.DESCRIPTION, "sky is clear");

            return values;
        }

        /**
         * Tri Hour Forecast table
         * Insert a second row for Seattle
         *
         * @return
         */
        static ContentValues insertValues_Seattle2() {
            ContentValues values = new ContentValues();

            //city id
            values.put(TriHourForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(TriHourForecastContract.Columns.FORECAST_DATETIME, 1429498800);
            //temperature and description
            values.put(TriHourForecastContract.Columns.TEMPERATURE, 55.2);
            values.put(TriHourForecastContract.Columns.DESCRIPTION, "sky is clear");

            return values;
        }

        /**
         * Tri Hour Forecast table
         * Update a row for Seattle
         *
         * @return
         */
        static ContentValues updateValues_Seattle() {
            ContentValues values = new ContentValues();

            //city id
            values.put(TriHourForecastContract.Columns.CITY_ID, SEATTLE_CITY_ID);
            //forecast datetime
            values.put(TriHourForecastContract.Columns.FORECAST_DATETIME, 1429488000);
            //temperature low and high
            values.put(TriHourForecastContract.Columns.TEMPERATURE, 70);
            values.put(TriHourForecastContract.Columns.DESCRIPTION, "cloudy");

            return values;
        }

        /**
         * Tri Hour Forecast table
         * Returns Seattle values 1 and 2 for bulk insert
         * @return
         */
        static ContentValues[] insertBulkValues() {
            ContentValues[] bulkValues = DataUtilities.insertBulkValues(
                    insertValues_Seattle1(),
                    insertValues_Seattle2());
            return bulkValues;
        }
    }

    /**
     * Returns 2 values for bulk insert
     * @return
     */
    static ContentValues[] insertBulkValues(ContentValues values1, ContentValues values2) {
        ContentValues[] bulkValues = new ContentValues[2];
        bulkValues[0] = values1;
        bulkValues[1] = values2;
        return bulkValues;
    }

    /**
     * Returns content values for bulk update
     * @return
     */
    static ContentValues updateBulkCurrentWeatherTimestamp() {
        ContentValues values = new ContentValues();
        Calendar date = Calendar.getInstance();
        values.put(CurrentWeatherContract.Columns.TIMESTAMP, date.getTimeInMillis());
        return values;
    }

    public static ContentValues[] mergeValues(ContentValues[] targetArray, ContentValues source) {
        for (ContentValues target : targetArray) {
            mergeValues(target, source);
        }
        return targetArray;
    }

    public static ContentValues mergeValues(ContentValues target, ContentValues source) {
        target.putAll(source);
        return target;
    }
}
