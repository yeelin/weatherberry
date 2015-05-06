package com.example.yeelin.homework2.h312yeelin.networkUtils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.example.yeelin.homework2.h312yeelin.BuildConfig;
import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;
import com.example.yeelin.homework2.h312yeelin.provider.DailyForecastContract;
import com.example.yeelin.homework2.h312yeelin.provider.TriHourForecastContract;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by ninjakiki on 5/5/15.
 */
public class ImageUtils {
    //logcat
    private static final String TAG = ImageUtils.class.getCanonicalName();

    //uri parts
    private static final String SCHEME = "http";
    private static final String AUTHORITY = "api.openweathermap.org";
    private static final String IMG = "img";
    private static final String W_PATH = "w";
    private static final String IMG_EXTENSION = ".png";

//    private static LruCache lruCache;
//
//    public void initialize(Context context) {
//        if (lruCache == null)
//            lruCache = new LruCache(context);
//
//        Picasso picassoInstance = new Picasso.Builder(context)
//                .memoryCache(lruCache)
//                .indicatorsEnabled(true)
//                .build();
//        Picasso.setSingletonInstance(picassoInstance);
//    }

    /**
     * Fetch unique weather icons from the new data just inserted into the database.
     * This is for warming the cache.
     *
     * @param context
     * @param currentWeatherValues
     * @param dailyForecastValues
     * @param triHourForecastValues
     */
    public static void getImages(Context context,
                                  ContentValues[] currentWeatherValues,
                                  ContentValues[] dailyForecastValues,
                                  ContentValues[] triHourForecastValues) {
        Log.d(TAG, "getImages");
        //get all the unique icons to fetch
        HashMap<String, String> iconNameMap = getUniqueIconNamesToFetch(currentWeatherValues, dailyForecastValues, triHourForecastValues);

        //iterate over each icon name, create a uri, and fetch into cache
        for (final String iconName: iconNameMap.keySet()) {
            Log.d(TAG, "getImages: Fetching icon:" + iconName);
            Uri iconUri = buildIconUri(iconName);

            Picasso.with(context)
                    .load(iconUri)
                    .resizeDimen(R.dimen.weather_image_size, R.dimen.weather_image_size)
                    .centerInside()
                    .onlyScaleDown()
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d(TAG, "onSuccess: Fetch success. Icon:" + iconName);
                        }

                        @Override
                        public void onError() {
                            Log.d(TAG, "onError: Fetch failed. Icon:" + iconName);
                        }
                    });
        }
        Log.d(TAG, "getImages: Done");
    }

    /**
     * Loads the weather icons into the imageview. Ideally the load would happen from the cache. This should
     * be called after getImages().
     *
     * @param context
     * @param iconName
     * @param imageView
     */
    public static void loadImage(Context context, String iconName, ImageView imageView) {
        Log.d(TAG, "loadImage: Loading icon:" + iconName);
        if (BuildConfig.DEBUG) {
            Picasso.with(context).setIndicatorsEnabled(true);
        }

        Picasso.with(context)
                .load(ImageUtils.buildIconUri(iconName))
                .into(imageView);

        CacheUtils.logCache();
        Log.d(TAG, "loadImage: Done");
    }

    /**
     * Loops over all the values inserted into the database and retrieves the unique icons
     * that need to be fetched.
     * @param currentWeatherValues
     * @param dailyForecastValues
     * @param triHourForecastValues
     * @return
     */
    private static HashMap<String, String> getUniqueIconNamesToFetch(ContentValues[] currentWeatherValues,
                                                                     ContentValues[] dailyForecastValues,
                                                                     ContentValues[] triHourForecastValues) {
        HashMap<String, String> iconNameMap = new HashMap<>();

        //get unique icon names
        for (ContentValues values : currentWeatherValues) {
            String iconName = (String) values.get(CurrentWeatherContract.Columns.ICON);
            if (!iconNameMap.containsKey(iconName)) {
                Log.d(TAG, "Adding to iconmap:" + iconName);
                iconNameMap.put(iconName, iconName);
            }
            else {
                Log.d(TAG, "Iconmap already contains " + iconName);
            }
        }
        for (ContentValues values : dailyForecastValues) {
            String iconName = (String) values.get(DailyForecastContract.Columns.ICON);
            if (!iconNameMap.containsKey(iconName)) {
                Log.d(TAG, "Adding to iconmap:" + iconName);
                iconNameMap.put(iconName, iconName);
            }
            else {
                Log.d(TAG, "Iconmap already contains " + iconName);
            }
        }
        for (ContentValues values : triHourForecastValues) {
            String iconName = (String) values.get(TriHourForecastContract.Columns.ICON);
            if (!iconNameMap.containsKey(iconName)) {
                Log.d(TAG, "Adding to iconmap:" + iconName);
                iconNameMap.put(iconName, iconName);
            }
            else {
                Log.d(TAG, "Iconmap already contains " + iconName);
            }
        }

        Log.d(TAG, String.format("getUniqueIconNamesToFetch: Count:%d, Contents:%s", iconNameMap.size(), iconNameMap.toString()));
        return iconNameMap;
    }

    /**
     * Builds an icon url for querying the open weather api
     * Format: http://api.openweathermap.org/img/w/10d.png
     *
     * @param iconName
     * @return
     */
    private static Uri buildIconUri(String iconName) {

        Uri uri = new Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(IMG)
                .appendPath(W_PATH)
                .appendPath(iconName + IMG_EXTENSION)
                .build();

        //Log.d(TAG, "buildIconUri: Uri: " + uri.toString());
        return uri;
    }
}
