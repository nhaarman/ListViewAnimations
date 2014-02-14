/*
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;

/**
 * Adds an option to swipe items in an AbsListView away.
 * Do not call {@link android.widget.AbsListView#setOnTouchListener(android.view.View.OnTouchListener)} or
 * {@link android.widget.AbsListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)} on your AbsListView! To use an {@link android.widget.AbsListView.OnScrollListener},
 * extends {@link SwipeOnScrollListener} and
 * pass it in the constructor {@link #SwipeDismissAdapter(android.widget.BaseAdapter, com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback, SwipeOnScrollListener)}.
 */
public class SwipeDismissAdapter extends BaseAdapterDecorator {

    protected OnDismissCallback mOnDismissCallback;
    protected SwipeDismissListViewTouchListener mSwipeDismissListViewTouchListener;
    protected SwipeOnScrollListener mSwipeOnScrollListener;

    /**
     * Create a new SwipeDismissAdapter.
     *
     * @param baseAdapter       the {@link android.widget.BaseAdapter to use}
     * @param onDismissCallback the {@link OnDismissCallback} to be notified of dismissed items.
     */
    public SwipeDismissAdapter(final BaseAdapter baseAdapter, final OnDismissCallback onDismissCallback) {
        this(baseAdapter, onDismissCallback, new SwipeOnScrollListener());
    }

    /**
     * Create a new SwipeDismissAdapter.
     *
     * @param baseAdapter           the {@link android.widget.BaseAdapter to use}
     * @param onDismissCallback     the {@link OnDismissCallback} to be notified of dismissed items.
     * @param swipeOnScrollListener the {@link SwipeOnScrollListener} to use.
     */
    public SwipeDismissAdapter(final BaseAdapter baseAdapter, final OnDismissCallback onDismissCallback, final SwipeOnScrollListener swipeOnScrollListener) {
        super(baseAdapter);
        mOnDismissCallback = onDismissCallback;
        mSwipeOnScrollListener = swipeOnScrollListener;
    }

    protected SwipeDismissListViewTouchListener createListViewTouchListener(final AbsListView listView) {
        return new SwipeDismissListViewTouchListener(listView, mOnDismissCallback, mSwipeOnScrollListener);
    }

    @Override
    public void setAbsListView(final AbsListView listView) {
        super.setAbsListView(listView);
        if (mDecoratedBaseAdapter instanceof ArrayAdapter<?>) {
            ((ArrayAdapter<?>) mDecoratedBaseAdapter).propagateNotifyDataSetChanged(this);
        }
        mSwipeDismissListViewTouchListener = createListViewTouchListener(listView);
        mSwipeDismissListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer());
        mSwipeDismissListViewTouchListener.setTouchChild(getTouchChild());
        listView.setOnTouchListener(mSwipeDismissListViewTouchListener);
    }

    @Override
    public void setIsParentHorizontalScrollContainer(final boolean isParentHorizontalScrollContainer) {
        super.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
        if (mSwipeDismissListViewTouchListener != null) {
            mSwipeDismissListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mSwipeDismissListViewTouchListener != null) {
            mSwipeDismissListViewTouchListener.notifyDataSetChanged();
        }
    }

    @Override
    public void setTouchChild(final int childResId) {
        super.setTouchChild(childResId);
        if (mSwipeDismissListViewTouchListener != null) {
            mSwipeDismissListViewTouchListener.setTouchChild(childResId);
        }
    }
}
