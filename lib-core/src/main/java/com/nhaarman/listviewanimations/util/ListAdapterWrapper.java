package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.widget.ListAdapter;

public class ListAdapterWrapper implements AdapterWrapper {

    @NonNull
    private final ListAdapter mListAdapter;

    public ListAdapterWrapper(@NonNull final ListAdapter listAdapter) {
        mListAdapter = listAdapter;
    }

    @Override
    public int getCount() {
        return mListAdapter.getCount();
    }

    @Override
    public long getItemId(final int position) {
        return mListAdapter.getItemId(position);
    }
}
