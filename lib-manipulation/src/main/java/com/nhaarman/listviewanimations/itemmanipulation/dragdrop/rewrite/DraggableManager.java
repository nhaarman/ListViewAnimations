package com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * An interface to be used for determining whether the user should be able to drag a {@code View}.
 */
public interface DraggableManager {

    /**
     * Returns whether the {@code View} at given {@code position} can be dragged.
     *
     * @param view the item {@code View}, as returned by {@link android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)} for given {@code position}.
     * @param position the position of the item
     * @param x the x coordinate of the touch within given {@code View}.
     * @param y the y coordinate of the touch within given {@code View}.
     *
     * @return {@code true} if the {@code View} should be dragged.
     */
    boolean isDraggable(@NonNull final View view, final int position, final float x, final float y);

}
