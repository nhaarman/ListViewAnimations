package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;

import java.util.HashMap;
import java.util.Map;


public class TimedUndoAdapter extends SimpleSwipeUndoAdapter {

    /**
     * The default time in milliseconds before an item in the undo state should automatically dismiss.
     */
    public static final long DEFAULT_TIMEOUT_MS = 3000;

    /**
     * The time in milliseconds before an item in the undo state should automatically dismiss.
     * Defaults to {@value #DEFAULT_TIMEOUT_MS}.
     */
    private long mTimeoutMs = DEFAULT_TIMEOUT_MS;

    /**
     * A {@link Handler} to post {@link TimeoutRunnable}s to.
     */
    private final Handler mHandler = new Handler();

    /**
     * The {@link TimeoutRunnable}s which are posted. Keys are positions.
     */
    private final Map<Integer, TimeoutRunnable> mRunnables = new HashMap<Integer, TimeoutRunnable>();

    /**
     * Creates a new {@code TimedUndoAdapter}, decorating given {@link android.widget.BaseAdapter}.
     * @param undoAdapter the {@link android.widget.BaseAdapter} that is decorated. Must implement {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter}.
     * @param context the {@link android.content.Context}.
     * @param dismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} that is notified of dismissed items.
     */
    public <T extends BaseAdapter & UndoAdapter> TimedUndoAdapter(final T undoAdapter, final Context context, final OnDismissCallback dismissCallback) {
        super(undoAdapter, context, dismissCallback);
    }

    /**
     * Sets the time in milliseconds after which an item in the undo state should automatically dismiss.
     * Defaults to {@value #DEFAULT_TIMEOUT_MS}.
     */
    public void setTimeoutMs(final long timeoutMs) {
        mTimeoutMs = timeoutMs;
    }

    @Override
    public void onUndoShown(final View view, final int position) {
        super.onUndoShown(view, position);
        TimeoutRunnable timeoutRunnable = new TimeoutRunnable(position);
        mRunnables.put(position, timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, mTimeoutMs);
    }

    @Override
    public void onDismiss(final View view, final int position) {
        super.onDismiss(view, position);
        cancelCallback(position);
    }

    @Override
    protected void undo(final View view, final int position) {
        super.undo(view, position);
        cancelCallback(position);
    }

    @Override
    public void dismiss(final int position) {
        super.dismiss(position);
        cancelCallback(position);
    }

    private void cancelCallback(final int position) {
        Runnable timeoutRunnable = mRunnables.get(position);
        if (timeoutRunnable != null) {
            mHandler.removeCallbacks(timeoutRunnable);
            mRunnables.remove(position);
        }
    }

    @Override
    public void onDismiss(final AbsListView absListView, final int[] reverseSortedPositions) {
        super.onDismiss(absListView, reverseSortedPositions);

        /* Adjust the pending timeout positions accordingly wrt the given dismissed positions */
        Map<Integer, TimeoutRunnable> newRunnables = new HashMap<Integer, TimeoutRunnable>();
        for (int position : reverseSortedPositions) {
            for (int key : mRunnables.keySet()) {
                TimeoutRunnable runnable = mRunnables.get(key);
                if (key > position) {
                    key--;
                    runnable.setPosition(key);
                    newRunnables.put(key, runnable);
                } else if (key != position) {
                    newRunnables.put(key, runnable);
                }
            }

            mRunnables.clear();
            mRunnables.putAll(newRunnables);
            newRunnables.clear();
        }
    }

    /**
     * A {@link Runnable} class which dismisses a position when executed.
     */
    private class TimeoutRunnable implements Runnable {

        private int mPosition;

        TimeoutRunnable(final int position) {
            mPosition = position;
        }

        @Override
        public void run() {
            dismiss(mPosition);
        }

        public int getPosition() {
            return mPosition;
        }

        public void setPosition(final int position) {
            mPosition = position;
        }
    }
}
