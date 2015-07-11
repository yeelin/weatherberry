package com.example.yeelin.homework.weatherberry.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.yeelin.homework.weatherberry.BuildConfig;
import com.example.yeelin.homework.weatherberry.R;
import com.example.yeelin.homework.weatherberry.adapter.CurrentWeatherStatePagerAdapter;
import com.example.yeelin.homework.weatherberry.fragment.CurrentWeatherAndDailyForecastFragment;
import com.example.yeelin.homework.weatherberry.fragment.LocationFragment;
import com.example.yeelin.homework.weatherberry.loader.CurrentWeatherLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.loader.LoaderIds;
import com.example.yeelin.homework.weatherberry.networkUtils.AlarmUtils;
import com.example.yeelin.homework.weatherberry.networkUtils.JobUtils;
import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;
import com.example.yeelin.homework.weatherberry.receiver.FavoritesBroadcastReceiver;
import com.example.yeelin.homework.weatherberry.service.NetworkIntentService;

import java.util.Date;

/**
 * Created by ninjakiki on 4/26/15.
 */
public class CurrentWeatherAndDailyForecastPagerActivity
        extends BasePlayServicesActivity
        implements CurrentWeatherAndDailyForecastFragment.CurrentWeatherAndDailyForecastFragmentListener,
        CurrentWeatherLoaderCallbacks.CurrentWeatherLoaderListener,
        ViewPager.OnPageChangeListener,
        FavoritesBroadcastReceiver.FavoritesListener {

    //logcat
    private static final String TAG = CurrentWeatherAndDailyForecastPagerActivity.class.getCanonicalName();

    //tag for location fragment
    private static final String TAG_LOCATION_FRAGMENT = CurrentWeatherAndDailyForecastPagerActivity.class.getSimpleName() + ".locationFragment";

    //extras for savedInstanceState
    private static final String EXTRA_PAGER_POSITION = CurrentWeatherAndDailyForecastPagerActivity.class.getSimpleName() + ".pagerPosition";
    private static final String EXTRA_HAS_SCHEDULED_PERIODIC_BG_FETCH = CurrentWeatherAndDailyForecastPagerActivity.class.getSimpleName() + ".scheduledPeriodicBgFetch";

    //UI
    private ViewPager viewPager;
    private Snackbar snackbar;

    //these member variables need to be saved into savedInstanceState
    private int viewPagerPosition = 0;
    private boolean hasScheduledPeriodicBgFetch = false;

    //broadcast receiver for favorite adds
    private FavoritesBroadcastReceiver favoritesBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        //turn on strict mode in debug builds
        //enable the recommended StrictMode defaults, with violations just being logged.
        //This catches disk and network access on the main thread, as well as leaked SQLite cursors and unclosed resources.
        if (BuildConfig.DEBUG) {
            StrictMode.enableDefaults();
        }

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
        viewPager.addOnPageChangeListener(this);
        Log.d(TAG, "onCreate: Setting current pager position:" + viewPagerPosition);
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
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_add:
                //startActivityForResult(SearchActivity.buildIntent(this), SearchActivity.REQUEST_CODE_SEARCH);
                startActivity(SearchActivity.buildIntent(this));
                return true;
            case R.id.action_remove:
                startActivity(FavoritesActivity.buildIntent(this));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Register for favorite add (success and failure) broadcasts
     */
    @Override
    protected void onResume() {
        super.onResume();
        favoritesBroadcastReceiver = new FavoritesBroadcastReceiver(this, this);
    }

    /**
     * Save the view pager position and boolean indicating whether we scheduled bg fetching
     * in case of rotation or backgrounding.
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
     * Restore the view pager position and boolean indicating whether we scheduled bg fetching
     * in case of rotation or coming back from backgrounding.
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
     * Unregister for favorite add (success and failure) broadcasts
     */
    @Override
    protected void onPause() {
        favoritesBroadcastReceiver.unregister();
        super.onPause();
    }

    /**
     * Remove a listener that was previously added via addOnPageChangeListener(OnPageChangeListener)
     */
    @Override
    protected void onDestroy() {
        viewPager.removeOnPageChangeListener(this);
        super.onDestroy();
    }

    /**
     * Helper method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        //check if action bar is null
        if (getSupportActionBar() == null) {
            Log.e(TAG, "setupToolbar: getSupportActionBar is null");
            return;
        }

        //set elevation
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
     * Method implementation for CurrentWeatherAndDailyForecastFragmentListener interface
     * @param cityId
     * @param cityName
     * @param userFavorite
     * @param forecastMillis this is the time of the daily forecast that was clicked on
     */
    @Override
    public void onDailyForecastItemClick(long cityId, String cityName, boolean userFavorite, long forecastMillis) {
        Intent intent = TriHourForecastActivity.buildIntent(this, cityId, cityName, userFavorite, forecastMillis);
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

//            if (justReturnedFromSearch) {
//                Log.d(TAG, "onLoadComplete: Just returned from search is true");
//                int pos = 0;
//                if(cursor.moveToFirst()) {
//                    do {
//                        String cityName = cursor.getString(CurrentWeatherStatePagerAdapter.CurrentWeatherCursorPosition.CITY_NAME_POS.getValue());
//                        Log.d(TAG, "onLoadComplete: JustSelected:" + justSelectedCityName + ", CityName:" + cityName);
//
//                        if (justSelectedCityName.equalsIgnoreCase(cityName)) {
//                            viewPagerPosition = pos;
//                            Log.d(TAG, "onLoadComplete: Found the new pager position:" + pos);
//
//                            //reseting the member variables since they are no longer needed for this search
//                            justSelectedCityName = null;
//                            justReturnedFromSearch = false;
//                            break;
//                        }
//                        ++pos;
//                    } while (cursor.moveToNext());
//                }
//            }

            //viewPager.setCurrentItem(viewPagerPosition, false);
            onPageSelected(viewPagerPosition);

        }
        else {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
        }
    }

    /**
     * Method implementation for ViewPager.OnPageChangeListener interface
     * This is the callback when a page in the viewpager is selected.
     * @param position
     */
    @Override
    public void onPageSelected(int position) {
        Log.d(TAG, "onPageSelected: Position:" + position);

        //update the current view pager position
        viewPagerPosition = position;

        if (getSupportActionBar() == null) {
            Log.d(TAG, "onPageSelected: Toolbar is null so nothing to do");
            return;
        }

        //set toolbar title
        CurrentWeatherStatePagerAdapter currentWeatherStatePagerAdapter = (CurrentWeatherStatePagerAdapter) viewPager.getAdapter();
        Cursor cursor = currentWeatherStatePagerAdapter.getCursor();
        String title;
        if (cursor == null || cursor.getCount() == 0) {
            //cursor is null or empty, set title to default string
            title = getString(R.string.title_current_weather_activity);
        }
        else {
            //cursor is not empty
            cursor.moveToPosition(position);
            String cityName = cursor.getString(CurrentWeatherStatePagerAdapter.CurrentWeatherCursorPosition.CITY_NAME_POS.getValue());
            int userFavorite = cursor.getInt(CurrentWeatherStatePagerAdapter.CurrentWeatherCursorPosition.USER_FAVORITE.getValue());
            if (cityName != null) {
                if (userFavorite == BaseWeatherContract.USER_FAVORITE_NO)
                    title = getString(R.string.current_weather_toolbar_title, cityName);
                else
                    title = cityName;
            }
            else {
                title = getString(R.string.no_city_name);
            }
        }
        getSupportActionBar().setTitle(title);

        //set toolbar subtitle to current time and date
        Date currentDate = new Date(System.currentTimeMillis());
        String subtitle = getString(
                R.string.current_weather_toolbar_subtitle,
                DateFormat.getMediumDateFormat(this).format(currentDate),
                DateFormat.getTimeFormat(this).format(currentDate));
        getSupportActionBar().setSubtitle(subtitle);
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

//            case SearchActivity.REQUEST_CODE_SEARCH:
//                if (resultCode == RESULT_OK) {
//                    String cityName = data.getStringExtra(SearchActivity.EXTRA_CITY_NAME);
//                    justReturnedFromSearch = true;
//                    justSelectedCityName = cityName;
//                }
//                break;

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
     * FavoritesBroadcastReceiver.FavoritesListener implementation
     * @param cityName
     * @param cityId
     * @param position
     */
    @Override
    public void onFavoriteAddSuccess(final String cityName, final long cityId, final int position) {
        Log.d(TAG, String.format("onFavoriteAddSuccess: cityName:%s, cityId:%d, position:%d", cityName, cityId, position));

        //create a snackbar to inform the user
        String message = String.format("%s has been added", cityName);
        snackbar = Snackbar.make(findViewById(R.id.pager_root_layout), message, Snackbar.LENGTH_LONG);
        if (position != -1) { //-1 means something isn't quite right
            snackbar.setAction("View", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, String.format("onClick: User has requested to view cityName:%s, cityId:%d, position:%d", cityName, cityId, position));
                    viewPagerPosition = position;
                    viewPager.setCurrentItem(viewPagerPosition);
                }
            });
        }
        snackbar.show();
    }

    /**
     * FavoritesBroadcastReceiver.FavoritesListener implementation
     * @param cityName
     */
    @Override
    public void onFavoriteAddFailure(String cityName) {
        Log.d(TAG, String.format("onFavoriteAddFailure: cityName:%s", cityName));

        //create a snackbar to inform the user
        String message = String.format("Failed to add %s", cityName);
        snackbar = Snackbar.make(findViewById(R.id.pager_root_layout), message, Snackbar.LENGTH_LONG);
    }
}
