package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.UndoCallback;

/**
 * An implementation of {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeUndoAdapter} which puts the primary and undo {@link View} in a {@link android.widget.FrameLayout},
 * and handles the undo click event.
 */
public class SimpleSwipeUndoAdapter extends SwipeUndoAdapter implements UndoCallback {

    private final Context mContext;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} that is notified of dismissed items.
     */
    private final OnDismissCallback mOnDismissCallback;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.UndoAdapter} that provides the undo {@link View}s.
     */
    private final UndoAdapter mUndoAdapter;

    /**
     * Create a new {@code SwipeUndoAdapter}, decorating given {@link android.widget.BaseAdapter}.
     * @param undoAdapter the {@code} BaseAdapter to decorate.
     */
    public <T extends BaseAdapter & UndoAdapter> SimpleSwipeUndoAdapter(final T undoAdapter, final Context context, final OnDismissCallback dismissCallback) {
        super(undoAdapter, null);
        setUndoCallback(this);
        mUndoAdapter = undoAdapter;
        mContext = context;
        mOnDismissCallback = dismissCallback;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        SwipeUndoView view = (SwipeUndoView) convertView;
        if (view == null) {
            view = new SwipeUndoView(mContext);
        }
        View primaryView = super.getView(position, view.getPrimaryView(), view);
        view.setPrimaryView(primaryView);

        View undoView = mUndoAdapter.getUndoView(position, view.getUndoView(), view);
        view.setUndoView(undoView);

        mUndoAdapter.getUndoClickView(undoView).setOnClickListener(new UndoClickListener(view));

        return view;
    }

    @Override
    public View getPrimaryView(final View view) {
        return ((SwipeUndoView) view).getPrimaryView();
    }

    @Override
    public View getUndoView(final View view) {
        return ((SwipeUndoView) view).getUndoView();
    }

    @Override
    public void onUndoShown(final View view, final int position) {
    }

    @Override
    public void onDismiss(final View view, final int position) {
    }

    @Override
    public void onDismiss(final AbsListView absListView, final int[] reverseSortedPositions) {
        mOnDismissCallback.onDismiss(absListView, reverseSortedPositions);
    }

    private class UndoClickListener implements View.OnClickListener {
        private final SwipeUndoView mView;

        UndoClickListener(final SwipeUndoView view) {
            mView = view;
        }

        @Override
        public void onClick(final View v) {
            undo(mView);
        }
    }
}
