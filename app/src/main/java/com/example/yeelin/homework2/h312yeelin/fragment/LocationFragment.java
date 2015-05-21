package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;


/**
 * Created by ninjakiki on 5/21/15.
 * This fragment has no UI.  It's sole purpose is to get the current location from
 * google play services, and the make a network call to fetch the data, and save it
 * to the database.
 */
public class LocationFragment
        extends Fragment
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    //logcat
    private static final String TAG = LocationFragment.class.getCanonicalName();

    //member variables
    private GoogleApiClient googleApiClient;
    private Location lastLocation;

    /**
     * Required empty public constructor
     */
    public LocationFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleApiClient = new GoogleApiClient
                .Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
