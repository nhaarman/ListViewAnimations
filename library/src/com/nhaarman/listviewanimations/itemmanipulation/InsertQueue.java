package com.nhaarman.listviewanimations.itemmanipulation;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class to insert items only when there are no active items.
 * A pending index-item pair can have two states: active and pending. When inserting new items, the {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter.Insertable#add
 * (int, Object)} method will be called directly if there are no active index-item pairs.
 * Otherwise, pairs will be queued until the active list is empty.
 */
public class InsertQueue<T> {

    private final AnimateAdditionAdapter.Insertable<T> mInsertable;

    private final Set<AtomicInteger> mActiveIndexes = new HashSet<AtomicInteger>();
    private final List<Pair<Integer, T>> mPendingItemsToInsert = new ArrayList<Pair<Integer, T>>();

    public InsertQueue(final AnimateAdditionAdapter.Insertable<T> insertable) {
        mInsertable = insertable;
    }

    /**
     * Insert an item into the queue at given index. Will directly call {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter.Insertable#add(int,
     * Object)} if there are no active index-item pairs. Otherwise, the pair will be queued.
     * @param index the index at which the item should be inserted.
     * @param item the item to insert.
     */
    public void insert(final int index, final T item) {
        if (mActiveIndexes.isEmpty() && mPendingItemsToInsert.isEmpty()) {
            mActiveIndexes.add(new AtomicInteger(index));
            //noinspection unchecked
            mInsertable.add(index, item);
        } else {
            mPendingItemsToInsert.add(new Pair<Integer, T>(index, item));
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public void insert(final Pair<Integer, T>... indexItemPair) {
        insert(Arrays.asList(indexItemPair));
    }

    public void insert(final List<Pair<Integer, T>> indexItemPairs) {
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
    public Collection<Integer> getActiveIndexes() {
        HashSet<Integer> result = new HashSet<Integer>();
        for (AtomicInteger i : mActiveIndexes) {
            result.add(i.get());
        }
        return result;
    }

    /**
     * Returns a {@code List} of {@code Pair}s with the index and items that are pending to be inserted, in the order they were requested.
     */
    public List<Pair<Integer, T>> getPendingItemsToInsert() {
        return mPendingItemsToInsert;
    }
}
