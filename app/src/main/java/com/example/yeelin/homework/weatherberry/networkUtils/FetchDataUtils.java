package com.example.yeelin.homework.weatherberry.networkUtils;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by ninjakiki on 5/24/15.
 */
public class FetchDataUtils {
    private static final String TAG = FetchDataUtils.class.getCanonicalName();

    //http connection related
    private static final String HTTP_REQUEST_METHOD = "GET";
    private static final int HTTP_CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int HTTP_READ_TIMEOUT_MILLIS = 15000;

    //http response
    private static final String DEFAULT_ENCODING = "UTF-8";

    //uri header
    private static final String SCHEME = "http";
    private static final String AUTHORITY = "api.openweathermap.org";
    private static final String PATH_DATA = "data";
    private static final String PATH_API_VERSION = "2.5";

    //uri footer
    private static final String QUERY_UNIT = "units";
    private static final String QUERY_APP_ID = "APPID";
    private static final String UNIT_IMPERIAL = "imperial";
    private static final String APP_ID = "3284992e5bfef187c44863ce0f31ad30";

    /**
     * Performs the necessary pre-network checks and returns true if we are ok to go, or false otherwise.
     * @param context
     * @return
     */
    public static boolean isPreNetworkCheckSuccessful(Context context) {
        //check if we have the latest SSL, and if this fails, exit
        if (!PlayServicesUtils.ensureLatestSSL(context)) {
            return false;
        }

        //no network, so do nothing and return
        if (ConnectivityUtils.isNotConnected(context)) {
            Log.d(TAG, "isPreNetworkCheckSuccessful: Not connected to network");
            return false;
        }
        return true;
    }

    /**
     * Builds the header part of the uri and returns the uri builder.
     * @return
     */
    @NonNull
    public static Uri.Builder getHeaderForUriBuilder() {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(AUTHORITY)
                .appendPath(PATH_DATA)
                .appendPath(PATH_API_VERSION);
    }

    /**
     * Appends the footer part to the given uri builder and returns it
     * @param uriBuilder
     * @return
     */
    public static Uri.Builder appendFooterToUriBuilder(@NonNull Uri.Builder uriBuilder) {
        uriBuilder.appendQueryParameter(QUERY_UNIT, UNIT_IMPERIAL)
                .appendQueryParameter(QUERY_APP_ID, APP_ID);
        return uriBuilder;
    }

    /**
     * Converts the uri builder into a URL.
     * @param uriBuilder
     * @return
     * @throws MalformedURLException
     */
    @NonNull
    public static URL buildUrl(@NonNull Uri.Builder uriBuilder) throws MalformedURLException {
        Uri uri = uriBuilder.build();
        Log.d(TAG, "buildUrl: " + uri.toString());
        return new URL(uri.toString());
    }

    /**
     * Calls the weather API given the url.
     * If http status is 200, returns the http url connection.  Caller is responsible for disconnecting.
     * Otherwise, for other status codes or exceptions, this method disconnects and returns null.  Callers should check for null.
     *
     * @param url
     * @return
     */
    @Nullable
    //public static HttpURLConnection performGet(URL url) throws IOException {
    public static HttpURLConnection performGet(URL url) {
        Log.d(TAG, "performGet:");
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod(HTTP_REQUEST_METHOD);
            urlConnection.setConnectTimeout(HTTP_CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(HTTP_READ_TIMEOUT_MILLIS);
            urlConnection.connect();

            //check the response code and process accordingly
            int httpStatus = urlConnection.getResponseCode();
            Log.d(TAG, "performGet: HTTP status:" + httpStatus);
            if (httpStatus == HttpURLConnection.HTTP_OK) {
                return urlConnection;
            }
            else {
                FetchDataUtils.logErrorStream(urlConnection.getErrorStream());
            }
        }
        catch (IOException e) {
            Log.e(TAG, "performGet: Unexpected IO exception:", e);
        }

        //if we reached this, it means we had gotten something other than status 200
        if (urlConnection != null) {
            //previously we disconnected in a finally clause, but we can't anymore since we are returning the connection
            //so disconnect here only on error.
            Log.d(TAG, "performGet: Disconnecting urlConnection");
            urlConnection.disconnect();
        }
        return null;
    }


    /**
     * Helper method that tries to get the content encoding from the response header
     * @param urlConnection
     * @return
     */
    @Nullable
    public static String getEncodingFromHeader(HttpURLConnection urlConnection) {
        //first try to get it from content encoding
        String encoding = urlConnection.getContentEncoding();
        if (encoding != null) {
            return encoding;
        }

        //next try to get it from content type
        String contentType = urlConnection.getContentType();
        if (contentType == null) {
            return null;
        }

        //parse content type
        //content type is likely of the form: application/json; charset=utf-8
        String[] values = contentType.split(";");

        for (String value : values) {
            value = value.trim();
            if (value.toLowerCase().startsWith("charset=")) {
                encoding = value.substring("charset=".length());
            }
        }

        //Log.d(TAG, "getEncodingFromContentTypeHeader: Encoding:" + encoding);
        return (encoding == null || encoding.equals("")) ? null : encoding;
    }

    /**
     * Log the error stream when something goes wrong with the http connection.
     * @param errorStream
     * @throws java.io.IOException
     */
    public static void logErrorStream(@Nullable InputStream errorStream) throws IOException {
        if (errorStream == null) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, DEFAULT_ENCODING));
        try {
            String result;
            while ((result = reader.readLine()) != null) {
                builder.append(result);
            }
        }
        finally {
            reader.close();
        }

        Log.w(TAG, String.format("logErrorStream: %s", builder.toString()));
    }

    /**
     * Helper method that just logs the values in the valuesArray.
     * @param valuesArray
     */
    private static void logContentValuesArray(@Nullable ContentValues[] valuesArray) {
        if (valuesArray != null) {
            for (ContentValues values : valuesArray) {
                Log.d(TAG, "Values:" + values);
            }
        }
    }
}
