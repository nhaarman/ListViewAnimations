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
 * Adds an option to swipe items in an {@link AbsListView} away.
 * Do not call {@link android.widget.AbsListView#setOnTouchListener(android.view.View.OnTouchListener)} on your {@code AbsListView}!
 */
public class SwipeDismissAdapter extends BaseAdapterDecorator {

    private final OnDismissCallback mOnDismissCallback;
    private SwipeDismissTouchListener mDismissTouchListener;

    /**
     * A boolean to indicate whether the {@link android.widget.AbsListView} is in a horizontal scroll container.
     */
    private boolean mParentIsHorizontalScrollContainer;

    /**
     * The resource id of the child that can be used to swipe a view away.
     */
    private int mSwipeTouchChildResId;

    /**
     * Create a new SwipeDismissAdapter.
     *
     * @param baseAdapter       the {@link android.widget.BaseAdapter to use}
     * @param onDismissCallback the {@link OnDismissCallback} to be notified of dismissed items.
     */
    public SwipeDismissAdapter(final BaseAdapter baseAdapter, final OnDismissCallback onDismissCallback) {
        super(baseAdapter);
        mOnDismissCallback = onDismissCallback;
    }

    @Override
    public void setAbsListView(final AbsListView absListView) {
        super.setAbsListView(absListView);
        if (getDecoratedBaseAdapter() instanceof ArrayAdapter<?>) {
            ((ArrayAdapter<?>) getDecoratedBaseAdapter()).propagateNotifyDataSetChanged(this);
        }
        mDismissTouchListener = new SwipeDismissTouchListener(absListView, mOnDismissCallback);
        if (mParentIsHorizontalScrollContainer) {
            mDismissTouchListener.setParentIsHorizontalScrollContainer();
        }
        if (mSwipeTouchChildResId != 0) {
            mDismissTouchListener.setTouchChild(mSwipeTouchChildResId);
        }
        absListView.setOnTouchListener(mDismissTouchListener);
    }

    /**
     * If the adapter's {@link AbsListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch-events from reaching the {@code AbsListView}.
     *
     * Call this method to fix this behavior.
     * Note that this will prevent the parent from scrolling horizontally when the user touches anywhere in a list item.
     */
    public void setParentIsHorizontalScrollContainer() {
        mParentIsHorizontalScrollContainer = true;
        mSwipeTouchChildResId = 0;
        if (mDismissTouchListener != null) {
            mDismissTouchListener.setParentIsHorizontalScrollContainer();
        }
    }

    /**
     * If the adapter's {@link AbsListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch events from reaching the {@code AbsListView}.
     *
     * If a {@code AbsListView} view has a child with the given resource id, the user can still swipe the list item by touching that child.
     * If the user touches an area outside that child (but inside the list item view), then the swipe will not happen and the parent
     * will do its job instead (scrolling horizontally).
     *
     * @param childResId The resource id of the list items' child that the user should touch to be able to swipe the list items.
     */
    public void setSwipeTouchChildResId(final int childResId) {
        mSwipeTouchChildResId = childResId;
        if (mDismissTouchListener != null) {
            mDismissTouchListener.setTouchChild(childResId);
        }
    }

    /**
     * Dismisses the {@link android.view.View} corresponding to given position.
     * Calling this method has the same effect as manually swiping an item off the screen.
     * @param position the position of the item in the {@link android.widget.ListAdapter}.
     */
    public void dismiss(final int position) {
        mDismissTouchListener.dismiss(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mDismissTouchListener != null) {
            mDismissTouchListener.notifyDataSetChanged();
        }
    }
}
