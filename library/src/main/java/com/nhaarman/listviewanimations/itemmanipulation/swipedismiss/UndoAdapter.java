package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.view.View;
import android.view.ViewGroup;

/**
 * An interface used for {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SimpleSwipeUndoAdapter}.
 * Used to provide the undo {@link View}s.
 */
public interface UndoAdapter {

    /**
     * Returns the entire undo {@link View} that should be shown.
     * @param position the position of the item for which the undo {@code View} should be shown.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     * @param parent The parent that this view will eventually be attached to
     */
    View getUndoView(final int position, final View convertView, final ViewGroup parent);

    /**
     * Returns the {@link View} which serves as a button to undo the swipe movement. When a user clicks on this {@code View}, the swipe is undone.
     * @param view the parent {@code View} as returned in {@link #getUndoView(int, android.view.View, android.view.ViewGroup)}.
     */
    View getUndoClickView(final View view);

}
