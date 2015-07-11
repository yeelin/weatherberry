package com.example.yeelin.homework.weatherberry.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ListView;

import com.example.yeelin.homework.weatherberry.R;
import com.example.yeelin.homework.weatherberry.adapter.TriHourForecastAdapter;
import com.example.yeelin.homework.weatherberry.fragmentUtils.AnimationUtils;
import com.example.yeelin.homework.weatherberry.loader.LoaderIds;
import com.example.yeelin.homework.weatherberry.loader.TriHourForecastLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by ninjakiki on 4/13/15.
 */
public class TriHourForecastFragment
        extends Fragment
        implements TriHourForecastLoaderCallbacks.TriHourForecastLoaderListener {
    //logcat
    private static final String TAG = TriHourForecastFragment.class.getCanonicalName();

    //bundle args
    private static final String ARG_CITY_ID = TriHourForecastFragment.class.getSimpleName() + ".cityId";
    private static final String ARG_USER_FAVORITE = TriHourForecastFragment.class.getSimpleName() + ".userFavorite";
    private static final String ARG_FORECAST_MILLIS = TriHourForecastFragment.class.getSimpleName() + ".forecastMillis";

    //member variables
    private long cityId = BaseWeatherContract.NO_ID;
    private boolean userFavorite = true;
    private long startMillis = 0;
    private long endMillis = 0;

    /**
     *
     * @param cityId
     * @param forecastMillis
     * @return
     */
    public static TriHourForecastFragment newInstance(long cityId, boolean userFavorite, long forecastMillis) {
        Bundle args = new Bundle();
        args.putLong(ARG_CITY_ID, cityId);
        args.putBoolean(ARG_USER_FAVORITE, userFavorite);
        args.putLong(ARG_FORECAST_MILLIS, forecastMillis);

        TriHourForecastFragment fragment = new TriHourForecastFragment();
        fragment.setArguments(args);

        return fragment;
    }

    //required public empty constructor
    public TriHourForecastFragment() {

    }

    /**
     * Initialize fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        long forecastMills = 0;
        if (args != null) {
            cityId = args.getLong(ARG_CITY_ID, BaseWeatherContract.NO_ID);
            userFavorite = args.getBoolean(ARG_USER_FAVORITE, true);
            forecastMills = args.getLong(ARG_FORECAST_MILLIS, 0);
        }

        calculateStartAndEndMillis(forecastMills);
    }

    /**
     * Inflate the fragment's view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tri_hour_forecast, container, false);
    }

    /**
     * Configure the view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //set up the listview adapter
        viewHolder.triHourForecastListView.setAdapter(new TriHourForecastAdapter(view.getContext()));

        //initially make the list container invisible and show the progress bar
        viewHolder.triHourForecastListContainer.setVisibility(View.GONE);
        viewHolder.triHourForecastProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initialize the trihour forecast loader
        Log.d(TAG, "onActivityCreated: Init tri hour forecast loader");
        TriHourForecastLoaderCallbacks.initLoader(getActivity(),
                getLoaderManager(),
                this,
                TriHourForecastAdapter.PROJECTION_TRI_HOUR_FORECAST,
                cityId,
                BaseWeatherContract.IdType.CITY_ID,
                userFavorite,
                startMillis,
                endMillis);
    }

    /**
     * Loader callback with an updated cursor. Update the view.
     * @param loaderId
     * @param cursor
     */
    @Override
    public void onLoadComplete(LoaderIds loaderId, Cursor cursor) {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //nothing to do since views are not ready yet
            Log.d(TAG, "onLoadComplete: view holder is null");
            return;
        }

        if(loaderId == LoaderIds.TRIHOUR_FORECAST_LOADER) {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Cursor swapped", loaderId));
            TriHourForecastAdapter triHourForecastAdapter = (TriHourForecastAdapter) viewHolder.triHourForecastListView.getAdapter();
            triHourForecastAdapter.swapCursor(cursor);

            //show the list container and hide the progress bar
            if (viewHolder.triHourForecastListContainer.getVisibility() != View.VISIBLE) {
                AnimationUtils.crossFadeViews(getActivity(),
                        viewHolder.triHourForecastListContainer,
                        viewHolder.triHourForecastProgressBar);
            }
        }
        else {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
        }
    }

    /**
     * Given a time in milliseconds, this method sets:
     * 1. startMillis to the midnight on the same day, i.e. 00:00:01
     * 2. endMillis to 11:59pm on the same day, i.e. 23:59:59
     *
     * @param forecastMillis
     */
    private void calculateStartAndEndMillis(long forecastMillis) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE yyyy-MM-dd HH:mm ZZZZ"); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        //simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        simpleDateFormat.setTimeZone(TimeZone.getDefault());
        String originalString = simpleDateFormat.format(new Date(forecastMillis));

        Log.d(TAG, String.format("calculateStartAndEndMillis: Original Millis:%d Original String:%s:", forecastMillis, originalString));

        //want to get the midnight time before forecastMillis which is in utc
        //TimeZone timeZone = TimeZone.getTimeZone("UTC");
        TimeZone timeZone = TimeZone.getDefault();
        Calendar startCalendar = Calendar.getInstance(timeZone);
        startCalendar.setTimeInMillis(forecastMillis);
        startCalendar.set(Calendar.HOUR_OF_DAY, 0);
        startCalendar.set(Calendar.MINUTE, 0);
        startCalendar.set(Calendar.SECOND, 0);
        startCalendar.set(Calendar.MILLISECOND, 1);
        startMillis = startCalendar.getTimeInMillis();
        String startString = simpleDateFormat.format(new Date(startMillis));
        Log.d(TAG, String.format("calculateStartAndEndMillis: Start Millis:%d Start String:%s:", startMillis, startString));

        //calculate end Millis
        Calendar endCalendar = Calendar.getInstance(timeZone);
        endCalendar.setTimeInMillis(forecastMillis);
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 59);
        endCalendar.set(Calendar.SECOND, 59);
        endCalendar.set(Calendar.MILLISECOND, 59);
        endMillis = endCalendar.getTimeInMillis();
        String endString = simpleDateFormat.format(new Date(endMillis));
        Log.d(TAG, String.format("calculateStartAndEndMillis: End Millis:%d End String:%s:", endMillis, endString));
    }

    /**
     * Returns the view holder for the fragment's view if one exists.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        //member variables
        final View triHourForecastListContainer;
        final ListView triHourForecastListView;
        final View triHourForecastProgressBar;

        ViewHolder(View view) {
            triHourForecastListContainer = view.findViewById(R.id.tri_hour_forecast_listContainer);
            triHourForecastListView = (ListView) view.findViewById(R.id.tri_hour_forecast_listview);
            triHourForecastListView.setEmptyView(view.findViewById(R.id.tri_hour_forecast_empty));
            triHourForecastProgressBar = view.findViewById(R.id.tri_hour_forecast_progressBar);
        }
    }
}
