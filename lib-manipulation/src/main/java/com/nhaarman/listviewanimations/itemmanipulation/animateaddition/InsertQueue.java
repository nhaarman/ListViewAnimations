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
package com.nhaarman.listviewanimations.itemmanipulation.animateaddition;

import android.support.annotation.NonNull;
import android.util.Pair;

import com.nhaarman.listviewanimations.util.Insertable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to insert items only when there are no active items.
 * A pending index-item pair can have two states: active and pending. When inserting new items, the {@link com.nhaarman.listviewanimations.itemmanipulation
 * .AnimateAdditionAdapter.Insertable#add
 * (int, Object)} method will be called directly if there are no active index-item pairs.
 * Otherwise, pairs will be queued until the active list is empty.
 */
public class InsertQueue<T> {

    @NonNull
    private final Insertable<T> mInsertable;

    @NonNull
    private final Collection<AtomicInteger> mActiveIndexes = new HashSet<>();

    @NonNull
    private final List<Pair<Integer, T>> mPendingItemsToInsert = new ArrayList<>();

    public InsertQueue(@NonNull final Insertable<T> insertable) {
        mInsertable = insertable;
    }

    /**
     * Insert an item into the queue at given index. Will directly call {@link Insertable#add(int,
     * Object)} if there are no active index-item pairs. Otherwise, the pair will be queued.
     *
     * @param index the index at which the item should be inserted.
     * @param item  the item to insert.
     */
    public void insert(final int index, @NonNull final T item) {
        if (mActiveIndexes.isEmpty() && mPendingItemsToInsert.isEmpty()) {
            mActiveIndexes.add(new AtomicInteger(index));
            //noinspection unchecked
            mInsertable.add(index, item);
        } else {
            mPendingItemsToInsert.add(new Pair<>(index, item));
        }
    }

    public void insert(@NonNull final Pair<Integer, T>... indexItemPair) {
        insert(Arrays.asList(indexItemPair));
    }

    public void insert(@NonNull final Collection<Pair<Integer, T>> indexItemPairs) {
        if (mActiveIndexes.isEmpty() && mPendingItemsToInsert.isEmpty()) {
            for (Pair<Integer, T> pair : indexItemPairs) {
                for (AtomicInteger existing : mActiveIndexes) {
                    if (existing.intValue() >= pair.first) {
                        existing.incrementAndGet();
                    }
                }
                mActiveIndexes.add(new AtomicInteger(pair.first));

                mInsertable.add(pair.first, pair.second);
            }
        } else {
            mPendingItemsToInsert.addAll(indexItemPairs);
        }
    }

    /**
     * Clears the active states and inserts any pending pairs if applicable.
     */
    public void clearActive() {
        mActiveIndexes.clear();
        insertPending();
    }

    /**
     * Clear the active state for given index. Will insert any pending pairs if this call leads to a state where there are no active pairs.
     *
     * @param index the index to remove.
     */
    public void removeActiveIndex(final int index) {
        boolean found = false;
        for (Iterator<AtomicInteger> iterator = mActiveIndexes.iterator(); iterator.hasNext() && !found; ) {
            if (iterator.next().get() == index) {
                iterator.remove();
                found = true;
            }
        }
        if (mActiveIndexes.isEmpty()) {
            insertPending();
        }
    }

    /**
     * Inserts pending items into the Insertable, and adds them to the active positions (correctly managing position shifting). Clears the pending items.
     */
    private void insertPending() {
        for (Pair<Integer, T> pi : mPendingItemsToInsert) {
            for (AtomicInteger existing : mActiveIndexes) {
                if (existing.intValue() >= pi.first) {
                    existing.incrementAndGet();
                }
            }
            mActiveIndexes.add(new AtomicInteger(pi.first));
            mInsertable.add(pi.first, pi.second);
        }
        mPendingItemsToInsert.clear();
    }

    /**
     * Returns a collection of currently active indexes.
     */
    @NonNull
    public Collection<Integer> getActiveIndexes() {
        Collection<Integer> result = new HashSet<>();
        for (AtomicInteger i : mActiveIndexes) {
            result.add(i.get());
        }
        return result;
    }

    /**
     * Returns a {@code List} of {@code Pair}s with the index and items that are pending to be inserted, in the order they were requested.
     */
    @NonNull
    public List<Pair<Integer, T>> getPendingItemsToInsert() {
        return mPendingItemsToInsert;
    }
}
