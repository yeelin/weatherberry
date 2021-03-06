package com.example.yeelin.homework.weatherberry.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.provider.BaseWeatherContract;
import com.example.yeelin.homework.weatherberry.provider.DailyForecastContract;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 4/20/15.
 */
public class DailyForecastLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<Cursor> {
    //logcat
    private static final String TAG = DailyForecastLoaderCallbacks.class.getCanonicalName();

    //bundle args in init loader
    private static final String ARG_URI = DailyForecastLoaderCallbacks.class.getSimpleName() + ".uri";
    private static final String ARG_PROJECTION = DailyForecastLoaderCallbacks.class.getSimpleName() + ".projection";
    private static final String ARG_SELECTION = DailyForecastLoaderCallbacks.class.getSimpleName() + ".selection";
    private static final String ARG_SELECTION_ARGS = DailyForecastLoaderCallbacks.class.getSimpleName() + ".selectionArgs";

    //member variables
    private Context applicationContext;
    private WeakReference<DailyForecastLoaderListener> listenerWeakReference;

    /**
     * Listener interface
     */
    public interface DailyForecastLoaderListener {
        public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor);
    }

    /**
     * Private constructor. Use init loader instead
     * @param context
     * @param listener
     */
    private DailyForecastLoaderCallbacks(Context context,
                                         DailyForecastLoaderListener listener) {
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(listener);
    }

    /**
     * Init loader without id.
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  DailyForecastLoaderListener listener,
                                  String[] projection) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, DailyForecastContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);

        loaderManager.initLoader(
                LoaderIds.DAILY_FORECAST_LOADER.getValue(),
                args,
                new DailyForecastLoaderCallbacks(context, listener));
    }

    /**
     * Init loader with a specific id (can be either row id or city id).
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     * @param id
     * @param idType
     * @param userFavorite
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  DailyForecastLoaderListener listener,
                                  String[] projection,
                                  long id,
                                  BaseWeatherContract.IdType idType,
                                  boolean userFavorite) {
        Bundle args = new Bundle();
        Uri uri = BaseWeatherContract.buildUri(DailyForecastContract.URI, id, idType);
        args.putParcelable(ARG_URI, uri);
        args.putStringArray(ARG_PROJECTION, projection);
        args.putString(ARG_SELECTION, BaseWeatherContract.whereClauseEquals(DailyForecastContract.Columns.USER_FAVORITE));
        args.putStringArray(ARG_SELECTION_ARGS, BaseWeatherContract.whereArgs(userFavorite ? BaseWeatherContract.USER_FAVORITE_YES : BaseWeatherContract.USER_FAVORITE_NO));

        loaderManager.initLoader(
                LoaderIds.DAILY_FORECAST_LOADER.getValue(),
                args,
                new DailyForecastLoaderCallbacks(context, listener));
    }

    /**
     * Restart the loader
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     */
    public static void restartLoader(Context context,
                                     LoaderManager loaderManager,
                                     DailyForecastLoaderListener listener,
                                     String[] projection) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);

        //call loader manager's restart loader
        loaderManager.restartLoader(
                LoaderIds.DAILY_FORECAST_LOADER.getValue(),
                args,
                new DailyForecastLoaderCallbacks(context, listener));
    }

    /**
     * Destroy the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderIds.DAILY_FORECAST_LOADER.getValue());
    }

    /**
     * Creates a new loader
     * @param loaderId
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        //Log.d(TAG, "onCreateLoader");

        //get bundle args
        Uri uri = args.getParcelable(ARG_URI);
        String[] projection = args.getStringArray(ARG_PROJECTION);
        String selection = args.getString(ARG_SELECTION);
        String[] selectionArgs = args.getStringArray(ARG_SELECTION_ARGS);

        //return a new cursor loader
        return new CursorLoader(applicationContext,
                uri,
                projection,
                selection,
                selectionArgs,
                DailyForecastContract.Columns.FORECAST_DATETIME + " asc");
    }

    /**
     * Loader has finished, notify listeners
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Log.d(TAG, "onLoadFinished: Cursor with LoaderId:" +  LoaderIds.getLoaderIdForInt(loader.getId()) + " is " + (cursor != null ? "not null" : "null"));

        //let the listener know
        DailyForecastLoaderListener listener = listenerWeakReference.get();
        if (listener != null) {
            //Log.d(TAG, "onLoadFinished: Notifying listener");
            listener.onLoadComplete(LoaderIds.getLoaderIdForInt(loader.getId()), cursor);
        }
    }

    /**
     * Loader has been reset, notify the listeners with a null cursor.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Log.d(TAG, "onLoaderReset");

        //let the listener know
        onLoadFinished(loader, null);
    }
}
