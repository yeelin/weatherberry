package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragment.TriHourForecastFragment;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/13/15.
 */
public class TriHourForecastActivity
        extends ActionBarActivity {

    //logcat
    private static final String TAG = TriHourForecastActivity.class.getCanonicalName();

    //intent extras
    private static final String EXTRA_CITY_ID = TriHourForecastActivity.class.getSimpleName() + ".cityId";
    private static final String EXTRA_CITY_NAME = TriHourForecastActivity.class.getSimpleName() + ".cityName";
    private static final String EXTRA_FORECAST_MILLIS = TriHourForecastActivity.class.getSimpleName() + ".forecastMillis";

    /**
     * Builds the appropriate intent for starting this activity.
     * @param context
     * @param cityId
     * @param cityName
     * @return
     */
    public static Intent buildIntent(Context context, long cityId, String cityName, long forecastMillis) {
        Intent intent = new Intent(context, TriHourForecastActivity.class);

        intent.putExtra(EXTRA_CITY_ID, cityId);
        intent.putExtra(EXTRA_CITY_NAME, cityName);
        intent.putExtra(EXTRA_FORECAST_MILLIS, forecastMillis);

        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tri_hour_forecast);

        //read extras from intent
        Intent intent = getIntent();
        long cityId = intent.getLongExtra(EXTRA_CITY_ID, BaseWeatherContract.NO_ID);
        String cityName = intent.getStringExtra(EXTRA_CITY_NAME);
        long forecastMillis = intent.getLongExtra(EXTRA_FORECAST_MILLIS, 0);

        //setup toolbar
        setupToolbar(cityName, forecastMillis);

        //check if triHour forecast fragment is there, if not instantiate it
        TriHourForecastFragment fragment = (TriHourForecastFragment) getSupportFragmentManager().findFragmentById(R.id.triHourForecast_fragmentContainer);
        if (fragment == null) {
            fragment = TriHourForecastFragment.newInstance(cityId, forecastMillis);

            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.triHourForecast_fragmentContainer, fragment)
                    .commit();
        }
    }

    /**
     * Helper method to setup toolbar
     */
    private void setupToolbar(@Nullable String cityName, long forecastMillis) {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

        //enable the Up arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //set title to city name
        if (cityName != null) {
            getSupportActionBar().setTitle(cityName);
        }
        else {
            getSupportActionBar().setTitle(getString(R.string.no_city_name));
        }

        //format the day of the week
        Date forecastDate = new Date(forecastMillis);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.US); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        String dayOfWeek = simpleDateFormat.format(forecastDate);

        //set subtitle to day of week and date
        String subtitle = getString(
                R.string.tri_hour_forecast_toolbar_subtitle,
                dayOfWeek,
                DateFormat.getMediumDateFormat(this).format(forecastDate));
        getSupportActionBar().setSubtitle(subtitle);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(TAG, "Up button clicked");
                navigateUpToParentActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Provides Up navigation the proper way :)
     *
     * Clear top : if the activity being launched is already in the current task, then instead of launching a new instance,
     * all activities on top of it will be closed, and this intent will be delivered to the old activity as a new intent
     * and will be either finished and recreated OR restarted.
     *
     * Single top: if set, the activity will not be recreated if it is already at the top of the stack.
     */
    private void navigateUpToParentActivity() {
        //get the intent that started the parent activity
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NavUtils.navigateUpTo(this, intent);
    }
}
