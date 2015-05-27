package com.example.yeelin.homework2.h312yeelin.adapter;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragmentUtils.FormatUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ImageUtils;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;

import java.util.Date;

/**
 * Created by ninjakiki on 4/21/15.
 */
public class TriHourForecastAdapter extends CursorAdapter {
    //logcat
    private static final String TAG = TriHourForecastAdapter.class.getCanonicalName();

    //for users of this class
    public static final String[] PROJECTION_TRI_HOUR_FORECAST = new String[] {
            TriHourForecastContract.Columns._ID,
            TriHourForecastContract.Columns.CITY_ID,
            TriHourForecastContract.Columns.FORECAST_DATETIME,
            TriHourForecastContract.Columns.TEMPERATURE,
            TriHourForecastContract.Columns.DESCRIPTION,
            TriHourForecastContract.Columns.ICON
    };

    //for bind view
    private enum TriHourForecastCursorPosition {
        ID_POS(0),
        CITY_ID_POS(1),
        FORECAST_DATETIME_POS(2),
        TEMP_POS(3),
        DESCRIPTION_POS(4),
        ICON_POS(5);

        private int value;
        private TriHourForecastCursorPosition(int value) {
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
    public TriHourForecastAdapter(Context context) {
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
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_item_tri_hour_forecast, parent, false);
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

        long forecastMillis = cursor.getLong(TriHourForecastCursorPosition.FORECAST_DATETIME_POS.getValue());
        double temperature = cursor.getDouble(TriHourForecastCursorPosition.TEMP_POS.getValue());
        String description = cursor.getString(TriHourForecastCursorPosition.DESCRIPTION_POS.getValue());
        String iconName = cursor.getString(TriHourForecastCursorPosition.ICON_POS.getValue());

        //format the time according to user's preference on device
        String forecastTimeInUserFormat = DateFormat.getTimeFormat(context).format(new Date(forecastMillis));

        viewHolder.triHour.setText(forecastTimeInUserFormat);
        viewHolder.temp.setText(String.valueOf(Math.round(temperature)));
        viewHolder.description.setText(FormatUtils.formatDescription(description));

        //load the image using picasso
        ImageUtils.loadImage(context, iconName, viewHolder.icon);
    }

    /**
     * View holder class
     */
    private class ViewHolder {
        final TextView triHour;
        final TextView temp;
        final TextView description;
        final ImageView icon;

        ViewHolder(View view) {
            triHour = (TextView) view.findViewById(R.id.tri_hour_forecast_time);
            temp = (TextView) view.findViewById(R.id.tri_hour_forecast_temp);
            description = (TextView) view.findViewById(R.id.tri_hour_forecast_description);
            icon = (ImageView) view.findViewById(R.id.tri_hour_forecast_image);
        }
    }
}
