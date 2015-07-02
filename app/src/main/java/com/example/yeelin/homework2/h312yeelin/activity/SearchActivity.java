package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragment.SearchFragment;

import java.util.List;

/**
 * Created by ninjakiki on 5/12/15.
 */
public class SearchActivity
        extends BasePlayServicesActivity
        implements SearchFragment.SearchFragmentListener {
    //logcat
    private static final String TAG = SearchActivity.class.getCanonicalName();

    /**
     * Builds intent to start this activity
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    /**
     * Creates the search activity and sets up the toolbar
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
     * Handle up navigation
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.d(TAG, "Up button clicked");
                navigateUpToParentActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * BasePlayServicesActivity override
     * Helper method. Used when no play services are available. Shows a toast and then navigate back to parent activity
     * since search isn't going to work.
     */
    @Override
    protected void noPlayServicesAvailable() {
        Log.d(TAG, "noPlayServicesAvailable");
        Toast.makeText(this, R.string.play_services_error, Toast.LENGTH_LONG).show();

        //not much else to do here since places search won't work, so navigate back to parent seems logical
        navigateUpToParentActivity();
    }

    /**
     * BasePlayServicesActivity override
     * Helper method. Used when play services become available. Notify search fragment that play services are available
     * and to retry connection.
     */
    @Override
    protected void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable");
        SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.search_fragment);
        if (searchFragment != null) {
            searchFragment.onPlayServicesAvailable();
        }
    }

    /**
     * Callback from SearchFragmentListener
     * This callback happens when the user selects a search result in the list.
     */
    @Override
    public void onPlaceSelected(String name, double latitude, double longitude, List<Integer> placeTypes) {
        Log.d(TAG, "onPlaceSelected");
        navigateUpToParentActivity();
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

        //check if action bar is null
        if (getSupportActionBar() == null) {
            Log.e(TAG, "setupToolbar: getSupportActionBar is null");
            return;
        }

        //set elevation
        getSupportActionBar().setElevation(getResources().getDimensionPixelSize(R.dimen.toolbar_elevation));

        //enable the Up arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
