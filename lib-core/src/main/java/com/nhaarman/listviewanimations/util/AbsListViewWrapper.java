package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

public class AbsListViewWrapper<T extends AbsListView> implements ListViewWrapper<T> {

    @NonNull
    private final T mAbsListView;

    public AbsListViewWrapper(@NonNull final T absListView) {
        mAbsListView = absListView;
    }

    @Override
    @NonNull
    public T getListView() {
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
}
