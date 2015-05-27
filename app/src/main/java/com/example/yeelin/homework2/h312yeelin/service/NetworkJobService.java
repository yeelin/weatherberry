package com.example.yeelin.homework2.h312yeelin.service;

import android.annotation.TargetApi;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;

/**
 * Created by ninjakiki on 4/30/15.
 * Processes the scheduled job.
 * This class runs on the thread it's called on, unless stated otherwise.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class NetworkJobService extends JobService {
    //logcat
    private static final String TAG = NetworkJobService.class.getCanonicalName();

    //task
    private FetchDataAsyncTask task;

    /**
     * Start the job on async task using a thread pool
     * @param params
     * @return true since service needs to process the work on a separate thread.
     * False if there is no more work to be done.
     */
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob");

        //create a new async task
        task = new FetchDataAsyncTask(this, params);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        return true;
    }

    /**
     * Cancel the job that the async task is running. Nul
     * @param params
     * @return false to drop job. True is for retry.
     */
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob");

        //cancel the async task
        task.cancel(true);
        task = null;

        return false;
    }

    /**
     * Async task to do the job
     */
    private static class FetchDataAsyncTask
            extends AsyncTask<Void, Void, Void>
            implements FetchDataHelper.FetchDataHelperCallback {

        private final String TAG = FetchDataAsyncTask.class.getCanonicalName();

        private final Context applicationContext;
        private final WeakReference<NetworkJobService> jobServiceWeakReference;
        private final JobParameters jobParameters;

        /**
         * Constructor
         * @param jobService
         * @param jobParameters
         */
        public FetchDataAsyncTask(NetworkJobService jobService, JobParameters jobParameters) {
            applicationContext = jobService.getApplicationContext();
            jobServiceWeakReference = new WeakReference<>(jobService);
            this.jobParameters = jobParameters;
        }

        /**
         * Do stuff in the background. Stuff == download and store data
         * @param params
         * @return
         */
        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");

            FetchDataHelper.handleActionMultiCityLoad(applicationContext, this);
            Log.d(TAG, "doInBackground: Done");

            return null;
        }

        /**
         * Do this when done doing stuff in the background
         * @param aVoid
         */
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            Log.d(TAG, "onPostExecute");
            NetworkJobService jobService = jobServiceWeakReference.get();
            if (jobService == null) {
                //do nothing since the service has gone away
                return;
            }

            //inform job manager that the job is done.
            //when system receives this message, it will release the wakelock
            jobService.jobFinished(jobParameters, false);
            Log.d(TAG, "onPostExecute: Done");
        }

        /**
         * FetchDataHelperCallback method implementation.
         * @return whether the asynctask has been cancelled
         */
        @Override
        public boolean shouldCancelFetch() {
            return isCancelled();
        }
    }
}
