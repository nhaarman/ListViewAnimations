package com.nhaarman.listviewanimations.itemmanipulation;

import android.view.View;

/**
 * A convenience implementation of {@link com.nhaarman.listviewanimations.itemmanipulation.UndoCallback} where optional methods have been implemented.
 */
public abstract class UndoCallbackAdapter implements UndoCallback {

    @Override
    public void onUndoShown(final View view, final int position) {
    }

    @Override
    public void onDismiss(final View view, final int position) {
    }
}
