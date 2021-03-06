package com.example.yeelin.homework.weatherberry.fragment;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.homework.weatherberry.R;
import com.example.yeelin.homework.weatherberry.adapter.FavoritesAdapter;
import com.example.yeelin.homework.weatherberry.fragmentUtils.AnimationUtils;
import com.example.yeelin.homework.weatherberry.loader.FavoritesLoaderCallbacks;
import com.example.yeelin.homework.weatherberry.loader.LoaderIds;
import com.example.yeelin.homework.weatherberry.service.NetworkIntentService;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 5/27/15.
 */
public class FavoritesFragment
        extends Fragment
        implements AdapterView.OnItemClickListener,
        FavoritesLoaderCallbacks.FavoritesLoaderListener {
    //logcat
    private static final String TAG = FavoritesFragment.class.getCanonicalName();
    //member variable
    //private HashMap<Integer, Long> selectedItemPositionsToCityIdsMap;

    //listener member variable
    private FavoritesFragmentListener favoritesListener;
    private SparseBooleanArray checkedItemPositions;

    /**
     * Listener interface to be implemented by whoever is interested in events from this fragment.
     */
    public interface FavoritesFragmentListener {
        public void handleSomething();
    }

    /**
     * Required public empty constructor
     */
    public FavoritesFragment() {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment parent = getParentFragment();
        Object objectToCast = parent != null ? parent : activity;
        try {
            favoritesListener = (FavoritesFragmentListener) objectToCast;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(objectToCast.getClass().getSimpleName()
                + " must implement FavoritesFragmentListener");
        }
    }

    /**
     * Configure the fragment
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //notify that we have an options menu so that we will get a callback to create one later
        setHasOptionsMenu(true);
    }

    /**
     * Inflate the fragment's view
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    /**
     * Configure the fragment's view
     * @param view
     * @param savedInstanceState
     */
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //setup view holder
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        //set up the listview adapter
        viewHolder.favoritesListView.setAdapter(new FavoritesAdapter(view.getContext()));
        viewHolder.favoritesListView.setOnItemClickListener(this);
        viewHolder.favoritesListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        //initially make the list container invisible and show the progress bar
        viewHolder.favoritesListContainer.setVisibility(View.GONE);
        viewHolder.favoritesProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //initialize the loader
        FavoritesLoaderCallbacks.initLoader(getActivity(),
                getLoaderManager(),
                this,
                FavoritesAdapter.PROJECTION_FAVORITES);
    }

    /**
     * Configure the delete button in the options menu
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_favorites, menu);
    }

    /**
     * Handle the delete action
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        switch (id) {
            case R.id.action_delete:
//                if (selectedItemPositionsToCityIdsMap != null && selectedItemPositionsToCityIdsMap.size() > 0){
//                    Log.d(TAG, "onOptionsItemSelected: Favorites to be deleted: " + selectedItemPositionsToCityIdsMap);
//
//                    getActivity().startService(NetworkIntentService.buildIntentForFavoriteCitiesPurge(
//                            getActivity(),
//                            new ArrayList<>(selectedItemPositionsToCityIdsMap.values())));
//                }

                ViewHolder viewHolder = getViewHolder();
                SparseBooleanArray sparseBooleanArray = viewHolder.favoritesListView.getCheckedItemPositions();
                if (sparseBooleanArray.size() == 0) {
                    Log.d(TAG, "onOptionsItemSelected: sparse boolean array is empty, so nothing to do");
                    return true;
                }
                Log.d(TAG, "onOptionsItemSelected: sparse boolean array size:" + sparseBooleanArray.size()); //size != number of currently selected rows

                FavoritesAdapter favoritesAdapter = (FavoritesAdapter) viewHolder.favoritesListView.getAdapter();
                ArrayList<Long> cityIdsArrayList = new ArrayList<>(sparseBooleanArray.size());
                //sparse boolean array is map of key-value pairs
                //key = row in the listview, value = true/false to indicate whether row is selected
                //value would be false if the row was checked then unchecked
                for (int i=0; i<sparseBooleanArray.size(); i++) {
                    int key = sparseBooleanArray.keyAt(i);
                    boolean value = sparseBooleanArray.valueAt(i);

                    Log.d(TAG, String.format("onOptionsItemSelected: Index:%d, Key:%d, Value:%s", i, key, String.valueOf(value)));
                    //check if value is true (true means row is currently selected)
                    if (value) {
                        Cursor cursor = (Cursor) favoritesAdapter.getItem(key);
                        long cityId = cursor.getLong(FavoritesAdapter.FavoritesCursorPosition.CITY_ID_POS.getValue());
                        String cityName = cursor.getString(FavoritesAdapter.FavoritesCursorPosition.CITY_NAME_POS.getValue());
                        cityIdsArrayList.add(cityId);
                        Log.d(TAG, String.format("onOptionsItemSelected: Index:%d, Key:%d, Value:%s, CityId:%d, CityName:%s", i, key, String.valueOf(value), cityId, cityName));
                    }
                }

                if (cityIdsArrayList.size() == 0) {
                    Log.d(TAG, "onOptionsItemSelected: CityIds size is 0, so nothing is currently selected.");
                    return true;
                }
                Log.d(TAG, "onOptionsItemSelected: CityIds selected: " + cityIdsArrayList);

                getActivity().startService(
                        NetworkIntentService.buildIntentForFavoriteCitiesPurge(
                        getActivity(),
                        cityIdsArrayList));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Nullify the listener before detaching
     */
    @Override
    public void onDetach() {
        favoritesListener = null;
        super.onDetach();
    }

    /**
     * AdapterView.OnItemClickListener callback when an item in the listview is clicked.
     * All we do here is check the corresponding checkbox for that item
     *
     * @param parent the listview
     * @param view the view that was clicked
     * @param position position in the listview
     * @param id row id in the database
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick: Position clicked:" + position);
        //parent is the listview
        //Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        FavoritesAdapter.ViewHolder viewHolder = (FavoritesAdapter.ViewHolder) view.getTag();

        //change the checked state of the view to the inverse of its current state
        viewHolder.favoriteCity.toggle();

//        if (selectedItemPositionsToCityIdsMap.containsKey(position)) {
//            //the checkbox was previously checked so remove it from map
//            selectedItemPositionsToCityIdsMap.remove(position);
//        }
//        else {
//            //the checkbox was not previously checked, so add it to map
//            long cityId = cursor.getLong(FavoritesAdapter.FavoritesCursorPosition.CITY_ID_POS.getValue());
//            selectedItemPositionsToCityIdsMap.put(position, cityId);
//        }
    }

    /**
     * FavoritesLoader callback with an updated cursor. Update the view.
     * @param loaderId
     * @param cursor
     */
    @Override
    public void onLoadComplete(LoaderIds loaderId, @Nullable Cursor cursor) {
        ViewHolder viewHolder = getViewHolder();
        if (viewHolder == null) {
            Log.d(TAG, "onLoadComplete: view holder is null");
            return;
        }

        if (loaderId == LoaderIds.FAVORITES_LOADER) {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Cursor swapped", loaderId));
            //swap the adapter's cursor
            FavoritesAdapter favoritesAdapter = (FavoritesAdapter) viewHolder.favoritesListView.getAdapter();
            favoritesAdapter.swapCursor(cursor);
            //viewHolder.favoritesListView.clearChoices();

            //clear selected items map and checkboxes
            //selectedItemPositionsToCityIdsMap = new HashMap<>(cursor.getCount());

            //show the list container and hide the progress bar
            if (viewHolder.favoritesListContainer.getVisibility() != View.VISIBLE) {
                AnimationUtils.crossFadeViews(getActivity(),
                        viewHolder.favoritesListContainer,
                        viewHolder.favoritesProgressBar);
            }
        }
        else {
            Log.d(TAG, String.format("onLoadComplete: LoaderId:%s. Unknown loader id:", loaderId));
        }
    }

    /**
     * Returns the view holder for the fragment's view if one exists.
     * @return
     */
    private ViewHolder getViewHolder() {
        View view = getView();
        return view != null ? (ViewHolder) view.getTag() : null;
    }

    /**
     * ViewHolder class
     */
    private class ViewHolder {
        final View favoritesListContainer;
        final ListView favoritesListView;
        final View favoritesProgressBar;

        ViewHolder(View view) {
            favoritesListContainer = view.findViewById(R.id.favorites_listContainer);
            favoritesListView = (ListView) view.findViewById(R.id.favorites_listview);
            favoritesListView.setEmptyView(view.findViewById(R.id.favorites_empty));
            favoritesProgressBar = view.findViewById(R.id.favorites_progressBar);
        }
    }
}
