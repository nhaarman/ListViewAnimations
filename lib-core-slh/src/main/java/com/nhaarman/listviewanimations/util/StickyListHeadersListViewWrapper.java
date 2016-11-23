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

package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ListAdapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import se.emilsjolander.stickylistheaders.WrapperView;

public class StickyListHeadersListViewWrapper implements ListViewWrapper {

    @NonNull
    private final StickyListHeadersListView mListView;

    public StickyListHeadersListViewWrapper(@NonNull final StickyListHeadersListView listView) {
        mListView = listView;
    }

    @NonNull
    @Override
    public StickyListHeadersListView getListView() {
        return mListView;
    }

    @Nullable
    @Override
    public View getChildAt(final int index) {
        return unwrapItemView(mListView.getListChildAt(index));
    }

    @Override
    public int getFirstVisiblePosition() {
        return mListView.getFirstVisiblePosition();
    }

    @Override
    public int getLastVisiblePosition() {
        return mListView.getLastVisiblePosition();
    }

    @Override
    public int getCount() {
        return mListView.getCount();
    }

    @Override
    public int getChildCount() {
        return mListView.getListChildCount();
    }

    @Override
    public int getHeaderViewsCount() {
        return mListView.getHeaderViewsCount();
    }

    @Override
    public int getPositionForView(@NonNull final View view) {
        return mListView.getPositionForView(view);
    }

    @NonNull
    @Override
    public ListAdapter getAdapter() {
        return mListView.getAdapter();
    }

    @Override
    public void smoothScrollBy(final int distance, final int duration) {
        mListView.smoothScrollBy(distance, duration);
    }

    /**
     * Retrieves the original View that is now possibly wrapped in a WrapperView.
     *
     * @param view The View to retrieve the original View from.
     *
     * @return The original View.
     */
    private View unwrapItemView(final View view) {
        if (view instanceof WrapperView) {
            return ((WrapperView) view).getItem();
        }
        return view;
    }
}
