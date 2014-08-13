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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.util.Insertable;
import com.nhaarman.listviewanimations.util.Swappable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A true {@link ArrayList} adapter providing access to some of the {@code ArrayList} methods.
 * <p/>
 * Also implements {@link Swappable} for easy object swapping,
 * and {@link com.nhaarman.listviewanimations.util.Insertable} for inserting objects.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ArrayAdapter<T> extends BaseAdapter implements Swappable, Insertable<T> {

    @NonNull
    private final List<T> mItems;

    private BaseAdapter mDataSetChangedSlavedAdapter;

    /**
     * Creates a new ArrayAdapter with an empty {@code List}.
     */
    protected ArrayAdapter() {
        this(null);
    }

    /**
     * Creates a new ArrayAdapter, using (a copy of) given {@code List}, or an empty {@code List} if objects = null.
     */
    protected ArrayAdapter(@Nullable final List<T> objects) {
        if (objects != null) {
            mItems = objects;
        } else {
            mItems = new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    @Override
    @NonNull
    public T getItem(final int position) {
        return mItems.get(position);
    }

    /**
     * Returns the items.
     */
    @NonNull
    public List<T> getItems() {
        return mItems;
    }

    /**
     * Appends the specified element to the end of the {@code List}.
     *
     * @param object the object to add.
     *
     * @return always true.
     */
    public boolean add(@NonNull final T object) {
        boolean result = mItems.add(object);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public void add(final int index, @NonNull final T item) {
        mItems.add(index, item);
        notifyDataSetChanged();
    }

    /**
     * Adds the objects in the specified collection to the end of this List. The objects are added in the order in which they are returned from the collection's iterator.
     *
     * @param collection the collection of objects.
     *
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     */
    public boolean addAll(@NonNull final Collection<? extends T> collection) {
        boolean result = mItems.addAll(collection);
        notifyDataSetChanged();
        return result;
    }

    public boolean contains(final T object) {
        return mItems.contains(object);
    }

    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    public boolean remove(@NonNull final Object object) {
        boolean result = mItems.remove(object);
        notifyDataSetChanged();
        return result;
    }

    @NonNull
    public T remove(final int location) {
        T result = mItems.remove(location);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public void swapItems(final int positionOne, final int positionTwo) {
        T firstItem = mItems.set(positionOne, getItem(positionTwo));
        notifyDataSetChanged();
        mItems.set(positionTwo, firstItem);
    }

    public void propagateNotifyDataSetChanged(@NonNull final BaseAdapter slavedAdapter) {
        mDataSetChangedSlavedAdapter = slavedAdapter;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mDataSetChangedSlavedAdapter != null) {
            mDataSetChangedSlavedAdapter.notifyDataSetChanged();
        }
    }
}
