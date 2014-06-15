package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

public interface ListViewWrapper<T extends ViewGroup> {

    @NonNull
    T getListView();

    @Nullable
    View getChildAt(int index);

    int getFirstVisiblePosition();

    int getLastVisiblePosition();

    int getCount();

    int getChildCount();

    int getHeaderViewsCount();

    int getPositionForView(@NonNull View view);

}
