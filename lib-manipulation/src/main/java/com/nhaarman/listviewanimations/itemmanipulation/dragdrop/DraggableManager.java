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

package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * An interface to be used for determining whether the user should be able to drag a {@code View}.
 */
public interface DraggableManager {

    /**
     * Returns whether the {@code View} at given {@code position} can be dragged.
     *
     * @param view the item {@code View}, as returned by {@link android.widget.ListAdapter#getView(int, android.view.View, android.view.ViewGroup)} for given {@code position}.
     * @param position the position of the item
     * @param x the x coordinate of the touch within given {@code View}.
     * @param y the y coordinate of the touch within given {@code View}.
     *
     * @return {@code true} if the {@code View} should be dragged, {@code false} otherwise.
     */
    boolean isDraggable(@NonNull final View view, final int position, final float x, final float y);

}
