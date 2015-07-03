package com.example.yeelin.homework.weatherberry.networkUtils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;

import com.example.yeelin.homework.weatherberry.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.Collection;

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
     * Fetch unique weather icons given a collection of icon names.
     * This is for warming the cache.
     * @param context
     * @param iconNames
     */
    public static void getImages(Context context, @NonNull Collection<String> iconNames) {
        Log.d(TAG, "getImages: Collection: " + iconNames);
        for (final String iconName: iconNames) {
            //Log.d(TAG, "getImages: Fetching icon:" + iconName);
            Uri iconUri = buildIconUri(iconName);

            Picasso.with(context)
                    .load(iconUri)
                    .resizeDimen(R.dimen.weather_image_size, R.dimen.weather_image_size)
                    .centerInside()
                    .onlyScaleDown()
                    .fetch(new Callback() {
                        @Override
                        public void onSuccess() {
                            //Log.d(TAG, "onSuccess: Fetch success. Icon:" + iconName);
                        }

                        @Override
                        public void onError() {
                            //Log.d(TAG, "onError: Fetch failed. Icon:" + iconName);
                        }
                    });
        }
        //Log.d(TAG, "getImages: Done");
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
        //Log.d(TAG, "loadImage: Loading icon:" + iconName);
//        if (BuildConfig.DEBUG) {
//            Picasso.with(context).setIndicatorsEnabled(true);
//        }

        Picasso.with(context)
                .load(ImageUtils.buildIconUri(iconName))
                .into(imageView);

        //CacheUtils.logCache();
        //Log.d(TAG, "loadImage: Done");
    }


    /**
     * Builds an icon url for querying the open weather api
     * Format: http://api.openweathermap.org/img/w/10d.png
     *
     * @param iconName
     * @return
     */
    @NonNull
    private static Uri buildIconUri(@NonNull String iconName) {

        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(IMG)
                .appendPath(W_PATH)
                .appendPath(iconName + IMG_EXTENSION)
                .build();

        //Log.d(TAG, "buildIconUri: Uri: " + uri.toString());
    }
}
