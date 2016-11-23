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

/**
 * An interface for removing items at a certain index.
 */
public interface Removable<T> {

    /**
     * Will be called to remove  item at given {@code index} in the list.
     *
     * @param index the index of the item which should be removed
     */
    T remove(int index);

    int getCount();
}