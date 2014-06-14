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

import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.util.Insertable;
import com.nhaarman.listviewanimations.util.Swappable;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * A {@code true} {@link ArrayList} adapter providing access to all ArrayList methods. Also implements {@link Swappable} for easy object swapping, and {@link
 * com.nhaarman.listviewanimations.util.Insertable} for inserting objects.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ArrayAdapter<T> extends BaseAdapter implements List<T>, Swappable, Insertable<T> {

    @NonNull
    private final List<T> mItems;

    /**
     * Creates a new ArrayAdapter with an empty {@code List}.
     */
    protected ArrayAdapter() {
        this(null);
    }

    /**
     * Creates a new ArrayAdapter using given {@code List}, or an empty {@code List} if objects == null.
     */
    protected ArrayAdapter(@Nullable final List<T> objects) {
        this(objects, false);
    }

    /**
     * Creates a new ArrayAdapter, using (a copy of) given {@code List}, or an empty {@code List} if objects = null.
     *
     * @param copyList {@code true} to create a copy of the {@code List}, {@code false} to reuse the reference.
     */
    protected ArrayAdapter(@Nullable final List<T> objects, final boolean copyList) {
        if (objects != null) {
            if (copyList) {
                mItems = new ArrayList<>(objects);
            } else {
                mItems = objects;
            }
        } else {
            mItems = new ArrayList<>();
        }
    }


    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    @NonNull
    public T getItem(final int position) {
        return mItems.get(position);
    }

    @Override
    public long getItemId(final int position) {
        return position;
    }

    /**
     * Appends the specified element to the end of the {@code List}.
     *
     * @param object the object to add.
     *
     * @return always true.
     */
    @Override
    public boolean add(@NonNull final T object) {
        boolean result = mItems.add(object);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public void add(final int location, @NonNull final T object) {
        mItems.add(location, object);
        notifyDataSetChanged();
    }

    /**
     * Adds the objects in the specified collection to the end of this List. The objects are added in the order in which they are returned from the collection's iterator.
     *
     * @param collection the collection of objects.
     *
     * @return {@code true} if this {@code List} is modified, {@code false} otherwise.
     */
    @Override
    public boolean addAll(@NonNull final Collection<? extends T> collection) {
        boolean result = mItems.addAll(collection);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Appends all of the elements in the specified collection to the end of the {@code List} , in the order that they are specified.
     *
     * @param objects the array of objects.
     *
     * @return {@code true} if the collection changed during insertion.
     */
    public final boolean addAll(@NonNull final T... objects) {
        boolean result = Collections.addAll(mItems, objects);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public boolean addAll(final int location, @NonNull final Collection<? extends T> collection) {
        boolean result = mItems.addAll(location, collection);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Inserts the objects in the specified collection at the specified location in this List. The objects are added in the order that they specified.
     *
     * @param location the index at which to insert.
     * @param objects  the array of objects.
     */
    public void addAll(final int location, @NonNull final T... objects) {
        for (int i = location; i < objects.length + location; i++) {
            mItems.add(i, objects[i]);
        }
        notifyDataSetChanged();
    }

    @Override
    public void clear() {
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public boolean contains(final Object object) {
        return mItems.contains(object);
    }

    @Override
    public boolean containsAll(@NonNull final Collection<?> collection) {
        return mItems.containsAll(collection);
    }

    @Override
    @NonNull
    public T get(final int location) {
        return mItems.get(location);
    }

    @Override
    @NonNull
    public T set(final int location, @NonNull final T object) {
        T result = mItems.set(location, object);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public int size() {
        return mItems.size();
    }

    @Override
    @NonNull
    public List<T> subList(final int start, final int end) {
        return mItems.subList(start, end);
    }

    @Override
    @NonNull
    public Object[] toArray() {
        return mItems.toArray();
    }

    @Override
    @NonNull
    public <T1> T1[] toArray(@NonNull final T1[] array) {
        //noinspection SuspiciousToArrayCall
        return mItems.toArray(array);
    }

    @Override
    public boolean remove(@NonNull final Object object) {
        boolean result = mItems.remove(object);
        notifyDataSetChanged();
        return result;
    }

    @Override
    @NonNull
    public T remove(final int location) {
        T result = mItems.remove(location);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Removes all elements at the specified locations in the {@code List}.
     *
     * @param locations the collection of indexes to remove.
     *
     * @return a collection containing the removed objects.
     */
    public Collection<T> removePositions(@NonNull final Collection<Integer> locations) {
        Collection<T> removedItems = new ArrayList<>();

        List<Integer> locationsList = new ArrayList<>(locations);
        Collections.sort(locationsList);
        Collections.reverse(locationsList);
        for (int location : locationsList) {
            removedItems.add(mItems.remove(location));
        }
        notifyDataSetChanged();
        return removedItems;
    }

    @Override
    public boolean removeAll(@NonNull final Collection<?> collection) {
        boolean result = mItems.removeAll(collection);
        notifyDataSetChanged();
        return result;
    }

    @Override
    public boolean retainAll(@NonNull final Collection<?> collection) {
        boolean result = mItems.retainAll(collection);
        notifyDataSetChanged();
        return result;
    }

    /**
     * Sorts the content of this adapter using the specified comparator.
     *
     * @param comparator The comparator used to sort the objects contained in this adapter.
     */
    public void sort(@Nullable final Comparator<? super T> comparator) {
        Collections.sort(mItems, comparator);
        notifyDataSetChanged();
    }

    @Override
    public int indexOf(@Nullable final Object object) {
        return mItems.indexOf(object);
    }

    @Override
    @NonNull
    public Iterator<T> iterator() {
        return mItems.iterator();
    }

    @Override
    public int lastIndexOf(@Nullable final Object object) {
        return mItems.lastIndexOf(object);
    }

    @Override
    @NonNull
    public ListIterator<T> listIterator() {
        return mItems.listIterator();
    }

    @Override
    @NonNull
    public ListIterator<T> listIterator(final int location) {
        return mItems.listIterator(location);
    }

    @Override
    public void swapItems(final int positionOne, final int positionTwo) {
        T firstItem = set(positionOne, getItem(positionTwo));
        set(positionTwo, firstItem);
    }

    private BaseAdapter mDataSetChangedSlavedAdapter;

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
