package com.haarman.listviewanimations.itemmanipulation;

/**
 * Created by sambarboza on 27/11/13.
 * sambarbosaa@gmail.com
 * Listener to get noticed when item is moved by drag and drop.
 */
public interface ItemMovedListener {
    /**
     * It's called after the item is finally dropped and moved.
     * It's not called while the item is being dragged.
     * @param item
     * @param newPosition
     */
    public void onItemMoved(Object item, int newPosition);
}
