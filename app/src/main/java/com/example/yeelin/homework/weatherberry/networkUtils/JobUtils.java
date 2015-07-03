package com.example.yeelin.homework.weatherberry.networkUtils;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.example.yeelin.homework.weatherberry.service.NetworkJobService;

/**
 * Created by ninjakiki on 4/30/15.
 * Helps with the creation and scheduling of jobs
 */
public class JobUtils {
    //logcat
    private static final String TAG = JobUtils.class.getCanonicalName();

    //job info
    private static final int JOB_ID = 100;

    //alarm intervals
    private static final long THREE_MINUTES_MILLIS = 3 * 60 * 1000; //TODO: use this for testing
    private static final long ONE_HOUR_MILLIS = 60 * 60 * 1000; //TODO: use this for shipping
    private static final long JOB_INTERVAL = ONE_HOUR_MILLIS;

    /**
     * Schedules a job (encapsulated in a JobInfo object) with the JobScheduler.
     * The job will be executed on the NetworkJobService.
     * @param context
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void scheduleJob(Context context) {
        //create a job
        JobInfo newJob = buildJobInfo(context);

        //ask the job scheduler to schedule it
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int result = jobScheduler.schedule(newJob);

        //check the result
        if (result == JobScheduler.RESULT_FAILURE) {
            Log.w(TAG, "scheduleJob: Job failed to be scheduled");
        }
        else {
            Log.d(TAG, "scheduleJob: Job scheduled");
        }
    }

    /**
     * Helper method that creates a jobInfo object.
     * @param context
     * @return
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static JobInfo buildJobInfo(Context context) {
        return new JobInfo.Builder(JOB_ID, new ComponentName(context, NetworkJobService.class))
                //run periodically
                .setPeriodic(JOB_INTERVAL)
                //run across reboots
                .setPersisted(true)
                //require network access
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build();
    }
}
