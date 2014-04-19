package com.nhaarman.listviewanimations.itemmanipulation;

import android.view.View;

/**
 * A callback interface used to inform its client about a successful dismissal of one or more list item positions.
 */
public interface UndoCallback extends OnDismissCallback {

    /**
     * Returns the primary {@link View} contained in given {@code View}.
     * @param view the parent {@code View}, which contains both the primary and the undo {@link View}s.
     */
    View getPrimaryView(View view);
    /**
     * Returns the undo {@link View} contained in given {@code View}.
     * @param view the parent {@code View}, which contains both the primary and the undo {@link View}s.
     */
    View getUndoView(View view);

    /**
     * Called when the undo {@link View} is shown for given position.
     * @param view the parent {@code View}, which contains both the primary and the undo {@link View}s.
     * @param position the position for which the undo {@code View} is shown.
     */
    void onUndoShown(View view, int position);

    /**
     * Called when the user has definitively dismissed an item.<br>
     * Do <i><b>NOT</b></i> remove the item from the adapter here!
     * @param view the parent {@code View}, which contains both the primary and the undo {@link View}s.
     * @param position the position of the item that is dismissed.
     */
    void onDismiss(View view, int position);
}
