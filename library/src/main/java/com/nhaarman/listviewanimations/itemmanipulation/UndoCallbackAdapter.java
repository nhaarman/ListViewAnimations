package com.nhaarman.listviewanimations.itemmanipulation;

import android.view.View;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoCallback;

/**
 * A convenience implementation of {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoCallback} where optional methods have been implemented.
 */
public abstract class UndoCallbackAdapter implements UndoCallback {

    @Override
    public void onUndoShown(final View view, final int position) {
    }

    @Override
    public void onDismiss(final View view, final int position) {
    }
}
