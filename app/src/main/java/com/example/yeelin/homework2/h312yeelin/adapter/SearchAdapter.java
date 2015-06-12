package com.example.yeelin.homework2.h312yeelin.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.yeelin.homework2.h312yeelin.R;

import java.util.List;

/**
 * Created by ninjakiki on 5/13/15.
 */
public class SearchAdapter extends ArrayAdapter<SearchResultItem> {

    public SearchAdapter(Context context, List<SearchResultItem> items) {
        super(context, 0, items);
    }

    /**
     * Recycles view
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_search, parent, false);
        }

        TextView searchTextView = (TextView) view.findViewById(R.id.search_result_item);
        searchTextView.setText(getItem(position).getDescription());
        return view;
    }

    /**
     * Removes all items from the underlying array and then repopulates it with new items
     * @param items
     */
    public void updateAllItems(List<SearchResultItem> items) {
        //remove all items from the list
        clear();
        //add all items to the end of the array
        addAll(items);
    }
}
