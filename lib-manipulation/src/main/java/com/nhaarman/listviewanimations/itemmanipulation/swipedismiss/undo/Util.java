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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

class Util {

    private Util() {
    }

    @NonNull
    static Collection<Integer> processDeletions(@NonNull final Collection<Integer> positions, @NonNull final int[] dismissedPositions) {
        List<Integer> dismissedList = new ArrayList<>();
        for (int position : dismissedPositions) {
            dismissedList.add(position);
        }
        return processDeletions(positions, dismissedList);
    }

    /**
     * Removes positions in {@code dismissedPositions} from {@code positions}, and shifts the remaining positions accordingly.
     *
     * @param positions          the list of positions to remove from.
     * @param dismissedPositions the list of positions to remove.
     *
     * @return a new {@link java.util.Collection} instance, containing the resulting positions.
     */
    @NonNull
    static Collection<Integer> processDeletions(@NonNull final Collection<Integer> positions, @NonNull final List<Integer> dismissedPositions) {
        Collection<Integer> result = new ArrayList<>(positions);
        Collections.sort(dismissedPositions, Collections.reverseOrder());
        Collection<Integer> newUndoPositions = new ArrayList<>();
        for (int position : dismissedPositions) {
            for (Iterator<Integer> iterator = result.iterator(); iterator.hasNext(); ) {
                int undoPosition = iterator.next();
                if (undoPosition > position) {
                    iterator.remove();
                    newUndoPositions.add(undoPosition - 1);
                } else if (undoPosition == position) {
                    iterator.remove();
                } else {
                    newUndoPositions.add(undoPosition);
                }
            }
            result.clear();
            result.addAll(newUndoPositions);
            newUndoPositions.clear();
        }

        return result;
    }
}
