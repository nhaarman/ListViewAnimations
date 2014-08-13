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

import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ListView;

public class AdapterViewUtil {

    private AdapterViewUtil() {
    }

    /**
     * Returns the position within the adapter's dataset for the view, where view is an adapter item or a descendant of an adapter item.
     * Unlike {@link AdapterView#getPositionForView(android.view.View)}, returned position will reflect the position of the item given view is representing,
     * by subtracting the header views count.
     *
     * @param listViewWrapper the IListViewWrapper wrapping the ListView containing the view.
     * @param view            an adapter item or a descendant of an adapter item. This must be visible in given AdapterView at the time of the call.
     *
     * @return the position of the item in the AdapterView represented by given view, or {@link AdapterView#INVALID_POSITION} if the view does not
     * correspond to a list item (or it is not visible).
     */
    public static int getPositionForView(@NonNull final ListViewWrapper listViewWrapper, @NonNull final View view) {
        return listViewWrapper.getPositionForView(view) - listViewWrapper.getHeaderViewsCount();
    }

    /**
     * Returns the position within the adapter's dataset for the view, where view is an adapter item or a descendant of an adapter item.
     * Unlike {@link AdapterView#getPositionForView(android.view.View)}, returned position will reflect the position of the item given view is representing,
     * by subtracting the header views count.
     *
     * @param absListView the ListView containing the view.
     * @param view        an adapter item or a descendant of an adapter item. This must be visible in given AdapterView at the time of the call.
     *
     * @return the position of the item in the AdapterView represented by given view, or {@link AdapterView#INVALID_POSITION} if the view does not
     * correspond to a list item (or it is not visible).
     */
    public static int getPositionForView(@NonNull final AbsListView absListView, @NonNull final View view) {
        int position = absListView.getPositionForView(view);
        if (absListView instanceof ListView) {
            position -= ((ListView) absListView).getHeaderViewsCount();
        }
        return position;
    }

    /**
     * Returns the {@link View} that represents the item for given position.
     *
     * @param listViewWrapper the {@link ListViewWrapper} wrapping the ListView that should be examined
     * @param position        the position for which the {@code View} should be returned.
     *
     * @return the {@code View}, or {@code null} if the position is not currently visible.
     */
    @Nullable
    public static View getViewForPosition(@NonNull final ListViewWrapper listViewWrapper, final int position) {
        int childCount = listViewWrapper.getChildCount();
        View downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = listViewWrapper.getChildAt(i);
            if (child != null && getPositionForView(listViewWrapper, child) == position) {
                downView = child;
            }
        }
        return downView;
    }

    /**
     * Returns the {@link View} that represents the item for given position.
     *
     * @param absListView the ListView that should be examined
     * @param position    the position for which the {@code View} should be returned.
     *
     * @return the {@code View}, or {@code null} if the position is not currently visible.
     */
    @Nullable
    public static View getViewForPosition(@NonNull final AbsListView absListView, final int position) {
        int childCount = absListView.getChildCount();
        View downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = absListView.getChildAt(i);
            if (child != null && getPositionForView(absListView, child) == position) {
                downView = child;
            }
        }
        return downView;
    }
}
