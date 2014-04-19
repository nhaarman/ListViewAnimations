package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A convenience class which holds a primary and a undo {@link View}.
 */
class SwipeUndoView extends FrameLayout {

    /**
     * The primary {@link View}.
     */
    private View mPrimaryView;

    /**
     * The undo {@link View}.
     */
    private View mUndoView;

    /**
     * Creates a new {@code SwipeUndoView}.
     */
    SwipeUndoView(final Context context) {
        super(context);
    }

    /**
     * Sets the primary {@link View}. Removes any existing primary {@code View} if present.
     */
    void setPrimaryView(final View primaryView) {
        if (mPrimaryView != null) {
            removeView(mPrimaryView);
        }
        mPrimaryView = primaryView;
        addView(mPrimaryView);
    }

    /**
     * Sets the undo {@link View}. Removes any existing primary {@code View} if present, and sets the visibility of the {@code undoView} to {@link #GONE}.
     */
    void setUndoView(final View undoView) {
        if (mUndoView != null) {
            removeView(mUndoView);
        }
        mUndoView = undoView;
        mUndoView.setVisibility(GONE);
        addView(mUndoView);
    }

    /**
     * Returns the undo {@link View}.
     */
    View getUndoView() {
        return mUndoView;
    }

    /**
     * Returns the primary {@link View}.
     */
    View getPrimaryView() {
        return mPrimaryView;
    }
}
