package com.example.yeelin.homework2.h312yeelin.fragment;

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

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.adapter.DailyForecastAdapter;
import com.example.yeelin.homework2.h312yeelin.fragmentUtils.AnimationUtils;
import com.example.yeelin.homework2.h312yeelin.fragmentUtils.FormatUtils;
import com.example.yeelin.homework2.h312yeelin.loader.BaseWeatherLoaderCallbacks;
import com.example.yeelin.homework2.h312yeelin.loader.CurrentWeatherLoaderCallbacks;
import com.example.yeelin.homework2.h312yeelin.loader.DailyForecastLoaderCallbacks;
import com.example.yeelin.homework2.h312yeelin.loader.LoaderIds;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;


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

    //for loader initialization
//    private final String[] PROJECTION_CURRENT_WEATHER = new String[] {
//        CurrentWeatherContract.Columns.CITY_ID,
//        CurrentWeatherContract.Columns.CITY_NAME,
//        CurrentWeatherContract.Columns.DESCRIPTION,
//        CurrentWeatherContract.Columns.TEMPERATURE,
//        CurrentWeatherContract.Columns.HUMIDITY,
//        CurrentWeatherContract.Columns.WIND_SPEED,
//        CurrentWeatherContract.Columns.TIMESTAMP
//    };
//
//    //for retrieving data from cursor
//    private enum CurrentWeatherCursorPosition {
//        CITY_ID_POS(0),
//        CITY_NAME_POS(1),
//        DESCRIPTION_POS(2),
//        TEMP_POS(3),
//        HUMIDITY_POS(4),
//        WINDSPEED_POS(5),
//        TIMESTAMP_POS(6);
//
//        private int value;
//        private CurrentWeatherCursorPosition(int value) {
//            this.value = value;
//        }
//
//        public int getValue() {
//            return value;
//        }
//    }

    //member variables
    private long cityId = BaseWeatherContract.NO_ID;
    private String cityName;
    private String description;
    private double temp;
    private double humidity;
    private double windSpeed;
    private String iconName;
    private long lastUpdateMillis;


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
     * Hosting activity should use this instead of constructor.
     *
     * @return
     */
    public static CurrentWeatherAndDailyForecastFragment newInstance() {
        Bundle args = new Bundle();

        CurrentWeatherAndDailyForecastFragment fragment = new CurrentWeatherAndDailyForecastFragment();
        fragment.setArguments(args);

        return fragment;
    }

    /**
     *
     * @param cityId
     * @param cityName
     * @param description
     * @param temp
     * @param humidity
     * @param windSpeed
     * @param iconName
     * @param lastUpdateMillis
     * @return
     */
    public static CurrentWeatherAndDailyForecastFragment newInstance(long cityId,
                                                                     String cityName,
                                                                     String description,
                                                                     double temp,
                                                                     double humidity,
                                                                     double windSpeed,
                                                                     String iconName,
                                                                     long lastUpdateMillis) {
        Bundle args = new Bundle();
        args.putLong(ARG_CITY_ID, cityId);
        args.putString(ARG_CITY_NAME, cityName);
        args.putString(ARG_DESCRIPTION, description);
        args.putDouble(ARG_TEMPERATURE, temp);
        args.putDouble(ARG_HUMIDITY, humidity);
        args.putDouble(ARG_WIND_SPEED, windSpeed);
        args.putString(ARG_ICON, iconName);
        args.putLong(ARG_TIMESTAMP, lastUpdateMillis);

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
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //tell the activity to set up the action bar
        //listener.onCurrentWeatherLoadComplete(cityName);

        //initialize the current weather loader
//        Log.d(TAG, "onActivityCreated: Init current weather loader");
//        CurrentWeatherLoaderCallbacks.initLoader(
//                getActivity(),
//                getLoaderManager(),
//                this,
//                PROJECTION_CURRENT_WEATHER);

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
            Log.d(TAG, "onLoadComplete: view holder is null");
            return;
        }

        switch (loaderId) {
            case CURRENT_WEATHER_LOADER:
                Log.d(TAG, "This should not happen");
//                if (cursor == null) {
//                    Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Cursor is null. ", loaderId));
//                    //loader is resetting
//                    return;
//                }
//
//                if (cursor.moveToFirst()) {
//                    Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. View is not null, cursor is not empty", loaderId));
//                    //update model
//                    updateModel(cursor);
//
//                    //update current view
//                    updateCurrentWeatherView(viewHolder, cursor);
//
//                    //notify the listener to set the title on the toolbar
//                    listener.onCurrentWeatherLoadComplete(cityName);
//                }
//                else {
//                    Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. View is not null but cursor is empty", loaderId));
//                    //resetCurrentWeatherView(viewHolder);
//                }
                break;

            case DAILY_FORECAST_LOADER:
                Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Cursor swapped", loaderId));
                DailyForecastAdapter dailyForecastAdapter = (DailyForecastAdapter) viewHolder.dailyForecastListView.getAdapter();
                dailyForecastAdapter.swapCursor(cursor);

                //show the list container and hide the progress bar
                if (viewHolder.dailyForecastListContainer.getVisibility() != View.VISIBLE) {
                    AnimationUtils.crossFadeViews(getActivity(),
                            viewHolder.dailyForecastListContainer,
                            viewHolder.dailyForecastProgressBar);
                }
                break;

            case TRIHOUR_FORECAST_LOADER:
                Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Loader with this id should not be used here", loaderId));
                break;

            default:
                Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
                return;
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
        Log.d(TAG, String.format("onDailyForecastItemClick: Position:%d Forecast date:%s", position, forecastFormatted));

        //pass city id, city name, and forecast time to listener
        listener.onDailyForecastItemClick(cityId, cityName, forecastMillis);
    }

    /**
     * Helper method to update the member variables
     * @param cursor
     */
//    private void updateModel(@NonNull Cursor cursor) {
//        cityId = cursor.getLong(CurrentWeatherCursorPosition.CITY_ID_POS.getValue());
//        cityName = cursor.getString(CurrentWeatherCursorPosition.CITY_NAME_POS.getValue());
//    }

    /**
     * Helper method to update the view
     * @param viewHolder
     */
    private void updateCurrentWeatherView(@NonNull ViewHolder viewHolder) {
                                          //) {
        //read values from the cursor
//        String description = cursor.getString(CurrentWeatherCursorPosition.DESCRIPTION_POS.getValue());
//        double temp = cursor.getDouble(CurrentWeatherCursorPosition.TEMP_POS.getValue());
//        double humidity = cursor.getDouble(CurrentWeatherCursorPosition.HUMIDITY_POS.getValue());
//        double windSpeed = cursor.getDouble(CurrentWeatherCursorPosition.WINDSPEED_POS.getValue());
//        long lastUpdateMillis = cursor.getLong(CurrentWeatherCursorPosition.TIMESTAMP_POS.getValue());

        //format as necessary
        Date lastUpdateDate = new Date(lastUpdateMillis);
        String lastUpdateInUserFormat = DateFormat.getMediumDateFormat(getActivity()).format(lastUpdateDate) +
                " " + DateFormat.getTimeFormat(getActivity()).format(lastUpdateDate);

        viewHolder.currentDescription.setText(FormatUtils.formatDescription(description));
        viewHolder.currentTemp.setText(String.valueOf(Math.round(temp)));
        viewHolder.currentHumidity.setText(String.valueOf(Math.round(humidity)) + " " + getString(R.string.humidity_unit));
        viewHolder.currentWindSpeed.setText(getString(R.string.current_windspeed_value, String.valueOf(Math.round(windSpeed))));
        viewHolder.lastUpdate.setText(getString(R.string.last_update_value, lastUpdateInUserFormat));

        //load the image using picasso
        ImageUtils.loadImage(getActivity(), iconName, viewHolder.currentIcon);
//        Picasso.with(getActivity())
//                .load(ImageUtils.buildIconUri(iconName))
//                .into(viewHolder.currentIcon);
//        CacheUtils.logCache();

        Log.i(TAG, String.format("onLoadComplete: Updating views with cityName:%s, description:%s, temp:%f, humidity:%f, windspeed:%f lastUpdate:%s",
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
        final TextView lastUpdate;

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

            lastUpdate = (TextView) view.findViewById(R.id.last_update);
        }
    }
}
