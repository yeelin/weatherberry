package com.example.yeelin.homework2.h312yeelin.activity;

import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by ninjakiki on 5/13/15.
 * The Activity that receives the search query in the intent's Query extra, searches the data,
 * and displays the search results.  This is declared in the manifest as such.
 * However, since we are using autocomplete, we would never get here.  Regardless,
 * Android requires that we have a "searchable activity", so here it is.
 */
public class DummyActivity extends AppCompatActivity {
    //logcat
    private static final String TAG = DummyActivity.class.getCanonicalName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Log.d(TAG, "onCreate: Query in intent:" + query);
        }
        finish();
        Log.d(TAG, "onCreate: Finished");
    }
}
