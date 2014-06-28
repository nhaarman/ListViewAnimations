package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerViewWrapper implements ListViewWrapper {

    @NonNull
    private final RecyclerView mRecyclerView;

    public RecyclerViewWrapper(@NonNull final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @NonNull
    @Override
    public ViewGroup getListView() {
        return mRecyclerView;
    }

    @Nullable
    @Override
    public View getChildAt(final int index) {
        return mRecyclerView.getChildAt(index);
    }

    @Override
    public int getFirstVisiblePosition() {
        View view = mRecyclerView.getChildAt(0);
        if (view != null) {
            return mRecyclerView.getChildPosition(view);
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getLastVisiblePosition() {
        View view = mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1);
        if (view != null) {
            return mRecyclerView.getChildPosition(view);
        }
        return RecyclerView.NO_POSITION;
    }

    @Override
    public int getCount() {
        return mRecyclerView.getAdapter().getItemCount();
    }

    @Override
    public int getChildCount() {
        return mRecyclerView.getChildCount();
    }

    @Override
    public int getHeaderViewsCount() {
        return 0;
    }

    @Override
    public int getPositionForView(@NonNull final View view) {
        return mRecyclerView.getChildPosition(view);
    }

    @NonNull
    @Override
    public AdapterWrapper getAdapterWrapper() {
        return new RecyclerAdapterWrapper(mRecyclerView.getAdapter());
    }

    @Override
    public void smoothScrollBy(final int distance, final int duration) {
        mRecyclerView.smoothScrollBy(0, distance);
    }
}
