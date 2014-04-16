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
package com.nhaarman.listviewanimations;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nhaarman.listviewanimations.widget.DynamicListView;
import com.nhaarman.listviewanimations.widget.DynamicListView.Swappable;

/**
 * A decorator class that enables decoration of an instance of the {@link BaseAdapter} class.
 *
 * Classes extending this class can override methods and provide extra functionality before or after calling the super method.
 */
public abstract class BaseAdapterDecorator extends BaseAdapter implements SectionIndexer, Swappable, ListViewSetter {

    /**
     * The {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    private final BaseAdapter mDecoratedBaseAdapter;

    /**
     * The {@link android.widget.AbsListView} this {@code BaseAdapterDecorator} will be bound to.
     */
    private AbsListView mAbsListView;

    /**
     * A boolean to indicate whether the {@link android.widget.AbsListView} is in a horizontal scroll container.
     * @deprecated to be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    private boolean mIsParentHorizontalScrollContainer;

    /**
     * The resource id of the child that can be used to swipe a view away.
     * @deprecated to be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    private int mResIdTouchChild;

    /**
     * Create a new {@code BaseAdapterDecorator}, decorating given {@link android.widget.BaseAdapter}.
     * @param baseAdapter the {@code} BaseAdapter to decorate.
     */
    public BaseAdapterDecorator(final BaseAdapter baseAdapter) {
        mDecoratedBaseAdapter = baseAdapter;
    }

    /**
     * Returns the {@link android.widget.BaseAdapter} that this {@code BaseAdapterDecorator} decorates.
     */
    protected BaseAdapter getDecoratedBaseAdapter() {
        return mDecoratedBaseAdapter;
    }

    /**
     * Returns the root {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    protected BaseAdapter getRootAdapter() {
        BaseAdapter adapter = mDecoratedBaseAdapter;
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }
        return adapter;
    }

    @Override
    /**
     * Sets the {@link android.widget.AbsListView} that this adapter will be bound to.
     * Call this method before setting this adapter to the {@code AbsListView}.
     * Also propagates the {@code AbsListView} to the decorated {@code BaseAdapter} if applicable.
     */
    public void setAbsListView(final AbsListView absListView) {
        mAbsListView = absListView;

        if (mDecoratedBaseAdapter instanceof ListViewSetter) {
            ((ListViewSetter) mDecoratedBaseAdapter).setAbsListView(absListView);
        }

        // TODO: 2.8.+: remove
        if (mAbsListView instanceof DynamicListView) {
            DynamicListView dynListView = (DynamicListView) mAbsListView;
            dynListView.setIsParentHorizontalScrollContainer(mIsParentHorizontalScrollContainer);
            dynListView.setDynamicTouchChild(mResIdTouchChild);
        }
    }

    /**
     * Returns the {@link android.widget.AbsListView} this {@code BaseAdapterDecorator} is bound to.
     */
    protected AbsListView getAbsListView() {
        return mAbsListView;
    }

    @Override
    public int getCount() {
        return mDecoratedBaseAdapter.getCount();
    }

    @Override
    public Object getItem(final int position) {
        return mDecoratedBaseAdapter.getItem(position);
    }

    @Override
    public long getItemId(final int position) {
        return mDecoratedBaseAdapter.getItemId(position);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        return mDecoratedBaseAdapter.getView(position, convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mDecoratedBaseAdapter.areAllItemsEnabled();
    }

    @Override
    public View getDropDownView(final int position, final View convertView, final ViewGroup parent) {
        return mDecoratedBaseAdapter.getDropDownView(position, convertView, parent);
    }

    @Override
    public int getItemViewType(final int position) {
        return mDecoratedBaseAdapter.getItemViewType(position);
    }

    @Override
    public int getViewTypeCount() {
        return mDecoratedBaseAdapter.getViewTypeCount();
    }

    @Override
    public boolean hasStableIds() {
        return mDecoratedBaseAdapter.hasStableIds();
    }

    @Override
    public boolean isEmpty() {
        return mDecoratedBaseAdapter.isEmpty();
    }

    @Override
    public boolean isEnabled(final int position) {
        return mDecoratedBaseAdapter.isEnabled(position);
    }

    @Override
    public void notifyDataSetChanged() {
        if (!(mDecoratedBaseAdapter instanceof ArrayAdapter<?>)) {
            // fix #35 dirty trick !
            // leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
            // TODO: investigate
            mDecoratedBaseAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Helper function if you want to force notifyDataSetChanged()
     */
    @SuppressWarnings("UnusedDeclaration")
    public void notifyDataSetChanged(final boolean force) {
        if (force || !(mDecoratedBaseAdapter instanceof ArrayAdapter<?>)) {
            // leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
            // TODO: investigate
            mDecoratedBaseAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataSetInvalidated() {
        mDecoratedBaseAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void registerDataSetObserver(final DataSetObserver observer) {
        mDecoratedBaseAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(final DataSetObserver observer) {
        mDecoratedBaseAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public int getPositionForSection(final int sectionIndex) {
        int result = 0;
        if (mDecoratedBaseAdapter instanceof SectionIndexer) {
            result = ((SectionIndexer) mDecoratedBaseAdapter).getPositionForSection(sectionIndex);
        }
        return result;
    }

    @Override
    public int getSectionForPosition(final int position) {
        int result = 0;
        if (mDecoratedBaseAdapter instanceof SectionIndexer) {
            result = ((SectionIndexer) mDecoratedBaseAdapter).getSectionForPosition(position);
        }
        return result;
    }

    @Override
    public Object[] getSections() {
        Object[] result = null;
        if (mDecoratedBaseAdapter instanceof SectionIndexer) {
            result = ((SectionIndexer) mDecoratedBaseAdapter).getSections();
        }
        return result;
    }

    @Override
    public void swapItems(final int positionOne, final int positionTwo) {
        if (mDecoratedBaseAdapter instanceof Swappable) {
            ((Swappable) mDecoratedBaseAdapter).swapItems(positionOne, positionTwo);
        }
    }

    /**
     * If the adapter's list-view is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch-events from reaching the list-view.
     *
     * Call this method with the value 'true' to fix this behavior.
     * Note that this will prevent the parent from scrolling horizontally when the user touches anywhere in a list-item.
     * @deprecated use {@link com.nhaarman.listviewanimations.widget.DynamicListView#setParentIsHorizontalScrollContainer()} instead. To be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    public void setIsParentHorizontalScrollContainer(final boolean isParentHorizontalScrollContainer) {
        mIsParentHorizontalScrollContainer = isParentHorizontalScrollContainer;
        if (mAbsListView instanceof DynamicListView) {
            DynamicListView dynListView = (DynamicListView) mAbsListView;
            dynListView.setIsParentHorizontalScrollContainer(mIsParentHorizontalScrollContainer);
        }
    }

    /**
     * @deprecated to be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    public boolean isParentHorizontalScrollContainer() {
        return mIsParentHorizontalScrollContainer;
    }

    /**
     * If the adapter's list-view is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch-events from reaching the list-view.
     *
     * If a list-item view has a child with the given resource-ID, the user can still swipe the list-item by touching that child.
     * If the user touches an area outside that child (but inside the list-item view), then the swipe will not happen and the parent
     * will do its job instead (scrolling horizontally).
     *
     * @param childResId The resource-ID of the list-items' child that the user should touch to be able to swipe the list items.
     * @deprecated use {@link com.nhaarman.listviewanimations.widget.DynamicListView#setDynamicTouchChild(int)} instead. To be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    public void setTouchChild(final int childResId) {
        mResIdTouchChild = childResId;
        if (mAbsListView instanceof DynamicListView) {
            DynamicListView dynListView = (DynamicListView) mAbsListView;
            dynListView.setDynamicTouchChild(mResIdTouchChild);
        }
    }

    /**
     * @deprecated to be removed in 2.8.+.
     */
    // TODO: 2.8.+: remove
    @Deprecated
    public int getTouchChild() {
        return mResIdTouchChild;
    }
}