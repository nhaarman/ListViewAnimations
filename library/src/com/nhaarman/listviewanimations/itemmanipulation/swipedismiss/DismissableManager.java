package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

/**
 * An interface to specify whether certain items can or cannot be dismissed.
 */
public interface DismissableManager {

    /**
     * Returns whether the item for given id and position can be dismissed.
     * @param id the id of the item.
     * @param position the position of the item.
     * @return true if the item can be dismissed, false otherwise.
     */
    public boolean isDismissable(long id, int position);
}