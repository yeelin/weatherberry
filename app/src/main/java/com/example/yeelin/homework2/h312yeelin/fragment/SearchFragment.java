package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.activity.DummyActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

/**
 * Created by ninjakiki on 5/13/15.
 */
public class SearchFragment
        extends Fragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //logcat
    private static final String TAG = SearchFragment.class.getCanonicalName();

    //states for SaveInstanceState
    private static final String EXTRA_RESOLVING_ERROR = SearchFragment.class.getSimpleName() + ".resolvingError";

    //other constants
    private static final int MINIMUM_QUERY_TEXT_LENGTH = 2;
    private static final int RESOLVE_ERROR_REQUEST = 110;

    //member variables
    private GoogleApiClient googleApiClient;
    private boolean resolvingError;

    //listener member variable
    private SearchFragmentListener listener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface SearchFragmentListener {
        public void showPlayServicesErrorDialog(int errorCode);

        public void addCity();
    }

    /**
     * Required empty constructor
     */
    public SearchFragment() {
    }

    /**
     * Activity uses this method to notify the fragment that play services error
     * has been resolved. If not connected, or connecting, restart the connection process.
     */
    public void onPlayServicesAvailable() {
        Log.d(TAG, "onPlayServicesAvailable");

        resolvingError = false;

        if (!googleApiClient.isConnected() && !googleApiClient.isConnecting()) {
            googleApiClient.connect();
            Log.d(TAG, "onPlayServicesAvailable: Connect called");
        }
    }

    /**
     * Make sure the hosting activity or fragment implements the listener interface.
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;

        try {
            listener = (SearchFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                    + " must implement SearchFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //notify that we have an options menu so that we get the callback to create one later
        setHasOptionsMenu(true);

        //read the saved instance state if it's not null
        resolvingError = savedInstanceState != null && savedInstanceState.getBoolean(EXTRA_RESOLVING_ERROR, false);

        //create a google api client instance
        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    /**
     * Inflate the fragment's view.
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    /**
     * Configure the fragment's view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //set view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //setup listview
        viewHolder.searchListView.setOnItemClickListener(this);
    }

    /**
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_search, menu);

        //get the search view and set the searchable configuration
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        //reference to the dummy activity that will handle the search result
        ComponentName componentName = new ComponentName(getActivity(), DummyActivity.class);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));

        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint(getString(R.string.search_query_hint));
        //searchView.setIconifiedByDefault(false); //do not iconify the widget, expand it by default
    }

    /**
     *
     */
    @Override
    public void onStart() {
        super.onStart();

        //if resolving error, don't start the client. It is already started.
        if (!resolvingError) {
            googleApiClient.connect();
        }
    }

    /**
     * Save out the resolving error in case we get destroyed
     * @param outState
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_RESOLVING_ERROR, resolvingError);
    }

    /**
     *
     */
    @Override
    public void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    /**
     * AdapterView.OnItemClickListener implementation
     * Handle clicks on a search result.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
        listener.addCity();
    }

    /**
     * SearchView.OnQueryTextListener implementation
     * Returns true since we are handling the action and don't want the SearchView to perform
     * the default action of launching the associated intent.
     *
     * @param query
     * @return
     */
    @Override
    public boolean onQueryTextSubmit(String query) {
        //do nothing
        return true;
    }

    /**
     * SearchView.OnQueryTextListener implementation
     * Returns true since we are handling the action and don't want the SearchView to perform
     * the default action of showing suggestions.
     * @param newText
     * @return
     */
    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.length() < MINIMUM_QUERY_TEXT_LENGTH) {
            return true;
        }
        Log.d(TAG, "onQueryTextChange: Query:" + newText);

        return true;
    }

    /**
     * GoogleApiClient.ConnectionCallbacks implementation
     * This callback happens when we are connected to Google Play Services.
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected: Connected to Google Play Services!");
    }

    /**
     * GoogleApiClient.ConnectionCallbacks implementation
     * This callback happens when our connection is interrupted.
     * @param i
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: Connection suspended");
    }

    /**
     * GoogleApiClient.OnConnectionFailedListener implementation
     * Handle errors that occurred while attempting to connect with Google.
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        if (resolvingError) {
            //already attempting to resolve the error
            return;
        }

        if (connectionResult.hasResolution()) {
            //If it returns true, you can request the user take immediate action to resolve the error
            try {
                resolvingError = true;
                connectionResult.startResolutionForResult(getActivity(), RESOLVE_ERROR_REQUEST);

            }
            catch (IntentSender.SendIntentException e) {
                //there was an error with the resolution. try again.
                googleApiClient.connect();
                resolvingError = false;
            }
        }
        else {
            // Show error dialog using GooglePlayServicesUtil.getErrorDialog()
            listener.showPlayServicesErrorDialog(connectionResult.getErrorCode());
            resolvingError = true;
        }
    }

    /**
     * Returns the view holder for the fragment's view if one exists.
     * @return
     */
    @Nullable
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * ViewHolder class
     */
    private class ViewHolder {
        //member variables
        final ListView searchListView;
        final View searchAttribution;

        ViewHolder(View view) {
            searchListView = (ListView) view.findViewById(R.id.search_listview);
            searchListView.setEmptyView(view.findViewById(R.id.search_empty));
            searchAttribution = view.findViewById(R.id.search_attribution);
        }
    }
}
