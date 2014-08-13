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

/**
 * An interface which provides a callback that is called when an item has moved using the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite.DynamicListView}.
 */
public interface OnItemMovedListener {

    /**
     * Called when an item that was dragged has been dropped.
     *
     * @param originalPosition the original position of the item that was dragged.
     * @param newPosition the new position of the item that was dragged.
     */
    void onItemMoved(int originalPosition, int newPosition);
}