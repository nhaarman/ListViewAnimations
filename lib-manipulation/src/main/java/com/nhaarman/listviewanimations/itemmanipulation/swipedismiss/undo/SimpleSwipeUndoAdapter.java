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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of {@link SwipeUndoAdapter} which puts the primary and undo {@link android.view.View} in a {@link android.widget.FrameLayout},
 * and handles the undo click event.
 */
public class SimpleSwipeUndoAdapter extends SwipeUndoAdapter implements UndoCallback {

    @NonNull
    private final Context mContext;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback} that is notified of dismissed items.
     */
    @NonNull
    private final OnDismissCallback mOnDismissCallback;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter} that provides the undo {@link android.view.View}s.
     */
    @NonNull
    private final UndoAdapter mUndoAdapter;

    /**
     * The positions of the items currently in the undo state.
     */
    private final Collection<Integer> mUndoPositions = new ArrayList<>();

    /**
     * Create a new {@code SimpleSwipeUndoAdapterGen}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param undoAdapter     the {@link android.widget.BaseAdapter} that is decorated. Must implement
     *                        {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter}.
     * @param context         the {@link android.content.Context}.
     * @param dismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback} that is notified of dismissed items.
     */
    public SimpleSwipeUndoAdapter(@NonNull final BaseAdapter adapter, @NonNull final Context context,
                                  @NonNull final OnDismissCallback dismissCallback) {
        // We fix this right away
        // noinspection ConstantConditions
        super(adapter, null);
        setUndoCallback(this);

        BaseAdapter undoAdapter = adapter;
        while (undoAdapter instanceof BaseAdapterDecorator) {
            undoAdapter = ((BaseAdapterDecorator) undoAdapter).getDecoratedBaseAdapter();
        }

        if (!(undoAdapter instanceof UndoAdapter)) {
            throw new IllegalStateException("BaseAdapter must implement UndoAdapter!");
        }

        mUndoAdapter = (UndoAdapter) undoAdapter;
        mContext = context;
        mOnDismissCallback = dismissCallback;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        SwipeUndoView view = (SwipeUndoView) convertView;
        if (view == null) {
            view = new SwipeUndoView(mContext);
        }
        View primaryView = super.getView(position, view.getPrimaryView(), view);
        view.setPrimaryView(primaryView);

        View undoView = mUndoAdapter.getUndoView(position, view.getUndoView(), view);
        view.setUndoView(undoView);

        mUndoAdapter.getUndoClickView(undoView).setOnClickListener(new UndoClickListener(view, position));

        boolean isInUndoState = mUndoPositions.contains(position);
        primaryView.setVisibility(isInUndoState ? View.GONE : View.VISIBLE);
        undoView.setVisibility(isInUndoState ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    @NonNull
    public View getPrimaryView(@NonNull final View view) {
        View primaryView = ((SwipeUndoView) view).getPrimaryView();
        if (primaryView == null) {
            throw new IllegalStateException("primaryView == null");
        }
        return primaryView;
    }

    @Override
    @NonNull
    public View getUndoView(@NonNull final View view) {
        View undoView = ((SwipeUndoView) view).getUndoView();
        if (undoView == null) {
            throw new IllegalStateException("undoView == null");
        }
        return undoView;
    }

    @Override
    public void onUndoShown(@NonNull final View view, final int position) {
        mUndoPositions.add(position);
    }

    @Override
    public void onUndo(@NonNull final View view, final int position) {
        mUndoPositions.remove(position);
    }

    @Override
    public void onDismiss(@NonNull final View view, final int position) {
        mUndoPositions.remove(position);
    }

    @Override
    public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
        mOnDismissCallback.onDismiss(listView, reverseSortedPositions);

        Collection<Integer> newUndoPositions = Util.processDeletions(mUndoPositions, reverseSortedPositions);
        mUndoPositions.clear();
        mUndoPositions.addAll(newUndoPositions);
    }


    private class UndoClickListener implements View.OnClickListener {

        @NonNull
        private final SwipeUndoView mView;

        private final int mPosition;

        UndoClickListener(@NonNull final SwipeUndoView view, final int position) {
            mView = view;
            mPosition = position;
        }

        @Override
        public void onClick(@NonNull final View v) {
            undo(mView);
        }
    }
}
