package com.example.yeelin.homework.weatherberry.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.yeelin.homework.weatherberry.provider.CurrentWeatherContract;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 4/10/15.
 */
public class CurrentWeatherLoaderCallbacks
        extends BaseWeatherLoaderCallbacks
        implements LoaderManager.LoaderCallbacks<Cursor> {
    //logcat
    private static final String TAG = CurrentWeatherLoaderCallbacks.class.getCanonicalName();

    //bundle args to init loader
    private static final String ARG_URI = CurrentWeatherLoaderCallbacks.class.getSimpleName() + ".uri";
    private static final String ARG_PROJECTION = CurrentWeatherLoaderCallbacks.class.getSimpleName() + ".projection";

    //member variables
    private Context applicationContext;
    private WeakReference<CurrentWeatherLoaderListener> listenerWeakReference;

    /**
     * Listener interface
     */
    public interface CurrentWeatherLoaderListener {
        public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor);
    }

    /**
     * Private constructor. Use init loader instead
     * @param context
     * @param listener
     */
    private CurrentWeatherLoaderCallbacks(Context context,
                                          CurrentWeatherLoaderListener listener) {
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
                                  CurrentWeatherLoaderListener listener,
                                  String[] projection) {

        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, CurrentWeatherContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);

        //call loaderManager's initLoader
        loaderManager.initLoader(
                LoaderIds.CURRENT_WEATHER_LOADER.getValue(),
                args,
                new CurrentWeatherLoaderCallbacks(context, listener));
    }

    /**
     * Init loader with a specific id, either row id or city id
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     * @param id can be either row id or city id
     * @param idType either row id or city id
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  CurrentWeatherLoaderListener listener,
                                  String[] projection,
                                  long id,
                                  IdType idType) {

        Bundle args = new Bundle();
        Uri uri = buildUri(CurrentWeatherContract.URI, id, idType);
        args.putParcelable(ARG_URI, uri);
        args.putStringArray(ARG_PROJECTION, projection);

        //call loaderManager's initLoader
        loaderManager.initLoader(
                LoaderIds.CURRENT_WEATHER_LOADER.getValue(),
                args,
                new CurrentWeatherLoaderCallbacks(context, listener));
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
                                     CurrentWeatherLoaderListener listener,
                                     String[] projection) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);

        //call loaderManager's restartLoader
        loaderManager.restartLoader(
                LoaderIds.CURRENT_WEATHER_LOADER.getValue(),
                args,
                new CurrentWeatherLoaderCallbacks(context, listener));
    }

    /**
     * Destroy the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderIds.CURRENT_WEATHER_LOADER.getValue());
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

        //return a new cursor loader
        return new CursorLoader(applicationContext,
                uri,
                projection,
                null,
                null,
                CurrentWeatherContract.Columns.USER_FAVORITE + " asc, " + CurrentWeatherContract.Columns.CITY_NAME + " asc"); //TODO: may have to change sort order
    }

    /**
     * Loader has finished, notify the listeners if any.
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Log.d(TAG, "onLoadFinished: Result is " + (cursor != null ? "not null" : "null"));

        //let the listener know
        CurrentWeatherLoaderListener listener = listenerWeakReference.get();
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
