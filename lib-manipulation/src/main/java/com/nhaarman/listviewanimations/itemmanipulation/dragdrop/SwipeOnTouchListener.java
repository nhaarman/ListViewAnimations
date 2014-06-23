package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

import android.view.View;

/**
 * An OnTouchListener that should be used when list-view items can be swiped horizontally.
 */
public interface SwipeOnTouchListener extends View.OnTouchListener {
    /**
     * @return true if the user is currently swiping a list item horizontally.
     */
    boolean isSwiping();
}