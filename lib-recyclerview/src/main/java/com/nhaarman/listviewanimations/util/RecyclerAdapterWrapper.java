package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

public class RecyclerAdapterWrapper<T extends RecyclerView.ViewHolder> implements AdapterWrapper {

    @NonNull
    private final RecyclerView.Adapter<T> mAdapter;

    public RecyclerAdapterWrapper(@NonNull final RecyclerView.Adapter<T> adapter) {
        mAdapter = adapter;
    }

    @Override
    public int getCount() {
        return mAdapter.getItemCount();
    }

    @Override
    public long getItemId(final int position) {
        return mAdapter.getItemId(position);
    }
}
