package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

import android.widget.AbsListView;

import com.nhaarman.listviewanimations.util.ListViewWrapper;

public interface DragAndDropListViewWrapper extends ListViewWrapper {

    void setOnScrollListener(AbsListView.OnScrollListener onScrollListener);

    int pointToPosition(int x, int y);

    int computeVerticalScrollOffset();

    int computeVerticalScrollExtent();

    int computeVerticalScrollRange();
}
