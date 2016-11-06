package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

/**
 * An interface which provides a callback that is called when an item's moving has been canceled
 * using the {@link com.nhaarman.listviewanimations.itemmanipulation.DynamicListView}.
 * It usually happens when user drags item but then drop it back to its original position
 */
public interface OnItemMovingCanceledListener {

    /**
     * Called when an item that was dragged has been dropped without moving to new position.
     *
     * @param originalPosition the original position of the item that was dragged.
     *
     */
    void onItemMovingCanceled(int originalPosition);
}
