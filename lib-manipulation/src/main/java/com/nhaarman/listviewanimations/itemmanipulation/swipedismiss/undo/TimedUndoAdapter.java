/*
 * Copyright 2014 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.HashMap;
import java.util.Map;

/**
 * A {@link SimpleSwipeUndoAdapter} which automatically dismisses items after a timeout.
 */
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
     * A {@link android.os.Handler} to post {@link TimeoutRunnable}s to.
     */
    @NonNull
    private final Handler mHandler = new Handler();

    /**
     * The {@link TimeoutRunnable}s which are posted. Keys are positions.
     */
    //noinspection UseSparseArrays
    private final Map<Integer, TimeoutRunnable> mRunnables = new HashMap<>();

    /**
     * Creates a new {@code TimedUndoAdapterGen}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param undoAdapter     the {@link android.widget.BaseAdapter} that is decorated. Must implement
     *                        {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter}.
     * @param context         the {@link android.content.Context}.
     * @param dismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback} that is notified of dismissed items.
     */
    public <V extends BaseAdapter & UndoAdapter> TimedUndoAdapter(@NonNull final V undoAdapter, @NonNull final Context context, @NonNull final OnDismissCallback
            dismissCallback) {
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
    public void onUndoShown(@NonNull final View view, final int position) {
        super.onUndoShown(view, position);
        TimeoutRunnable timeoutRunnable = new TimeoutRunnable(position);
        mRunnables.put(position, timeoutRunnable);
        mHandler.postDelayed(timeoutRunnable, mTimeoutMs);
    }

    @Override
    public void onUndo(@NonNull final View view, final int position) {
        super.onUndo(view, position);
        cancelCallback(position);
    }

    @Override
    public void onDismiss(@NonNull final View view, final int position) {
        super.onDismiss(view, position);
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
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
        super.onDismiss(listView, reverseSortedPositions);

        /* Adjust the pending timeout positions accordingly wrt the given dismissed positions */
        //noinspection UseSparseArrays
        Map<Integer, TimeoutRunnable> newRunnables = new HashMap<>();
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
