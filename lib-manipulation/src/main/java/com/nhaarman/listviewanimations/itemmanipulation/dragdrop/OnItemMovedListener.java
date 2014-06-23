package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

/**
 * Implement this interface to be notified of ordering changes.
 */
public interface OnItemMovedListener {

    /**
     * Called after an item is dropped and moved.
     *
     * @param newPosition the new position of the item.
     */
    void onItemMoved(int newPosition);
}