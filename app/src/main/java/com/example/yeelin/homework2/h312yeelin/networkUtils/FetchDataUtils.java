package com.example.yeelin.homework2.h312yeelin.networkUtils;

import android.content.ContentValues;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
     * Calls the weather API given the url and returns the http url connection or null.
     *
     * @param url
     * @return
     * @throws java.io.IOException
     */
    @Nullable
    public static HttpURLConnection performGet(URL url) throws IOException {
        Log.d(TAG, "performGet");

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
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

            //if we reached this, it means we have an error
            FetchDataUtils.logErrorStream(urlConnection.getErrorStream());
            return null;
        }
        finally {
            //always disconnect regardless of success or failure
            urlConnection.disconnect();
        }
    }


    /**
     * Helper method that tries to get the content encoding from the response header
     * @param urlConnection
     * @return
     */
    @Nullable
    public static String getEncodingFromHeader(HttpURLConnection urlConnection) {
        String encoding = null;

        //first try to get it from content encoding
        encoding = urlConnection.getContentEncoding();
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
        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream, "UTF-8"));
        try {
            String result;
            while ((result = reader.readLine()) != null) {
                builder.append(result);
            }
        }
        finally {
            reader.close();
        }

        Log.w(TAG, builder.toString());
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
