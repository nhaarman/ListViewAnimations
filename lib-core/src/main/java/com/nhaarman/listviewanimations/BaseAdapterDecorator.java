/*
 * Copyright 2014 Niek Haarman
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.nhaarman.listviewanimations.util.AbsListViewWrapper;
import com.nhaarman.listviewanimations.util.Insertable;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nhaarman.listviewanimations.util.ListViewWrapperSetter;
import com.nhaarman.listviewanimations.util.Swappable;

/**
 * A decorator class that enables decoration of an instance of the {@link BaseAdapter} class.
 * <p/>
 * Classes extending this class can override methods and provide extra functionality before or after calling the super method.
 */
public abstract class BaseAdapterDecorator extends BaseAdapter implements SectionIndexer, Swappable, Insertable, ListViewWrapperSetter {

    /**
     * The {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    private final BaseAdapter mDecoratedBaseAdapter;

    /**
     * The {@link com.nhaarman.listviewanimations.util.ListViewWrapper} containing the ListView this {@code BaseAdapterDecorator} will be bound to.
     */
    @Nullable
    private ListViewWrapper mListViewWrapper;

    /**
     * Create a new {@code BaseAdapterDecorator}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter the {@code} BaseAdapter to decorate.
     */
    protected BaseAdapterDecorator(@NonNull final BaseAdapter baseAdapter) {
        mDecoratedBaseAdapter = baseAdapter;
    }

    /**
     * Returns the {@link android.widget.BaseAdapter} that this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    public BaseAdapter getDecoratedBaseAdapter() {
        return mDecoratedBaseAdapter;
    }

    /**
     * Returns the root {@link android.widget.BaseAdapter} this {@code BaseAdapterDecorator} decorates.
     */
    @NonNull
    protected BaseAdapter getRootAdapter() {
        BaseAdapter adapter = mDecoratedBaseAdapter;
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }
        return adapter;
    }

    public void setAbsListView(@NonNull final AbsListView absListView) {
        setListViewWrapper(new AbsListViewWrapper(absListView));
    }

    /**
     * Returns the {@link com.nhaarman.listviewanimations.util.ListViewWrapper} containing the ListView this {@code BaseAdapterDecorator} is bound to.
     */
    @Nullable
    public ListViewWrapper getListViewWrapper() {
        return mListViewWrapper;
    }

    /**
     * Alternative to {@link #setAbsListView(android.widget.AbsListView)}. Sets the {@link com.nhaarman.listviewanimations.util.ListViewWrapper} which contains the ListView
     * this adapter will be bound to. Call this method before setting this adapter to the ListView. Also propagates to the decorated {@code BaseAdapter} if applicable.
     */
    @Override
    public void setListViewWrapper(@NonNull final ListViewWrapper listViewWrapper) {
        mListViewWrapper = listViewWrapper;

        if (mDecoratedBaseAdapter instanceof ListViewWrapperSetter) {
            ((ListViewWrapperSetter) mDecoratedBaseAdapter).setListViewWrapper(listViewWrapper);
        }
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
    @NonNull
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        return mDecoratedBaseAdapter.getView(position, convertView, parent);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return mDecoratedBaseAdapter.areAllItemsEnabled();
    }

    @Override
    @NonNull
    public View getDropDownView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
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
    public void registerDataSetObserver(@NonNull final DataSetObserver observer) {
        mDecoratedBaseAdapter.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(@NonNull final DataSetObserver observer) {
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
    @NonNull
    public Object[] getSections() {
        Object[] result = new Object[0];
        if (mDecoratedBaseAdapter instanceof SectionIndexer) {
            result = ((SectionIndexer) mDecoratedBaseAdapter).getSections();
        }
        return result;
    }

    @Override
    public void swapItems(final int positionOne, final int positionTwo) {
        if (mDecoratedBaseAdapter instanceof Swappable) {
            ((Swappable) mDecoratedBaseAdapter).swapItems(positionOne, positionTwo);
        } else {
            Log.w("ListViewAnimations", "Warning: swapItems called on an adapter that does not implement Swappable!");
        }
    }

    @Override
    public void add(final int index, @NonNull final Object item) {
        if (mDecoratedBaseAdapter instanceof Insertable) {
            //noinspection rawtypes
            ((Insertable) mDecoratedBaseAdapter).add(index, item);
        } else {
            Log.w("ListViewAnimations", "Warning: add called on an adapter that does not implement Insertable!");
        }
    }
}