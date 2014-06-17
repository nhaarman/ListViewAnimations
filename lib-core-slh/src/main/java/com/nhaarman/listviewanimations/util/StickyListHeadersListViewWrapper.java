package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListAdapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class StickyListHeadersListViewWrapper implements ListViewWrapper {

    @NonNull
    private final StickyListHeadersListView mListView;

    public StickyListHeadersListViewWrapper(@NonNull final StickyListHeadersListView listView) {
        mListView = listView;
    }

    @NonNull
    @Override
    public StickyListHeadersListView getListView() {
        return mListView;
    }

    @Nullable
    @Override
    public View getChildAt(final int index) {
        return mListView.getChildAt(index);
    }

    @Override
    public int getFirstVisiblePosition() {
        return mListView.getFirstVisiblePosition();
    }

    @Override
    public int getLastVisiblePosition() {
        return mListView.getLastVisiblePosition();
    }

    @Override
    public int getCount() {
        return mListView.getCount();
    }

    @Override
    public int getChildCount() {
        return mListView.getChildCount();
    }

    @Override
    public int getHeaderViewsCount() {
        return mListView.getHeaderViewsCount();
    }

    @Override
    public int getPositionForView(@NonNull final View view) {
        return mListView.getPositionForView(view);
    }

    @NonNull
    @Override
    public ListAdapter getAdapter() {
        return mListView.getAdapter();
    }

    @Override
    public void smoothScrollBy(final int distance, final int duration) {
        mListView.smoothScrollBy(distance, duration);
    }
}
