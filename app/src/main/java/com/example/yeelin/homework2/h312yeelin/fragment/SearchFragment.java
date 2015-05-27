package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.widget.Toast;

import com.example.yeelin.homework2.h312yeelin.R;
import com.example.yeelin.homework2.h312yeelin.activity.DummyActivity;
import com.example.yeelin.homework2.h312yeelin.adapter.SearchAdapter;
import com.example.yeelin.homework2.h312yeelin.adapter.SearchResultItem;
import com.example.yeelin.homework2.h312yeelin.service.NetworkIntentService;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by ninjakiki on 5/13/15.
 */
public class SearchFragment
        extends BasePlayServicesFragment
        implements AdapterView.OnItemClickListener,
        SearchView.OnQueryTextListener,
        ResultCallback<AutocompletePredictionBuffer> {

    //logcat
    private static final String TAG = SearchFragment.class.getCanonicalName();

    //other constants
    private static final int MINIMUM_QUERY_TEXT_LENGTH = 2;
    private static final int PENDING_RESULT_TIME_SECONDS = 30; //30 seconds
    private static final LatLng US_SW = new LatLng(24.498899, -124.422985);
    private static final LatLng US_NE = new LatLng(48.902104, -67.008434);
    private static final LatLngBounds US_LAT_LNG_BOUNDS = new LatLngBounds(US_SW, US_NE); //rectangle encapsulating USA

    //member variables
    private PendingResult<AutocompletePredictionBuffer> autocompletePendingResult;

    //listener member variable
    private SearchFragmentListener searchListener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface SearchFragmentListener extends BasePlayServicesFragmentListener {
        public void onPlaceSelected(String name, double latitude, double longitude, List<Integer> placeTypes);
    }

    /**
     * Required empty constructor
     */
    public SearchFragment() {}

    /**
     * Creates a new google api client builder that does places search
     * @return
     */
    @Override
    @NonNull
    public GoogleApiClient.Builder buildGoogleApiClient() {
        return new GoogleApiClient
                .Builder(getActivity())
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API);
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
            searchListener = (SearchFragmentListener) objectToCast;
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

        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchView.setOnQueryTextListener(this); //listen for user actions within the search view
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
        searchListener = null;
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
    public void onItemClick(final AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");

        SearchResultItem searchResultItem = (SearchResultItem) parent.getItemAtPosition(position);
        Log.d(TAG, String.format("onItemClick: Description:%s PlaceId:%s", searchResultItem.getDescription(), searchResultItem.getPlaceId()));

        Places.GeoDataApi.getPlaceById(googleApiClient, searchResultItem.getPlaceId())
                .setResultCallback(new ResultCallback<PlaceBuffer>() {
                    @Override
                    public void onResult(PlaceBuffer places) {
                        Status placesStatus = places.getStatus();
                        if (!placesStatus.isSuccess()) {
                            //failed to get results, so log error
                            places.release();
                            Log.e(TAG, "onResult: Error contacting getPlaceById API: " + placesStatus.toString());
                            return;
                        }

                        //found a place matching the placeId
                        final Place foundPlace = places.get(0);
                        Log.d(TAG, String.format("onResult: Found place. Name:%s LatLng:%f, %f PlaceTypes:%s",
                                foundPlace.getName().toString(), foundPlace.getLatLng().latitude, foundPlace.getLatLng().longitude, foundPlace.getPlaceTypes().toString()));

                        //fetch weather data for the favorited city from the network
                        Intent favCityLoadIntent = NetworkIntentService.buildIntentForFavoriteCityLoad(
                                getActivity(),
                                foundPlace.getName().toString(),
                                foundPlace.getLatLng().latitude,
                                foundPlace.getLatLng().longitude);
                        getActivity().startService(favCityLoadIntent);

                        //notify the listener
                        searchListener.onPlaceSelected(
                                foundPlace.getName().toString(),
                                foundPlace.getLatLng().latitude,
                                foundPlace.getLatLng().longitude,
                                foundPlace.getPlaceTypes());
                        //release the places buffer, foundPlace shouldn't be used after this
                        places.release();
                    }
                });
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
        if (!googleApiClient.isConnected() || newText.length() < MINIMUM_QUERY_TEXT_LENGTH) {
            return true;
        }

        Log.d(TAG, "onQueryTextChange: Query:" + newText);

        //clear out any old pending results
        if (autocompletePendingResult != null) {
            autocompletePendingResult.cancel();
            autocompletePendingResult = null;
        }

        //send the query to the places api
        autocompletePendingResult = Places.GeoDataApi.getAutocompletePredictions(
                googleApiClient,
                newText, //user query
                US_LAT_LNG_BOUNDS, //restrict results to a bounding rectangle that encapsulates the US
                null); //no autocomplete filter
        autocompletePendingResult.setResultCallback(this, PENDING_RESULT_TIME_SECONDS, TimeUnit.SECONDS);

        return true;
    }

    /**
     * ResultCallback<AutocompletePredictionBuffer> implementation
     * @param autocompletePredictions
     */
    @Override
    public void onResult(AutocompletePredictionBuffer autocompletePredictions) {
        autocompletePendingResult = null;

        final Status status = autocompletePredictions.getStatus();
        if (!status.isSuccess()) {
            //failed to get autocomplete results
            autocompletePredictions.release();

            //notify the user and log the error
            Toast.makeText(getActivity(), "Error contacting Google Places Autocomplete API: " + status.toString(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "onResult: Error contacting Google Places Autocomplete API: " + status.toString());
            return;
        }

        //we have autocomplete results
        //check if we still have a view
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            //too late, view is gone
            autocompletePredictions.release();
            Log.d(TAG, "onResult: View is null");
            return;
        }

        //good, we still have a view so populate it
        //read search results from the predictions buffer and then release it
        ArrayList<SearchResultItem> searchResultItems = SearchResultItem.buildSearchResultItems(autocompletePredictions);
        autocompletePredictions.release();

        //set the adapter with the search results
        SearchAdapter searchAdapter = new SearchAdapter(viewHolder.searchListView.getContext(), searchResultItems);
        viewHolder.searchListView.setAdapter(searchAdapter);

        Log.d(TAG, "onResult: Done");
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
