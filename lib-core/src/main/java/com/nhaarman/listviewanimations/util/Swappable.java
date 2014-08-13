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

/**
 * Interface, usually implemented by a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator},
 * that indicates that it can swap the visual position of two list items.
 */
public interface Swappable {

    /**
     * Swaps the item on the first adapter position with the item on the second adapter position.
     * Be sure to call {@link android.widget.BaseAdapter#notifyDataSetChanged()} if appropriate when implementing this method.
     *
     * @param positionOne First adapter position.
     * @param positionTwo Second adapter position.
     */
    void swapItems(int positionOne, int positionTwo);
}