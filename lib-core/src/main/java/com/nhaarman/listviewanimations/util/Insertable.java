package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;

/**
 * An interface for inserting items at a certain index.
 */
public interface Insertable<T> {

    /**
     * Will be called to insert given {@code item} at given {@code index} in the list.
     *
     * @param index the index the new item should be inserted at
     * @param item  the item to insert
     */
    void add(int index, @NonNull T item);
}