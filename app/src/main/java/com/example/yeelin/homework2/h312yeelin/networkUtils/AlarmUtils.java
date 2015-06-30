package com.example.yeelin.homework2.h312yeelin.networkUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.receiver.AlarmBroadcastReceiver;

/**
 * Created by ninjakiki on 5/1/15.
 */
public class AlarmUtils {
    //logcat
    private static final String TAG = AlarmUtils.class.getCanonicalName();

    //alarm intervals
    private static final long THREE_MINUTES_MILLIS = 3 * 60 * 1000; //TODO: use this for testing
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000; //TODO: use this for shipping
    private static final long ALARM_INTERVAL = ONE_HOUR_MILLIS;

    /**
     * Schedules a recurring alarm
     * @param context
     */
    public static void scheduleRecurringAlarm(Context context) {
        Log.d(TAG, "scheduleRecurringAlarm");

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //create the pending intent that will perform a broadcast
        PendingIntent pendingBroadcastIntent = AlarmBroadcastReceiver.buildPendingIntent(context);

        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP, //Alarm time in System.currentTimeMillis instead of elapsed time since boot and will wake up the device when it goes off
                System.currentTimeMillis() + ALARM_INTERVAL, //next alarm
                ALARM_INTERVAL, //alarm interval
                pendingBroadcastIntent); //pending intent

        Log.d(TAG, "scheduleRecurringAlarm: Done");
    }
}
