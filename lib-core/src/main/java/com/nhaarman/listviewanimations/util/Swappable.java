package com.nhaarman.listviewanimations.util;

/**
 * Interface, usually implemented by a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator},
 * that indicates that it can swap the visual position of two list items.
 */
public interface Swappable {

    /**
     * Swaps the item on the first adapter position with the item on the second adapter position.
     * Be sure to call {@link android.widget.BaseAdapter#notifyDataSetChanged()} if appropriate when implementing this method.
     *
     * @param positionOne First adapter position.
     * @param positionTwo Second adapter position.
     */
    void swapItems(int positionOne, int positionTwo);
}