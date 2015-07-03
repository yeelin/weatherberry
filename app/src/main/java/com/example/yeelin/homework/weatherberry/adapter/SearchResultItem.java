package com.example.yeelin.homework.weatherberry.adapter;

import android.util.Log;

import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/13/15.
 */
public class SearchResultItem {
    private static final String TAG = SearchResultItem.class.getCanonicalName();

    private final String description;
    private final String placeId;

    public SearchResultItem(AutocompletePrediction prediction) {
        description = prediction.getDescription();
        placeId = prediction.getPlaceId();
    }

    public static ArrayList<SearchResultItem> buildSearchResultItems (AutocompletePredictionBuffer predictionBuffer) {
        Log.d(TAG, "buildSearchResultItems");
        ArrayList<SearchResultItem> searchResultItems = new ArrayList<>(predictionBuffer.getCount());

        for (AutocompletePrediction prediction : predictionBuffer) {
            searchResultItems.add(new SearchResultItem(prediction));
        }
        return searchResultItems;
    }

    public String getDescription() {
        return description;
    }

    public String getPlaceId() {
        return placeId;
    }

}
