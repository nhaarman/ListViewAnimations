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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * An interface used for {@link SimpleSwipeUndoAdapter}.
 * Used to provide the undo {@link android.view.View}s.
 */
public interface UndoAdapter {

    /**
     * Returns the entire undo {@link android.view.View} that should be shown.
     *
     * @param position    the position of the item for which the undo {@code View} should be shown.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *                    is non-null and of an appropriate type before using. If it is not possible to convert
     *                    this view to display the correct data, this method can create a new view.
     * @param parent      The parent that this view will eventually be attached to
     */
    @NonNull
    View getUndoView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent);

    /**
     * Returns the {@link android.view.View} which serves as a button to undo the swipe movement. When a user clicks on this {@code View}, the swipe is undone.
     *
     * @param view the parent {@code View} as returned in {@link #getUndoView(int, android.view.View, android.view.ViewGroup)}.
     */
    @NonNull
    View getUndoClickView(@NonNull final View view);

}
