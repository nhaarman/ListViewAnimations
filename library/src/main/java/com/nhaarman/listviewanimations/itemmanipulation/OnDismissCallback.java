/*
 * Copyright 2013 Niek Haarman
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
package com.nhaarman.listviewanimations.itemmanipulation;

import android.widget.AbsListView;
import android.widget.ListView;

/**
 * The callback interface used by {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissListViewTouchListener} to
 * inform its client about a successful dismissal of one or more list item
 * positions.
 */
public interface OnDismissCallback {
    /**
     * Called when the user has indicated they she would like to dismiss one or
     * more list item positions.
     *
     * @param listView
     *            The originating {@link ListView}.
     * @param reverseSortedPositions
     *            An array of positions to dismiss, sorted in descending order
     *            for convenience.
     */
    void onDismiss(AbsListView listView, int[] reverseSortedPositions);
}