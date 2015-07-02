package com.example.yeelin.homework2.h312yeelin.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by ninjakiki on 4/21/15.
 */
public class DailyForecastAdapter extends CursorAdapter {
    //logcat
    private static final String TAG = DailyForecastAdapter.class.getCanonicalName();

    //for users of this class
    public static final String[] PROJECTION_DAILY_FORECAST = new String[] {
            DailyForecastContract.Columns._ID,
            DailyForecastContract.Columns.CITY_ID,
            DailyForecastContract.Columns.FORECAST_DATETIME,
            DailyForecastContract.Columns.TEMPERATURE_LOW,
            DailyForecastContract.Columns.TEMPERATURE_HIGH,
            DailyForecastContract.Columns.ICON
    };

    //for bind view
    public enum DailyForecastCursorPosition {
        ID_POS(0),
        CITY_ID_POS(1),
        FORECAST_DATETIME_POS(2),
        TEMP_LOW_POS(3),
        TEMP_HIGH_POS(4),
        ICON_POS(5);

        private int value;
        DailyForecastCursorPosition(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    /**
     *
     * @param context
     */
    public DailyForecastAdapter(Context context) {
        super(context, null, 0);
    }

    /**
     * Called by getView in Adapter when the convert view is null. Returns a new view.
     *
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_item_daily_forecast, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /**
     * Called by getView in Adapter when the convert view is not null. Bind the view here.
     * Cursor is already at the right position.
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        long forecastMillis = cursor.getLong(DailyForecastCursorPosition.FORECAST_DATETIME_POS.getValue());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE", Locale.US); //EEEE is Day of week in long form, e.g. Monday, Tuesday, etc.
        String dayOfWeek = simpleDateFormat.format(new Date(forecastMillis));

        double temperatureLow = cursor.getDouble(DailyForecastCursorPosition.TEMP_LOW_POS.getValue());
        double temperatureHigh = cursor.getDouble(DailyForecastCursorPosition.TEMP_HIGH_POS.getValue());
        String iconName = cursor.getString(DailyForecastCursorPosition.ICON_POS.getValue());

        viewHolder.dayOfWeek.setText(dayOfWeek);
        viewHolder.tempLow.setText(String.valueOf(Math.round(temperatureLow)));
        viewHolder.tempHigh.setText(String.valueOf(Math.round(temperatureHigh)));

        //load the image using picasso
        ImageUtils.loadImage(context, iconName, viewHolder.icon);
//        Picasso.with(context)
//                .load(ImageUtils.buildIconUri(iconName))
//                .into(viewHolder.icon);
//        CacheUtils.logCache();
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        final TextView dayOfWeek;
        final TextView tempHigh;
        final TextView tempLow;
        final ImageView icon;

        ViewHolder(View view) {
            dayOfWeek = (TextView) view.findViewById(R.id.daily_forecast_dayOfWeek);
            tempHigh = (TextView) view.findViewById(R.id.daily_forecast_tempHigh);
            tempLow = (TextView) view.findViewById(R.id.daily_forecast_tempLow);
            icon = (ImageView) view.findViewById(R.id.daily_forecast_image);
        }
    }
}
