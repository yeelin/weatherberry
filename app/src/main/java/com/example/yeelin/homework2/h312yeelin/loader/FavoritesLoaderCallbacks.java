package com.example.yeelin.homework2.h312yeelin.loader;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.yeelin.homework2.h312yeelin.provider.BaseWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 5/27/15.
 */
public class FavoritesLoaderCallbacks implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = FavoritesLoaderCallbacks.class.getCanonicalName();

    //bundle args to init loader
    private static final String ARG_URI = FavoritesLoaderCallbacks.class.getSimpleName() + ".uri";
    private static final String ARG_PROJECTION = FavoritesLoaderCallbacks.class.getSimpleName() + ".projection";
    private static final String ARG_SELECTION = FavoritesLoaderCallbacks.class.getSimpleName() + ".selection";
    private static final String ARG_SELECTION_ARGS = FavoritesLoaderCallbacks.class.getSimpleName() + ".selectionArgs";
    private static final String ARG_SORT_ORDER = FavoritesLoaderCallbacks.class.getSimpleName() + ".sortOrder";

    //member variables
    private Context applicationContext;
    private WeakReference<FavoritesLoaderListener> listenerWeakReference;

    /**
     * Listener interface for those interested in callbacks from this loader
     */
    public interface FavoritesLoaderListener {
        public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor);
    }

    /**
     * Private constructor.  Use initLoader instead
     * @param context
     * @param listener
     */
    private FavoritesLoaderCallbacks(Context context, FavoritesLoaderListener listener) {
        applicationContext = context.getApplicationContext();
        listenerWeakReference = new WeakReference<>(listener);
    }

    /**
     *
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     */
    public static void initLoader(Context context,
                                  LoaderManager loaderManager,
                                  FavoritesLoaderListener listener,
                                  String[] projection) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, CurrentWeatherContract.URI);
        args.putStringArray(ARG_PROJECTION, projection);

        //call loader manager's initLoader
        loaderManager.initLoader(LoaderIds.FAVORITES_LOADER.getValue(), args, new FavoritesLoaderCallbacks(context, listener));
    }

    /**
     *
     * @param context
     * @param loaderManager
     * @param listener
     * @param projection
     */
    public static void restartLoader(Context context,
                                     LoaderManager loaderManager,
                                     FavoritesLoaderListener listener,
                                     String[] projection) {
        Bundle args = new Bundle();
        args.putStringArray(ARG_PROJECTION, projection);
        loaderManager.restartLoader(LoaderIds.FAVORITES_LOADER.getValue(), args, new FavoritesLoaderCallbacks(context, listener));
    }

    /**
     * Destroys the loader
     * @param loaderManager
     */
    public static void destroyLoader(LoaderManager loaderManager) {
        loaderManager.destroyLoader(LoaderIds.FAVORITES_LOADER.getValue());
    }

    /**
     * Creates a new loader
     * @param id
     * @param args
     * @return
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = args.getParcelable(ARG_URI);
        String[] projection = args.getStringArray(ARG_PROJECTION);

        return new CursorLoader(applicationContext,
                uri,
                projection,
                BaseWeatherContract.whereClauseEquals(CurrentWeatherContract.Columns.USER_FAVORITE),
                BaseWeatherContract.whereArgs(CurrentWeatherContract.USER_FAVORITE_YES),
                CurrentWeatherContract.Columns.CITY_NAME + " asc");
    }

    /**
     * Loader has finished, notify listeners
     * @param loader
     * @param cursor
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //let the listener know
        FavoritesLoaderListener listener = listenerWeakReference.get();
        if (listener != null) {
            listener.onLoadComplete(LoaderIds.getLoaderIdForInt(loader.getId()), cursor);
        }
    }

    /**
     * Loader has been reset, notify listeners
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //let the listener know
        onLoadFinished(loader, null);
    }
}
