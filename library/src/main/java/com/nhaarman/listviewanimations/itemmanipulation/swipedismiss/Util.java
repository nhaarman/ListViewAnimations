package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Util {

    private Util() {
    }

    public static Collection<Integer> processDeletions(final Collection<Integer> positions, final int[] dismissedPositions) {
        List<Integer> dismissedList = new ArrayList<Integer>();
        for (int position : dismissedPositions) {
            dismissedList.add(position);
        }
        return processDeletions(positions, dismissedList);
    }

    /**
     * Removes positions in {@code dismissedPositions} from {@code positions}, and shifts the remaining positions accordingly.
     * @param positions the list of positions to remove from.
     * @param dismissedPositions the list of positions to remove.
     * @return a new {@link Collection} instance, containing the resulting positions.
     */
    public static Collection<Integer> processDeletions(final Collection<Integer> positions, final List<Integer> dismissedPositions) {
        Collection<Integer> result = new ArrayList<Integer>(positions);
        Collections.sort(dismissedPositions, Collections.reverseOrder());
        Collection<Integer> newUndoPositions = new ArrayList<Integer>();
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
