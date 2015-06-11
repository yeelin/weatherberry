package com.example.yeelin.homework2.h312yeelin.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.provider.CurrentWeatherContract;

/**
 * Created by ninjakiki on 4/14/15.
 */
public class WeatherService extends IntentService {
    //logcat
    private static final String TAG = WeatherService.class.getCanonicalName();
    //actions in intent
    private static final String ACTION_INSERT = WeatherService.class.getSimpleName() + ".insert";
    private static final String ACTION_UPDATE = WeatherService.class.getSimpleName() + ".update";
    private static final String ACTION_DELETE = WeatherService.class.getSimpleName() + ".delete";

    //extras in intent
    private static final String EXTRA_URI = WeatherService.class.getSimpleName() + ".uri";
    private static final String EXTRA_VALUES = WeatherService.class.getSimpleName() + ".values";
    private static final String EXTRA_SELECTION = WeatherService.class.getSimpleName() + ".selection";
    private static final String EXTRA_SELECTION_ARGS = WeatherService.class.getSimpleName() + ".selectionArgs";

    /**
     * Required by the Manifest.
     */
    public WeatherService() {
        super(WeatherService.class.getSimpleName());
    }

    /**
     * Required. Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public WeatherService(String name) {
        super(name);
    }

    /**
     * Required override.  Handles the requested action in the intent by calling the
     * appropriate helper method.  This method runs on a background thread.
     * @param intent
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent");
        if (intent != null) {
            final String action = intent.getAction();

            if (ACTION_INSERT.equals(action)) {
                ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
                handleActionInsert(values);
            }
            else if (ACTION_UPDATE.equals(action)) {
                Uri uri = intent.getParcelableExtra(EXTRA_URI);
                ContentValues values = intent.getParcelableExtra(EXTRA_VALUES);
                String selection = intent.getStringExtra(EXTRA_SELECTION);
                String[] selectionArgs = intent.getStringArrayExtra(EXTRA_SELECTION_ARGS);

                handleActionUpdate(uri, values, selection, selectionArgs);
            }
            else if (ACTION_DELETE.equals(action)) {
                Uri uri = intent.getParcelableExtra(EXTRA_URI);
                String selection = intent.getStringExtra(EXTRA_SELECTION);
                String[] selectionArgs = intent.getStringArrayExtra(EXTRA_SELECTION_ARGS);

                handleActionDelete(uri, selection, selectionArgs);
            }
        }
    }

    /**
     * Helper method that does inserts on the background thread.
     * Called from onHandleIntent.
     * @param values
     */
    private void handleActionInsert(ContentValues values) {
        Log.d(TAG, "handleActionInsert");
        getContentResolver().insert(
                CurrentWeatherContract.URI,
                values);
    }

    /**
     * Helper method that does updates on the background thread.
     * Called from onHandleIntent.
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     */
    private void handleActionUpdate(Uri uri,
                                    ContentValues values,
                                    String selection,
                                    String[] selectionArgs) {
        Log.d(TAG, "handleActionUpdate");
        getContentResolver().update(
                uri,
                values,
                selection,
                selectionArgs);
    }

    /**
     * Helper method that does deletes on the background thread.
     * Called from onHandleIntent.
     * @param uri
     * @param selection
     * @param selectionArgs
     */
    private void handleActionDelete(Uri uri,
                                    String selection,
                                    String[] selectionArgs) {
        Log.d(TAG, "handleActionDelete");
        getContentResolver().delete(
                uri,
                selection,
                selectionArgs);
    }

    /**
     * Start this service to perform an insert.
     * @param context
     * @param values
     */
    public static void startActionInsert(Context context,
                                         ContentValues values) {

        Intent intent = new Intent(context, WeatherService.class);

        //set action and values
        intent.setAction(ACTION_INSERT);
        intent.putExtra(EXTRA_VALUES, values);

        context.startService(intent);
    }

    /**
     * Start this service to perform an update.
     * @param context
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     */
    public static void startActionUpdate(Context context,
                                         Uri uri,
                                         ContentValues values,
                                         String selection,
                                         String[] selectionArgs) {

        Intent intent = new Intent(context, WeatherService.class);

        //set action, uri, values, selection, args
        intent.setAction(ACTION_UPDATE);
        intent.putExtra(EXTRA_URI, uri);
        intent.putExtra(EXTRA_VALUES, values);
        intent.putExtra(EXTRA_SELECTION, selection);
        intent.putExtra(EXTRA_SELECTION_ARGS, selectionArgs);

        context.startService(intent);
    }

    /**
     * Start this service to perform a delete
     * @param context
     * @param uri
     * @param selection
     * @param selectionArgs
     */
    public static void startActionDelete(Context context,
                                         Uri uri,
                                         String selection,
                                         String[] selectionArgs) {

        Intent intent = new Intent(context, WeatherService.class);

        //set action, uri, selection, selection args
        intent.setAction(ACTION_DELETE);
        intent.putExtra(EXTRA_URI, uri);
        intent.putExtra(EXTRA_SELECTION, selection);
        intent.putExtra(EXTRA_SELECTION_ARGS, selectionArgs);

        context.startService(intent);
    }
}
