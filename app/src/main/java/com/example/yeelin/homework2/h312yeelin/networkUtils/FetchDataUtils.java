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

/**
 * Created by ninjakiki on 5/24/15.
 */
public class FetchDataUtils {
    private static final String TAG = FetchDataUtils.class.getCanonicalName();

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
