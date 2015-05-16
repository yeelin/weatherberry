package com.example.yeelin.homework2.h312yeelin.networkUtils;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;

/**
 * Created by ninjakiki on 5/15/15.
 */
public class PlayServicesUtils {
    private static final String TAG = PlayServicesUtils.class.getCanonicalName();

    /**
     * Helper method that checks the device has the latest security updates.
     * @return
     */
    public static boolean ensureLatestSSL(Context context) {
        try {
            //ensure the latest SSL per
            //http://developer.android.com/training/articles/security-gms-provider.html
            ProviderInstaller.installIfNeeded(context);
            return true;
        }
        catch (GooglePlayServicesRepairableException e) {
            //since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.getConnectionStatusCode(), context);
            Log.d(TAG, "ensureLatestSSL: Repairable error updating SSL");
            return false;
        }
        catch (GooglePlayServicesNotAvailableException e) {
            //since this is a background service, show a notification
            GooglePlayServicesUtil.showErrorNotification(e.errorCode, context);
            Log.d(TAG, "ensureLatestSSL: Missing play servers updating SSL");
            return false;
        }
    }
}
