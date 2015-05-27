package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.adapter.CurrentWeatherStatePagerAdapter;
import com.example.yeelin.homework2.h312yeelin.fragment.CurrentWeatherAndDailyForecastFragment;
import com.example.yeelin.homework2.h312yeelin.fragment.LocationFragment;
import com.example.yeelin.homework2.h312yeelin.loader.CurrentWeatherLoaderCallbacks;
import com.example.yeelin.homework2.h312yeelin.loader.LoaderIds;
import com.example.yeelin.homework2.h312yeelin.networkUtils.AlarmUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.JobUtils;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;
import com.example.yeelin.homework2.h312yeelin.service.NetworkIntentService;

import java.util.Date;

/**
 * Created by ninjakiki on 4/26/15.
 */
public class CurrentWeatherAndDailyForecastPagerActivity
        extends BasePlayServicesActivity
        implements CurrentWeatherAndDailyForecastFragment.CurrentWeatherAndDailyForecastFragmentListener,
        CurrentWeatherLoaderCallbacks.CurrentWeatherLoaderListener,
        ViewPager.OnPageChangeListener,
        LocationFragment.LocationFragmentListener {
    //logcat
    private static final String TAG = CurrentWeatherAndDailyForecastPagerActivity.class.getCanonicalName();

    //tag for location fragment
    private static final String TAG_LOCATION_FRAGMENT = CurrentWeatherAndDailyForecastActivity.class.getSimpleName() + ".locationFragment";

    //extras for savedInstanceState
    private static final String EXTRA_PAGER_POSITION = CurrentWeatherAndDailyForecastPagerActivity.class.getSimpleName() + ".pagerPosition";
    private static final String EXTRA_HAS_SCHEDULED_PERIODIC_BG_FETCH = CurrentWeatherAndDailyForecastPagerActivity.class.getSimpleName() + ".scheduledPeriodicBgFetch";

    private ViewPager viewPager;

    //these member variables need to be saved into savedInstanceState
    private int viewPagerPosition = 0;
    private boolean hasScheduledPeriodicBgFetch = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pager_current_weather_and_daily_forecast);

        //setupToolbar
        setupToolbar();


        if (savedInstanceState == null) {
            //check if we already have the location fragment
            Fragment locationFragment = getSupportFragmentManager().findFragmentByTag(TAG_LOCATION_FRAGMENT);
            if (locationFragment == null) {
                Log.d(TAG, "onCreate: Creating a location fragment");
                //create a location fragment
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(LocationFragment.newInstance(), TAG_LOCATION_FRAGMENT)
                        .commit();
            }
        }
        else {
            //read saved instance state
            viewPagerPosition = savedInstanceState.getInt(EXTRA_PAGER_POSITION, 0);
            hasScheduledPeriodicBgFetch = savedInstanceState.getBoolean(EXTRA_HAS_SCHEDULED_PERIODIC_BG_FETCH, false);

            Log.d(TAG, String.format("onCreate: Saved instance state is not null. Pager position:%d, Periodic background fetch:%s",
                    viewPagerPosition, hasScheduledPeriodicBgFetch));
        }

        //setup View Pager
        viewPager = (ViewPager) findViewById(R.id.currentWeatherAndDailyForecast_viewPager);
        viewPager.setAdapter(new CurrentWeatherStatePagerAdapter(getSupportFragmentManager(), null));
        viewPager.setOnPageChangeListener(this);
        viewPager.setCurrentItem(viewPagerPosition);

        //fetch fresh data from the network
        startService(NetworkIntentService.buildIntentForMultiCityLoad(this));

        //initialize the current weather loader to load data from the database
        Log.d(TAG, "onCreate: Init current weather loader");
        CurrentWeatherLoaderCallbacks.initLoader(
                this,
                getSupportLoaderManager(),
                this,
                CurrentWeatherStatePagerAdapter.PROJECTION_CURRENT_WEATHER);

        //schedule periodic background fetching of fresh data
        if (!hasScheduledPeriodicBgFetch) {
            Log.d(TAG, "onCreate: Periodic background fetch has not been scheduled yet so scheduling now");
            schedulePeriodicBackgroundFetch();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_cities, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                startActivity(SearchActivity.buildIntent(this));
                return true;

            case R.id.action_settings:
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Save the view pager position in case of rotation or backgrounding.
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(EXTRA_PAGER_POSITION, viewPagerPosition);
        outState.putBoolean(EXTRA_HAS_SCHEDULED_PERIODIC_BG_FETCH, hasScheduledPeriodicBgFetch);

        Log.d(TAG, String.format("onSaveInstanceState: Saving Pager position:%d, Periodic background fetch:%s",
                viewPagerPosition, hasScheduledPeriodicBgFetch));
    }

    /**
     * Restore the view pager position in case of rotation or coming back from backgrounding.
     * @param savedInstanceState
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            viewPagerPosition = savedInstanceState.getInt(EXTRA_PAGER_POSITION, 0);
            hasScheduledPeriodicBgFetch = savedInstanceState.getBoolean(EXTRA_HAS_SCHEDULED_PERIODIC_BG_FETCH, false);
            Log.d(TAG, String.format("onRestoreInstanceState: Restoring Pager position:%d, Periodic background fetch:%s",
                    viewPagerPosition, hasScheduledPeriodicBgFetch));

            viewPager.setCurrentItem(viewPagerPosition);

        }
    }

    /**
     * Helper method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
    }

    /**
     * Helper method to schedule periodic background fetching and loading of data
     */
    private void schedulePeriodicBackgroundFetch() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.d(TAG, "schedulePeriodicBackgroundFetch: Using job scheduler");
            JobUtils.scheduleJob(this);
        }
        else {
            Log.d(TAG, "schedulePeriodicBackgroundFetch: Using Alarm service");
            AlarmUtils.scheduleRecurringAlarm(this);
        }

        //set this to true since we have scheduled either a job or an alarm for background fetching
        hasScheduledPeriodicBgFetch = true;
    }

    /**
     * Implementation for CurrentWeatherAndDailyForecastFragmentListener interface
     * @param cityName
     */
    //@Override
    //public void onCurrentWeatherLoadComplete(String cityName) {
        //set title to city name
//        if (cityName != null) {
//            getSupportActionBar().setTitle(cityName);
//        }
//        else {
//            getSupportActionBar().setTitle(getString(R.string.no_city_name));
//        }
//
//        //set subtitle to current time and date
//        Date currentDate = new Date(System.currentTimeMillis());
//        String subtitle = getString(
//                R.string.current_weather_toolbar_subtitle,
//                DateFormat.getMediumDateFormat(this).format(currentDate),
//                DateFormat.getTimeFormat(this).format(currentDate));
//        getSupportActionBar().setSubtitle(subtitle);
    //}

    /**
     * Method implementation for CurrentWeatherAndDailyForecastFragmentListener interface
     * @param cityId
     * @param cityName
     * @param forecastMillis this is the time of the daily forecast that was clicked on
     */
    @Override
    public void onDailyForecastItemClick(long cityId, String cityName, long forecastMillis) {
        Intent intent = TriHourForecastActivity.buildIntent(this, cityId, cityName, forecastMillis);
        startActivity(intent);
    }

    /**
     * Method implementation for CurrentWeatherLoaderListener interface
     * @param loaderId
     * @param cursor
     */
    @Override
    public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor) {
        if (loaderId == LoaderIds.CURRENT_WEATHER_LOADER) {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Cursor swapped", loaderId));
            CurrentWeatherStatePagerAdapter currentWeatherStatePagerAdapter = (CurrentWeatherStatePagerAdapter) viewPager.getAdapter();
            currentWeatherStatePagerAdapter.swapCursor(cursor);

            onPageSelected(viewPagerPosition);
        }
        else {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
        }
    }

    /**
     * Method implementation for ViewPager.OnPageChangeListener interface
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: Position:" + position);

        CurrentWeatherStatePagerAdapter currentWeatherStatePagerAdapter = (CurrentWeatherStatePagerAdapter) viewPager.getAdapter();
        Cursor cursor = currentWeatherStatePagerAdapter.getCursor();
        if (cursor == null || cursor.getCount() == 0) {
            return;
        }

        cursor.moveToPosition(position);
        String cityName = cursor.getString(CurrentWeatherStatePagerAdapter.CurrentWeatherCursorPosition.CITY_NAME_POS.getValue());
        int userFavorite = cursor.getInt(CurrentWeatherStatePagerAdapter.CurrentWeatherCursorPosition.USER_FAVORITE.getValue());
        if (cityName != null) {
            if (userFavorite == CurrentWeatherContract.USER_FAVORITE_NO)
                getSupportActionBar().setTitle(getString(R.string.current_weather_toolbar_title, cityName));
            else
                getSupportActionBar().setTitle(cityName);
        }
        else {
            getSupportActionBar().setTitle(getString(R.string.no_city_name));
        }

        //set subtitle to current time and date
        Date currentDate = new Date(System.currentTimeMillis());
        String subtitle = getString(
                R.string.current_weather_toolbar_subtitle,
                DateFormat.getMediumDateFormat(this).format(currentDate),
                DateFormat.getTimeFormat(this).format(currentDate));
        getSupportActionBar().setSubtitle(subtitle);

        //update the current view pager position
        viewPagerPosition = position;
    }

    /**
     * Method implementation for ViewPager.OnPageChangeListener interface
     * @param position
     * @param positionOffset
     * @param positionOffsetPixels
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    /**
     * Method implementation for ViewPager.OnPageChangeListener interface
     * @param state
     */
    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * Callback from Play Services dialogs. Callback can originate from:
     * 1. status.startResolutionForResult(getActivity(), REQUEST_CODE_LOCATION_SETTINGS_RESOLUTION) in LocationFragment
     * 2. PlayServicesErrorDialog (handled by super class - BasePlayServicesActivity)
     *
     * BasePlayServices has a version of this that handles just the generic PlayServices error. This one handles
     * location-specific results.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        switch (requestCode) {
            case LocationFragment.REQUEST_CODE_LOCATION_SETTINGS_RESOLUTION:
                LocationFragment locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOCATION_FRAGMENT);

                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onActivityResult: User enabled location settings");
                    locationFragment.onLocationSettingEnabled();
                }
                else {
                    Log.d(TAG, "onActivityResult: User did not enable location settings");
                    locationFragment.onLocationSettingNotEnabled();
                }
                break;

        default:
            //ask BasePlayServicesActivity to handle the rest
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * BasePlayServicesActivity override
     * Helper method. Used when no play services are available. Shows a toast.
     */
    @Override
    protected void noPlayServicesAvailable() {
        Log.d(TAG, "noPlayServicesAvailable");
        Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();

        //at this point, the app will have to continue with no current location
    }

    /**
     * BasePlayServicesActivity override
     * Helper method. Used when play services become available. Notify search fragment that play services are available
     * and to retry connection.
     */
    @Override
    protected void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable");
        LocationFragment locationFragment = (LocationFragment) getSupportFragmentManager().findFragmentByTag(TAG_LOCATION_FRAGMENT);
        if (locationFragment != null) {
            locationFragment.onPlayServicesAvailable();
        }
    }

    /**
     * Callback from LocationFragmentListener
     * Presents a toast saying that current location has been fixed and geocoded. Open weather API has not been called yet.
     * TODO: Should probably remove this in a real app
     *
     * @param location
     * @param locationName
     */
    @Override
    public void onNewLocation(@Nullable Location location, @Nullable String locationName) {
        Log.d(TAG, "onNewLocation: Location Name: " + locationName + " Location : " + location);

        if (location != null) {
            String msg = null;
            if (locationName != null) {
                msg = String.format("Current location is %s (%f, %f),", locationName, location.getLatitude(), location.getLongitude());
            }
            else {
                msg = String.format("Current location is (%f, %f),", location.getLatitude(), location.getLongitude());
            }
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        }
    }
}
