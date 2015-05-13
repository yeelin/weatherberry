package com.example.yeelin.homework2.h312yeelin.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.fragment.SearchFragment;

/**
 * Created by ninjakiki on 5/12/15.
 */
public class SearchActivity
        extends AppCompatActivity
        implements SearchFragment.SearchFragmentListener {
    private static final String TAG = SearchActivity.class.getCanonicalName();

    /**
     * Builds intent to start this activity
     * @param context
     * @return
     */
    public static Intent buildIntent(Context context) {
        Intent intent = new Intent(context, SearchActivity.class);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        //setupToolbar
        setupToolbar();
    }

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
    public void addCity() {
        Log.d(TAG, "addCity:");
    }

    /**
     * Callback from SearchFragment
     */
    @Override
    public void noPlayServicesAvailable() {
        Log.d(TAG, "noPlayServicesAvailable");
    }
}
