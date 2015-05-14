package com.example.yeelin.homework2.h312yeelin.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragment.PlayServicesErrorDialogFragment;
import com.example.yeelin.homework2.h312yeelin.fragment.SearchFragment;

import java.util.List;

/**
 * Created by ninjakiki on 5/12/15.
 */
public class SearchActivity
        extends AppCompatActivity
        implements SearchFragment.SearchFragmentListener,
        PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener {
    //logcat
    private static final String TAG = SearchActivity.class.getCanonicalName();
    //error dialog
    private static final String TAG_GOOGLE_PLAY_ERROR_DIALOG = SearchActivity.class.getSimpleName() + ".googlePlayServicesErrorDialog";


    /**
     * Builds intent to start this activity
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //setupToolbar
        setupToolbar();
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(TAG, "Up button clicked");
                navigateUpToParentActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Callback from Play Services dialog result
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");
        // Handle the play services dialog result. This code is used whether the play
        // services dialog fragment was started by the fragment or the activity. Google
        // play services starts the activity such that only the activity is able to handle
        // the request code in onActivityResult.
        if (requestCode == PlayServicesErrorDialogFragment.PLAY_SERVICES_DIALOG_RESULT) {
            if (resultCode == RESULT_OK) {
                // Situation resolved. Can now activate GPS services
                Log.d(TAG, "onActivityResult: Google play services available");

                // Notify all fragments that we may have a connection to google play services.
                // I'd probably try to limit it to one fragment per activity to keep things
                // simple. In this example, need to notify the places fragment that play
                // services are available and to retry the connection.
                SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
                searchFragment.onPlayServicesAvailable();
            }
            else {
                // Update failed. Do something reasonable. Here leaving the app...
                Log.w(TAG, "onActivityResult: Result not ok");
                noPlayServicesAvailable();
            }
        }
    }

    /**
     * Helper method. Used when no play services are available. Shows a toast and then quits.
     */
    private void noPlayServicesAvailable() {
        Log.d(TAG, "noPlayServicesAvailable");
        Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();
        finish();
    }

    /**
     * Provides Up navigation the proper way :)
     *
     * Clear top : if the activity being launched is already in the current task, then instead of launching a new instance,
     * all activities on top of it will be closed, and this intent will be delivered to the old activity as a new intent
     * and will be either finished and recreated OR restarted.
     *
     * Single top: if set, the activity will not be recreated if it is already at the top of the stack.
     */
    private void navigateUpToParentActivity() {
        //get the intent that started the parent activity
        Intent intent = NavUtils.getParentActivityIntent(this);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        NavUtils.navigateUpTo(this, intent);
    }

    /**
     * Helper method to setup toolbar
     */
    private void setupToolbar() {
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

        //enable the Up arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * Callback from SearchFragment
     */
    @Override
    public void showPlayServicesErrorDialog(int errorCode) {
        DialogFragment errorDialogFragment = PlayServicesErrorDialogFragment.newInstance(errorCode);
        errorDialogFragment.show(getSupportFragmentManager(), TAG_GOOGLE_PLAY_ERROR_DIALOG);
    }

    /**
     * Callback from SearchFragment
     * This callback happens when the user selects a search result in the list.
     */
    @Override
    public void onPlaceSelected(String name, double latitude, double longitude, List<Integer> placeTypes) {
        Log.d(TAG, "onPlaceSelected");

        //TODO:close out the search activity and go back to the pager activity
    }

    /**
     * PlayServicesErrorDialogFragment.PlayServicesErrorDialogFragmentListener implementation
     *
     * This callback happens when the user cancels the PlayServicesErrorDialogFragment without
     * resolving the error.
     */
    @Override
    public void onPlayServicesErrorDialogCancelled() {
        Log.d(TAG, "onPlayServicesErrorDialogCancelled");
        noPlayServicesAvailable();
    }
}
