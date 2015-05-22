package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.yeelin.homework2.h312yeelin.fragment.BasePlayServicesFragment;
import com.example.yeelin.homework2.h312yeelin.fragment.PlayServicesErrorDialogFragment;

/**
 * Created by ninjakiki on 5/22/15.
 * This is the base activity for all activities that wish to use google play services.
 */
public abstract class BasePlayServicesActivity
        extends AppCompatActivity
        implements BasePlayServicesFragment.BasePlayServicesFragmentListener,
        PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener {
    //logcat
    private static final String TAG = BasePlayServicesActivity.class.getCanonicalName();

    //tag for error dialog fragment
    private static final String TAG_GOOGLE_PLAY_ERROR_DIALOG = BasePlayServicesActivity.class.getSimpleName() + ".googlePlayServicesErrorDialog";

    /**
     * Helper method. Used when no play services are available.
     */
    protected abstract void noPlayServicesAvailable();

    /**
     * Helper method. Used when play services become available.
     */
    protected abstract void onPlayServicesAvailable();

    /**
     * Callback from BasePlayServicesFragmentListener
     * @param errorCode
     */
    @Override
    public void showPlayServicesErrorDialog(int errorCode) {
        DialogFragment errorDialogFragment = PlayServicesErrorDialogFragment.newInstance(errorCode,
                BasePlayServicesFragment.REQUEST_CODE_PLAY_SERVICES_RESOLUTION);
        errorDialogFragment.show(getSupportFragmentManager(), TAG_GOOGLE_PLAY_ERROR_DIALOG);
    }

    /**
     * Callback from PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener
     *
     * This callback happens when the user cancels the PlayServicesErrorDialogFragment without
     * resolving the error.
     */
    @Override
    public void onPlayServicesErrorDialogCancelled() {
        Log.d(TAG, "onPlayServicesErrorDialogCancelled");
        noPlayServicesAvailable();
    }

    /**
     * Callback from Play Services dialogs.  Callback can originate from:
     * 1. connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_PLAY_SERVICES_RESOLUTION) in BasePlayServicesFragment
     * 2. GooglePlayServicesUtil.getErrorDialog(errorCode, getActivity(), requestCode) in PlayServicesErrorDialogFragment
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");

        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        switch (requestCode) {
            case BasePlayServicesFragment.REQUEST_CODE_PLAY_SERVICES_RESOLUTION:
                if (resultCode == RESULT_OK) {
                    // Situation resolved. Can now activate Play Services
                    Log.d(TAG, "onActivityResult: Google play services available");

                    // Notify all fragments that we may have a connection to google play services.
                    // try to limit it to one fragment per activity to keep things simple.
                    // notify the child activity (and in turn, the fragment) that play services are available and to retry the connection.
                    onPlayServicesAvailable();
                }
                else {
                    // Update failed. Do something reasonable.
                    Log.w(TAG, "onActivityResult: Result not ok");
                    noPlayServicesAvailable();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
