package com.example.yeelin.homework2.h312yeelin.receiver;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.networkUtils.AlarmUtils;
import com.example.yeelin.homework2.h312yeelin.networkUtils.ConnectivityUtils;
import com.example.yeelin.homework2.h312yeelin.service.NetworkIntentService;

/**
 * Created by ninjakiki on 5/1/15.
 */
public class AlarmBroadcastReceiver extends WakefulBroadcastReceiver {
    //logcat
    private static final String TAG = AlarmBroadcastReceiver.class.getCanonicalName();

    //intent and pending intent settings
    private static final int ALARM_BROADCAST_REQUEST_CODE = 100;
    private static final String ACTION_ALARM_TRIGGERED = AlarmBroadcastReceiver.class.getCanonicalName() + ".action.ALARM_TRIGGERED";

    /**
     * Creates a pending intent that wraps the intent with the alarm triggered action.
     * This broadcast receiver has registered to handle this action in the manifest.
     * @param context
     * @return
     */
    public static PendingIntent buildPendingIntent(Context context) {
        //create an intent with alarm triggered action
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        intent.setAction(ACTION_ALARM_TRIGGERED);

        //wrap the intent in a pending intent
        return PendingIntent.getBroadcast(
                context,
                ALARM_BROADCAST_REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");

        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "onReceive: Either the Intent or the action in the intent was null");
            return;
        }

        final String action = intent.getAction();

        if(Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            //alarms are cleared on reboot, so rescheduling the alarm
            AlarmUtils.scheduleRecurringAlarm(context);
            Log.d(TAG, "onReceive: Boot completed, alarm rescheduled");
        }
        else if (ACTION_ALARM_TRIGGERED.equals(action)) {
            //check if we are connected before calling the intent service
            if (ConnectivityUtils.isNotConnected(context)) {
                Log.d(TAG, "onReceive: Not connected. Exiting");
                return;
            }

            //create a wakeful intent for Network intent service
            //start the service
            startWakefulService(context, NetworkIntentService.buildWakefulIntent(context));
            Log.d(TAG, "onReceive: Connected. Started wakeful network intent service");
        }
        else {
            Log.d(TAG, "onReceive: Unknown action:" + action);
        }

        Log.d(TAG, "onReceive: Done");
    }
}
