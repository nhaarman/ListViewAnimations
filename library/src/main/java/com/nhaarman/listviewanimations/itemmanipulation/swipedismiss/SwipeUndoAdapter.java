package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.UndoCallback;

/**
 * Adds swipe-undo behaviour to the {@link android.widget.AbsListView}, using a {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeUndoTouchListener}.
 */
public abstract class SwipeUndoAdapter extends BaseAdapterDecorator {

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeUndoTouchListener} that is set to the {@link android.widget.AbsListView}.
     */
    private SwipeUndoTouchListener mSwipeUndoTouchListener;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.UndoCallback} that is used.
     */
    private UndoCallback mUndoCallback;

    /**
     * Create a new {@code SwipeUndoAdapter}, decorating given {@link android.widget.BaseAdapter}.
     * @param baseAdapter the {@link android.widget.BaseAdapter} to decorate.
     * @param undoCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.UndoCallback} that is used.
     */
    public SwipeUndoAdapter(final BaseAdapter baseAdapter, final UndoCallback undoCallback) {
        super(baseAdapter);
        mUndoCallback = undoCallback;
    }

    @Override
    public void setAbsListView(final AbsListView absListView) {
        super.setAbsListView(absListView);
        mSwipeUndoTouchListener = new SwipeUndoTouchListener(absListView, mUndoCallback);
        absListView.setOnTouchListener(mSwipeUndoTouchListener);
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        if (getAbsListView() == null) {
            throw new IllegalArgumentException("Call setAbsListView() on this SwipeUndoAdapter before setAdapter()!");
        }
        return super.getView(position, convertView, parent);
    }

    /**
     * Set the {@link com.nhaarman.listviewanimations.itemmanipulation.UndoCallback} to use
     */
    public void setUndoCallback(final UndoCallback undoCallback) {
        mUndoCallback = undoCallback;
    }

    /**
     * Performs the undo animation and restores the original state for given {@link View}.
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     */
    public void undo(final View view) {
        mSwipeUndoTouchListener.undo(view);
    }
}
