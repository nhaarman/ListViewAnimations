package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

public class AbsListViewWrapper implements ListViewWrapper {

    @NonNull
    private final AbsListView mAbsListView;

    public AbsListViewWrapper(@NonNull final AbsListView absListView) {
        mAbsListView = absListView;
    }

    @Override
    @NonNull
    public AbsListView getListView() {
        return mAbsListView;
    }

    @Nullable
    @Override
    public View getChildAt(final int index) {
        return mAbsListView.getChildAt(index);
    }

    @Override
    public int getFirstVisiblePosition() {
        return mAbsListView.getFirstVisiblePosition();
    }

    @Override
    public int getLastVisiblePosition() {
        return mAbsListView.getLastVisiblePosition();
    }

    @Override
    public int getCount() {
        return mAbsListView.getCount();
    }

    @Override
    public int getChildCount() {
        return mAbsListView.getChildCount();
    }

    @Override
    public int getHeaderViewsCount() {
        int result = 0;
        if (mAbsListView instanceof ListView) {
            result = ((ListView) mAbsListView).getHeaderViewsCount();
        }
        return result;
    }

    @Override
    public int getPositionForView(@NonNull final View view) {
        return mAbsListView.getPositionForView(view);
    }

    @NonNull
    @Override
    public ListAdapter getAdapter() {
        return mAbsListView.getAdapter();
    }

    @Override
    public void smoothScrollBy(final int distance, final int duration) {
        mAbsListView.smoothScrollBy(distance, duration);
    }
}
