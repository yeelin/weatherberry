package com.example.yeelin.homework2.h312yeelin.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.ListView;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

/**
 * Created by ninjakiki on 5/27/15.
 */
public class FavoritesAdapter extends CursorAdapter {
    private static final String TAG = FavoritesAdapter.class.getCanonicalName();

    //projection to get favorites from database
    public static final String[] PROJECTION_FAVORITES = new String[] {
            CurrentWeatherContract.Columns._ID,
            CurrentWeatherContract.Columns.CITY_ID,
            CurrentWeatherContract.Columns.CITY_NAME,
    };

    //enum for bind view
    public enum FavoritesCursorPosition {
        ID_POS(0),
        CITY_ID_POS(1),
        CITY_NAME_POS(2);

        private int value;
        FavoritesCursorPosition(int value) { this.value = value; }
        public int getValue() { return value; }
    }

    /**
     * Constructor
     * @param context
     */
    public FavoritesAdapter(Context context) {
        super(context, null, 0);
    }

    /**
     * Called by getView when convert view is null. Returns a new view.
     * @param context
     * @param cursor
     * @param parent
     * @return
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.adapter_item_favorite, parent, false);
        view.setTag(new ViewHolder(view));
        return view;
    }

    /**
     * Override to restore the state of the list view item if any
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        //restore the checked state of the list view item if any
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        ListView listview = (ListView) parent;
        viewHolder.favoriteCity.setChecked(listview.isItemChecked(position));
        return view;
    }

    /**
     * Called by getView in adapter when convert view is not null (i.e. we are recycling).
     * Bind view here. Cursor is already at the right position
     * @param view
     * @param context
     * @param cursor
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        String cityName = cursor.getString(FavoritesCursorPosition.CITY_NAME_POS.getValue());
        viewHolder.favoriteCity.setText(cityName);
    }

    /**
     * ViewHolder class
     */
    public class ViewHolder {
        public final CheckedTextView favoriteCity;

        ViewHolder(View view) {
            favoriteCity = (CheckedTextView) view.findViewById(R.id.favorite_city);
        }

    }
}
