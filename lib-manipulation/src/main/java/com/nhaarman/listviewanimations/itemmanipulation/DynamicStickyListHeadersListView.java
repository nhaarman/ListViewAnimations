package com.nhaarman.listviewanimations.itemmanipulation;

import com.nhaarman.listviewanimations.util.Insertable;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
import se.emilsjolander.stickylistheaders.StickyListHeadersListViewAbstract;


public class DynamicStickyListHeadersListView extends
        StickyListHeadersListViewAbstract {
    public DynamicStickyListHeadersListView(Context context) {
        super(context);
    }
    
    public DynamicStickyListHeadersListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicStickyListHeadersListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected DynamicWrapperViewList createListView(Context context) {
        return new DynamicWrapperViewList(context);
    }
    
    DynamicListView getDynamicListView() {
        return (DynamicListView) mList;
    }
    
    public void insert(final int index, final Object item) {
        getDynamicListView().insert(index, item);
    }

    /**
     * Inserts items, starting at given index. Will show an entrance animation for the new items if the newly added items are visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param index the starting index the new items should be inserted at.
     * @param items the items to insert.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public void insert(final int index, final Object... items) {
        getDynamicListView().insert(index, items);
    }

    /**
     * Inserts items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void insert(@NonNull final Pair<Integer, T>... indexItemPairs) {
        getDynamicListView().insert(indexItemPairs);
    }

    /**
     * Insert items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void insert(@NonNull final Iterable<Pair<Integer, T>> indexItemPairs) {
        getDynamicListView().insert(indexItemPairs);
    }

    /**
     * Remove an item at given index. Will show an leave animation for the item if item is visible.
     * Will also call {@link com.nhaarman.listviewanimations.util.Removable#remove(int)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param position the index of the item to remove
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void remove(@NonNull final int position) {
        getDynamicListView().remove(position);
    }

    /**
     * Remove items starting at given index. Will show an leave animation for the item if item is visible.
     * Will also call {@link com.nhaarman.listviewanimations.util.Removable#remove(int)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param position the index of the item to remove
     * @param count number of items to remove
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void remove(@NonNull final int position, final int count) {
        getDynamicListView().remove(position, count);
    }
}
