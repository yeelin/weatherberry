package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragment.CurrentWeatherAndDailyForecastFragment;
import com.example.yeelin.homework2.h312yeelin.service.NetworkIntentService;


public class CurrentWeatherAndDailyForecastActivity
        extends AppCompatActivity
        implements CurrentWeatherAndDailyForecastFragment.CurrentWeatherAndDailyForecastFragmentListener {
    //logcat
    private static final String TAG = CurrentWeatherAndDailyForecastActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_weather_and_daily_forecast);

        //setup toolbar
        setupToolbar();

        //check if current weather and daily forecast fragment is there
        //if no, instantiate
        //if yes, do nothing
        CurrentWeatherAndDailyForecastFragment fragment = (CurrentWeatherAndDailyForecastFragment) getSupportFragmentManager().findFragmentById(R.id.currentWeatherAndDailyForecast_fragmentContainer);
        if (fragment == null) {
            //create the fragment dynamically
            fragment = CurrentWeatherAndDailyForecastFragment.newInstance();

            //add fragment to fragment manager
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.currentWeatherAndDailyForecast_fragmentContainer, fragment)
                    .commit();
        }

        //NetworkIntentService.startService(this);
        startService(NetworkIntentService.buildIntent(this));
    }

    /**
     * Helper method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));
    }

    /*
    @Override
    public void onCurrentWeatherLoadComplete(String cityName) {
        //set title to city name
        if (cityName != null) {
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

    }
    */

    /**
     *
     * @param cityId
     * @param cityName
     * @param forecastMillis this is the time of the daily forecast that was clicked on
     */
    @Override
    public void onDailyForecastItemClick(long cityId, String cityName, long forecastMillis) {
        Intent intent = TriHourForecastActivity.buildIntent(this, cityId, cityName, forecastMillis);
        startActivity(intent);
    }
}
