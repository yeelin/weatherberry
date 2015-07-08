package com.example.yeelin.homework.weatherberry.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.yeelin.homework.weatherberry.R;
import com.example.yeelin.homework.weatherberry.adapter.DailyForecastAdapter;
import com.example.yeelin.homework.weatherberry.fragmentUtils.AnimationUtils;
import com.example.yeelin.homework.weatherberry.fragmentUtils.FormatUtils;
import com.example.yeelin.homework.weatherberry.loader.BaseWeatherLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.loader.CurrentWeatherLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.loader.DailyForecastLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.loader.LoaderIds;
import com.example.yeelin.homework.weatherberry.networkUtils.ImageUtils;
import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/9/15.
 */
public class CurrentWeatherAndDailyForecastFragment
        extends Fragment
        implements CurrentWeatherLoaderCallbacks.CurrentWeatherLoaderListener,
        DailyForecastLoaderCallbacks.DailyForecastLoaderListener, AdapterView.OnItemClickListener {
    //logcat
    private static final String TAG = CurrentWeatherAndDailyForecastFragment.class.getCanonicalName();
    //bundle args
    private static final String ARG_CITY_ID = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".cityId";
    private static final String ARG_CITY_NAME = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".cityName";
    private static final String ARG_DESCRIPTION = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".description";
    private static final String ARG_TEMPERATURE = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".temperature";
    private static final String ARG_HUMIDITY = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".humidity";
    private static final String ARG_WIND_SPEED = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".windSpeed";
    private static final String ARG_ICON = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".icon";
    private static final String ARG_TIMESTAMP = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".timestamp";
    private static final String ARG_PAGER_POSITION = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".positionInPager";

    //saved instance state
    private static final String STATE_PAGER_POSITION = CurrentWeatherAndDailyForecastFragment.class.getSimpleName() + ".positionInPager";

    //member variables
    private long cityId = BaseWeatherContract.NO_ID;
    private String cityName;
    private String description;
    private double temp;
    private double humidity;
    private double windSpeed;
    private String iconName;
    private long lastUpdateMillis;
    private int positionInPager;

    //listener member variable
    private CurrentWeatherAndDailyForecastFragmentListener listener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface CurrentWeatherAndDailyForecastFragmentListener {
        //public void onCurrentWeatherLoadComplete(String cityName);
        public void onDailyForecastItemClick(long cityId, String cityName, long forecastMillis);
    }

    /**
     * Creates a new instance of WeatherOverviewFragment.
     *
     * @deprecated
     * @return
     */
    public static CurrentWeatherAndDailyForecastFragment newInstance() {
        Bundle args = new Bundle();

        CurrentWeatherAndDailyForecastFragment fragment = new CurrentWeatherAndDailyForecastFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Creates a new instance of WeatherOverviewFragment.
     * Hosting activity should use this instead of constructor.
     *
     * @param cityId
     * @param cityName
     * @param description
     * @param temp
     * @param humidity
     * @param windSpeed
     * @param iconName
     * @param lastUpdateMillis
     * @param positionInPager
     * @return
     */
    public static CurrentWeatherAndDailyForecastFragment newInstance(long cityId,
                                                                     String cityName,
                                                                     String description,
                                                                     double temp,
                                                                     double humidity,
                                                                     double windSpeed,
                                                                     String iconName,
                                                                     long lastUpdateMillis,
                                                                     int positionInPager) {
        Bundle args = new Bundle();
        args.putLong(ARG_CITY_ID, cityId);
        args.putString(ARG_CITY_NAME, cityName);
        args.putString(ARG_DESCRIPTION, description);
        args.putDouble(ARG_TEMPERATURE, temp);
        args.putDouble(ARG_HUMIDITY, humidity);
        args.putDouble(ARG_WIND_SPEED, windSpeed);
        args.putString(ARG_ICON, iconName);
        args.putLong(ARG_TIMESTAMP, lastUpdateMillis);
        args.putInt(ARG_PAGER_POSITION, positionInPager);

        CurrentWeatherAndDailyForecastFragment fragment = new CurrentWeatherAndDailyForecastFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Required empty public constructor.
     */
    public CurrentWeatherAndDailyForecastFragment() {

    }

    /**
     * Returns current position in the view pager.
     * @return
     */
    public int getPositionInPager() { return positionInPager; }

    /**
     * Setter for current position in the view pager
     * @param newPosition
     */
    public void setPositionInPager(int newPosition) {
        positionInPager = newPosition;
    }

    /**
     * Returns the cityId of this fragment
     * @return
     */
    public long getCityId() { return cityId; }

    /**
     * Returns the city name of this fragment.
     * @return
     */
    public String getCityName() { return cityName; }

    /**
     * Make sure the hosting activity implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            listener = (CurrentWeatherAndDailyForecastFragmentListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.getClass().getSimpleName()
                    + " must implement CurrentWeatherAndDailyForecastFragmentListener");
        }
    }

    /**
     * Configure the fragment here
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //read args
        Bundle args = getArguments();
        if (args != null) {
            cityId = args.getLong(ARG_CITY_ID, BaseWeatherContract.NO_ID);
            cityName = args.getString(ARG_CITY_NAME, getString(R.string.no_city_name));
            description = args.getString(ARG_DESCRIPTION);
            temp = args.getDouble(ARG_TEMPERATURE, 0.0);
            humidity = args.getDouble(ARG_HUMIDITY, 0.0);
            windSpeed = args.getDouble(ARG_WIND_SPEED, 0.0);
            iconName = args.getString(ARG_ICON, "");
            lastUpdateMillis = args.getLong(ARG_TIMESTAMP, 0);
            positionInPager = args.getInt(ARG_PAGER_POSITION, 0);
        }

        //read saved instance state
        if (savedInstanceState != null) {
            if (positionInPager != savedInstanceState.getInt(STATE_PAGER_POSITION)) {
                Log.d(TAG, String.format("onCreate: Bundle args positionInPager:%d != Saved positionInPager:%d", positionInPager, savedInstanceState.getInt(STATE_PAGER_POSITION)));
            }
            positionInPager = savedInstanceState.getInt(STATE_PAGER_POSITION);
        }
    }

    /**
     * Inflate layout here
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_current_weather_and_daily_forecast, container, false);
    }

    /**
     * Configure fragment's view here
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set the view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //update the view for current weather
        updateCurrentWeatherView(viewHolder);

        //set up the listview adapter
        viewHolder.dailyForecastListView.setAdapter(new DailyForecastAdapter(view.getContext()));
        viewHolder.dailyForecastListView.setOnItemClickListener(this);

        //initially make the list container invisible and show the progress bar
        viewHolder.dailyForecastListContainer.setVisibility(View.GONE);
        viewHolder.dailyForecastProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Initialize the daily forecast loader
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initialize the daily forecast loader
        Log.d(TAG, "onActivityCreated: Init daily forecast loader");
        DailyForecastLoaderCallbacks.initLoader(
                getActivity(),
                getLoaderManager(),
                this,
                DailyForecastAdapter.PROJECTION_DAILY_FORECAST,
                cityId,
                BaseWeatherLoaderCallbacks.IdType.CITY_ID);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);

        //read saved instance state
        if (savedInstanceState != null) {
            positionInPager = savedInstanceState.getInt(STATE_PAGER_POSITION);
        }
    }

    /**
     * Save out the position in pager so that we can reconstruct later
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGER_POSITION, positionInPager);
        Log.d(TAG, String.format("onSaveInstanceState: Pager position:%d, CityName:%s", positionInPager, cityName));
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * Loader callback with an updated cursor.
     * Update the views.
     *
     * @param loaderId
     * @param cursor
     */
    @Override
    public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor) {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onLoadComplete: View holder is null, so nothing to do.");
            return;
        }

        if (loaderId == LoaderIds.DAILY_FORECAST_LOADER) {
            DailyForecastAdapter dailyForecastAdapter = (DailyForecastAdapter) viewHolder.dailyForecastListView.getAdapter();
            dailyForecastAdapter.swapCursor(cursor);

            //show the list container and hide the progress bar
            if (viewHolder.dailyForecastListContainer.getVisibility() != View.VISIBLE) {
                Log.d(TAG, String.format("onLoadComplete: Daily list container is not visible, so animating to make visible. City name:%s", cityName));
                AnimationUtils.crossFadeViews(getActivity(),
                        viewHolder.dailyForecastListContainer,
                        viewHolder.dailyForecastProgressBar);
            }
            else {
                Log.d(TAG, String.format("onLoadComplete: Daily list container is visible, so not doing any animation. City name:%s", cityName));
            }
        }
        else {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
        }
    }

    /**
     * List view's click callback
     *
     * @param parent the listview
     * @param view the view that was clicked
     * @param position position in the list view
     * @param id row id in the database
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //parent is the listview
        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        //get the forecast time from the item that was clicked on
        long forecastMillis = cursor.getLong(DailyForecastAdapter.DailyForecastCursorPosition.FORECAST_DATETIME_POS.getValue());
        //for logging purposes only
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm Z", Locale.US); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        String forecastFormatted = simpleDateFormat.format(new Date(forecastMillis));
        Log.d(TAG, String.format("onItemClick: Position:%d Forecast date:%s", position, forecastFormatted));

        //pass city id, city name, and forecast time to listener
        listener.onDailyForecastItemClick(cityId, cityName, forecastMillis);
    }

    /**
     * Helper method to update the view
     * @param viewHolder
     */
    private void updateCurrentWeatherView(@NonNull ViewHolder viewHolder) {
        //format as necessary
        Date lastUpdateDate = new Date(lastUpdateMillis);
        String lastUpdateInUserFormat = DateFormat.getMediumDateFormat(getActivity()).format(lastUpdateDate) +
                " " + DateFormat.getTimeFormat(getActivity()).format(lastUpdateDate);

        viewHolder.currentDescription.setText(FormatUtils.formatDescription(description));
        viewHolder.currentTemp.setText(String.valueOf(Math.round(temp)));
        viewHolder.currentHumidity.setText(String.valueOf(Math.round(humidity)) + " " + getString(R.string.humidity_unit));
        viewHolder.currentWindSpeed.setText(getString(R.string.current_windspeed_value, String.valueOf(Math.round(windSpeed))));
        viewHolder.currentLastUpdate.setText(getString(R.string.last_update_value, lastUpdateInUserFormat));

        //load the image using picasso
        ImageUtils.loadImage(getActivity(), iconName, viewHolder.currentIcon);

        Log.d(TAG, String.format("updateCurrentWeatherView: Updating views with cityName:%s, description:%s, temp:%f, humidity:%f, windspeed:%f lastUpdate:%s",
                cityName, description, temp, humidity, windSpeed, lastUpdateInUserFormat));
    }

    /**
     * Returns the fragment view's view holder if it exists, or null.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder
     */
    private class ViewHolder {
        //member variables
        final TextView currentDescription;
        final TextView currentTemp;
        final TextView currentHumidity;
        final TextView currentWindSpeed;
        final ImageView currentIcon;

        final ListView dailyForecastListView;
        final TextView currentLastUpdate;

        final View dailyForecastListContainer;
        final View dailyForecastProgressBar;

        ViewHolder(View view) {
            currentDescription = (TextView) view.findViewById(R.id.current_description);
            currentTemp = (TextView) view.findViewById(R.id.current_temp);
            currentHumidity = (TextView) view.findViewById(R.id.current_humidity);
            currentWindSpeed = (TextView) view.findViewById(R.id.current_wind_speed);
            currentIcon = (ImageView) view.findViewById(R.id.current_image);

            dailyForecastListView = (ListView) view.findViewById(R.id.daily_forecast_listview);
            dailyForecastListView.setEmptyView(view.findViewById(R.id.daily_forecast_empty));
            dailyForecastListContainer = view.findViewById(R.id.daily_forecast_listContainer);
            dailyForecastProgressBar = view.findViewById(R.id.daily_forecast_progressBar);

            currentLastUpdate = (TextView) view.findViewById(R.id.current_last_update);
        }
    }
}
