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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;

import java.util.ArrayList;
import java.util.Collection;

/**
 * An implementation of {@link SwipeUndoAdapter} which puts the primary and undo {@link View} in a {@link android.widget.FrameLayout},
 * and handles the undo click event.
 */
public class SimpleSwipeUndoAdapter extends SwipeUndoAdapter implements UndoCallback {

    private final Context mContext;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} that is notified of dismissed items.
     */
    private final OnDismissCallback mOnDismissCallback;

    /**
     * The {@link UndoAdapter} that provides the undo {@link View}s.
     */
    private final UndoAdapter mUndoAdapter;

    /**
     * The positions of the items currently in the undo state.
     */
    private final Collection<Integer> mUndoPositions = new ArrayList<Integer>();

    /**
     * Create a new {@code SimpleSwipeUndoAdapter}, decorating given {@link android.widget.BaseAdapter}.
     * @param undoAdapter the {@link android.widget.BaseAdapter} that is decorated. Must implement {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter}.
     * @param context the {@link Context}.
     * @param dismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} that is notified of dismissed items.
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

        mUndoAdapter.getUndoClickView(undoView).setOnClickListener(new UndoClickListener(view, position));

        boolean isInUndoState = mUndoPositions.contains(position);
        primaryView.setVisibility(isInUndoState ? View.GONE : View.VISIBLE);
        undoView.setVisibility(isInUndoState ? View.VISIBLE : View.GONE);

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
        mUndoPositions.add(position);
    }

    @Override
    public void onDismiss(final View view, final int position) {
        mUndoPositions.remove(position);
    }

    @Override
    public void onDismiss(final AbsListView absListView, final int[] reverseSortedPositions) {
        mOnDismissCallback.onDismiss(absListView, reverseSortedPositions);

        Collection<Integer> newUndoPositions = Util.processDeletions(mUndoPositions, reverseSortedPositions);
        mUndoPositions.clear();
        mUndoPositions.addAll(newUndoPositions);
    }

    @Override
    public void undo(final View view) {
        int position = AdapterViewUtil.getPositionForView(getAbsListView(), view);
        undo(view, position);
    }

    /**
     * Performs the undo animation and restores the original state for given {@link View}.
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     * @param position the position of the item in the {@link android.widget.BaseAdapter} corresponding to the {@code View}.
     */
    protected void undo(final View view, final int position) {
        super.undo(view);
        mUndoPositions.remove(position);
    }

    private class UndoClickListener implements View.OnClickListener {
        private final SwipeUndoView mView;
        private final int mPosition;

        UndoClickListener(final SwipeUndoView view, final int position) {
            mView = view;
            mPosition = position;
        }

        @Override
        public void onClick(final View v) {
            undo(mView, mPosition);
        }
    }
}
