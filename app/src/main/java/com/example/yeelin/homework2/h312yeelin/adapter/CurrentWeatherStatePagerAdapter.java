package com.example.yeelin.homework2.h312yeelin.adapter;

import android.database.Cursor;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.fragment.CurrentWeatherAndDailyForecastFragment;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

/**
 * Created by ninjakiki on 4/26/15.
 */
public class CurrentWeatherStatePagerAdapter
        extends FragmentStatePagerAdapter {
    //logcat
    private static final String TAG = CurrentWeatherStatePagerAdapter.class.getCanonicalName();

    //for loader initialization
    public static final String[] PROJECTION_CURRENT_WEATHER = new String[] {
            CurrentWeatherContract.Columns.CITY_ID,
            CurrentWeatherContract.Columns.CITY_NAME,
            CurrentWeatherContract.Columns.DESCRIPTION,
            CurrentWeatherContract.Columns.TEMPERATURE,
            CurrentWeatherContract.Columns.HUMIDITY,
            CurrentWeatherContract.Columns.WIND_SPEED,
            CurrentWeatherContract.Columns.ICON,
            CurrentWeatherContract.Columns.TIMESTAMP,
            CurrentWeatherContract.Columns.USER_FAVORITE
    };

    //for retrieving data from cursor
    public enum CurrentWeatherCursorPosition {
        CITY_ID_POS(0),
        CITY_NAME_POS(1),
        DESCRIPTION_POS(2),
        TEMP_POS(3),
        HUMIDITY_POS(4),
        WINDSPEED_POS(5),
        ICON_POS(6),
        TIMESTAMP_POS(7),
        USER_FAVORITE(8);

        private int value;
        CurrentWeatherCursorPosition(int value) {
            this.value = value;
        }
        public int getValue() {
            return value;
        }
    }

    //member variables
    private Cursor cursor;

    /**
     * Constructor that takes a cursor
     * @param fm
     * @param cursor
     */
    public CurrentWeatherStatePagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);

        this.cursor = cursor;
    }

    /**
     * Returns the fragment to be placed in the view pager
     * @param position
     * @return
     */
    @Nullable
    @Override
    public Fragment getItem(int position) {
        if (cursor == null) {
            return null;
        }

        cursor.moveToPosition(position);

        //read values from the cursor
        long cityId = cursor.getLong(CurrentWeatherCursorPosition.CITY_ID_POS.getValue());
        String cityName = cursor.getString(CurrentWeatherCursorPosition.CITY_NAME_POS.getValue());
        String description = cursor.getString(CurrentWeatherCursorPosition.DESCRIPTION_POS.getValue());
        double temp = cursor.getDouble(CurrentWeatherCursorPosition.TEMP_POS.getValue());
        double humidity = cursor.getDouble(CurrentWeatherCursorPosition.HUMIDITY_POS.getValue());
        double windSpeed = cursor.getDouble(CurrentWeatherCursorPosition.WINDSPEED_POS.getValue());
        String iconName = cursor.getString(CurrentWeatherCursorPosition.ICON_POS.getValue());
        long lastUpdateMillis = cursor.getLong(CurrentWeatherCursorPosition.TIMESTAMP_POS.getValue());

        //instantiate a new fragment
        return CurrentWeatherAndDailyForecastFragment.newInstance(
                cityId,
                cityName,
                description,
                temp,
                humidity,
                windSpeed,
                iconName,
                lastUpdateMillis);
    }

    /**
     * Returns the count of items in the cursor.
     * @return
     */
    @Override
    public int getCount() {
        if (cursor == null) {
            return 0;
        }
        return cursor.getCount();
    }

    /**
     * This method is called after notifyDataSetChanged.
     * Once you call notifyDataSetChanged method what you’ll normally see is that screens that were previously visited still
     * have the old Fragment instances but screens being visited for the first time have the new Fragment instances.
     * If you have a large number of screens and scroll back and forth between them you may see some of the older screens
     * eventually show a new Fragment instance.
     *
     * What’s happening is that FragmentStatePagerAdapter is trying to be efficient and only create new Fragment instances
     * when necessary. To determine when to request new Fragment instances after a call to the notifyDataSetChanged method,
     * FragmentStatePagerAdapter calls its getItemPosition method to see if an existing Fragment can be used in its current
     * or possibly a different position without having to recreate it. What we have to do is tell the FragmentStatePagerAdapter
     * instance that we don’t want to use the existing Fragment instance.
     * Solution from: https://hedgehogjim.wordpress.com/2013/10/03/android-updatable-swipe-navigation-with-fragmentstatepageradapter/
     *
     * @param object
     * @return
     */
    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    /**
     * Swaps the current cursor with the new one and notifies listeners that data have changed.
     * Causes the view to be refreshed.
     * @param newCursor
     */
    public void swapCursor(@Nullable Cursor newCursor) {
        //if it's the same cursor, do nothing
        if (cursor == newCursor) {
            return;
        }
        cursor = newCursor;

        if (cursor != null) {
            Log.d(TAG, "swapCursor: Cursor is not null. Calling notifyDataSetChanged");
            notifyDataSetChanged();
        }
        else {
            //the only time cursor is null is when the loader is shutting down
            //so no need to notify dataset changed as we don't want getItemPosition to be called
            Log.d(TAG, "swapCursor: Cursor is null. Not calling notifyDataSetChanged");
        }
    }

    public Cursor getCursor() {
        return cursor;
    }
}
