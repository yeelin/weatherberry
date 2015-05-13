package com.example.yeelin.homework2.h312yeelin.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.yeelin.homework2.h312yeelin.R;

/**
 * Created by ninjakiki on 5/13/15.
 */
public class SearchFragment extends Fragment implements AdapterView.OnItemClickListener {
    //logcat
    private static final String TAG = SearchFragment.class.getCanonicalName();

    //listener member variable
    private SearchFragmentListener listener;

    /**
     * Listener interface. To be implemented by whoever is interested in events from this fragment.
     */
    public interface SearchFragmentListener {
        public void addCity();

        public void noPlayServicesAvailable();
    }

    /**
     * Required empty constructor
     */
    public SearchFragment() {
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
    }

    /**
     * Handle clicks on a search result.
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
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
