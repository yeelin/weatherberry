package com.example.yeelin.homework2.h312yeelin.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 4/20/15.
 */
public class TriHourForecastLoaderCallbacks
        extends BaseWeatherLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<Cursor> {
    //logcat
    private static final String TAG = TriHourForecastLoaderCallbacks.class.getCanonicalName();

    //bundle args to init loader
    private static final String ARG_URI = TriHourForecastLoaderCallbacks.class.getSimpleName() + ".uri";
    private static final String ARG_PROJECTION = TriHourForecastLoaderCallbacks.class.getSimpleName() + ".projection";
    private static final String ARG_SELECTION = TriHourForecastLoaderCallbacks.class.getSimpleName() + ".selection";
    private static final String ARG_SELECTION_ARGS = TriHourForecastLoaderCallbacks.class.getSimpleName() + ".selectionArgs";

    //member variables
    private Context applicationContext;
    private WeakReference<TriHourForecastLoaderListener> listenerWeakReference;

    /**
     * Listener interface
     */
    public interface TriHourForecastLoaderListener {
        public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor);
    }

    /**
     * Private constructor. Use init loader instead
     * @param context
     * @param listener
     */
    private TriHourForecastLoaderCallbacks(Context context,
                                           TriHourForecastLoaderListener listener) {
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(listener);
    }

    /**
     * Init loader without id
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  TriHourForecastLoaderListener listener,
                                  String[] projection) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, TriHourForecastContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);

        //call LoaderManager's initLoader
        loaderManager.initLoader(
                LoaderIds.TRIHOUR_FORECAST_LOADER.getValue(),
                args,
                new TriHourForecastLoaderCallbacks(context, listener));
    }

    /**
     * Init loader with a specific id, either row id or city id
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     * @param id
     * @param idType
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  TriHourForecastLoaderListener listener,
                                  String[] projection,
                                  long id,
                                  IdType idType) {
        Bundle args = new Bundle();
        Uri uri = buildUri(TriHourForecastContract.URI, id, idType);
        args.putParcelable(ARG_URI, uri);
        args.putStringArray(ARG_PROJECTION, projection);

        //call LoaderManager's initLoader
        loaderManager.initLoader(
                LoaderIds.TRIHOUR_FORECAST_LOADER.getValue(),
                args,
                new TriHourForecastLoaderCallbacks(context, listener));
    }

    /**
     * Init loader with a specific id, either row id or city id
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     * @param id
     * @param idType
     * @param startMillis
     * @param endMillis
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  TriHourForecastLoaderListener listener,
                                  String[] projection,
                                  long id,
                                  IdType idType,
                                  long startMillis,
                                  long endMillis) {
        Bundle args = new Bundle();
        Uri uri = buildUri(TriHourForecastContract.URI, id, idType);
        args.putParcelable(ARG_URI, uri);
        args.putStringArray(ARG_PROJECTION, projection);

        args.putString(ARG_SELECTION, BaseWeatherContract.whereClauseBetween(TriHourForecastContract.Columns.FORECAST_DATETIME));
        args.putStringArray(ARG_SELECTION_ARGS, BaseWeatherContract.whereArgs(startMillis, endMillis));

        //call LoaderManager's initLoader
        loaderManager.initLoader(
                LoaderIds.TRIHOUR_FORECAST_LOADER.getValue(),
                args,
                new TriHourForecastLoaderCallbacks(context, listener));
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
                                     TriHourForecastLoaderListener listener,
                                     String[] projection) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);

        //call loaderManager's restartLoader
        loaderManager.restartLoader(
                LoaderIds.TRIHOUR_FORECAST_LOADER.getValue(),
                args,
                new TriHourForecastLoaderCallbacks(context, listener));
    }

    /**
     * Destroy the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderIds.TRIHOUR_FORECAST_LOADER.getValue());
    }

    /**
     * Creates a new loader
     * @param loaderId
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Log.d(TAG, "onCreateLoader");

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
                TriHourForecastContract.Columns.FORECAST_DATETIME + " asc");
    }

    /**
     * Loader has finished, notify the listeners if any.
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished: Result is " + (cursor != null ? "not null" : "null"));

        //let the listener know
        TriHourForecastLoaderListener listener = listenerWeakReference.get();
        if (listener != null) {
            Log.d(TAG, "onLoadFinished: Notifying listener");
            listener.onLoadComplete(LoaderIds.getLoaderIdForInt(loader.getId()), cursor);
        }
    }

    /**
     * Loader has been reset, notify the listeners with a null cursor.
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset");

        //let the listener know
        onLoadFinished(loader, null);
    }
}
