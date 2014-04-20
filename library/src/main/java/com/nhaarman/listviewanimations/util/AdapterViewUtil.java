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

package com.nhaarman.listviewanimations.util;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class AdapterViewUtil {

    private AdapterViewUtil() {
    }

    /**
     * Returns the position within the adapter's dataset for the view, where view is an adapter item or a descendant of an adapter item.
     * Unlike {@link AdapterView#getPositionForView(android.view.View)}, returned position will reflect the position of the item given view is representing,
     * by subtracting the header views count.
     * @param adapterView the AdapterView containing the view.
     * @param view an adapter item or a descendant of an adapter item. This must be visible in given AdapterView at the time of the call.
     * @return the position of the item in the AdapterView represented by given view, or {@link AdapterView#INVALID_POSITION} if the view does not
     * correspond to a list item (or it is not visible).
     */
    public static int getPositionForView(final AdapterView<?> adapterView, final View view) {
        int position = adapterView.getPositionForView(view);

        if (adapterView instanceof ListView) {
            position -= ((ListView) adapterView).getHeaderViewsCount();
        }

        return position;
    }

    /**
     * Returns the {@link View} that represents the item for given position.
     * @param adapterView the {@link android.widget.AdapterView} that should be examined
     * @param position the position for which the {@code View} should be returned.
     * @return the {@code View}, or {@code null} if the position is not currently visible.
     */
    public static View getViewForPosition(final AdapterView<?> adapterView, final int position) {
        int childCount = adapterView.getChildCount();
        View downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = adapterView.getChildAt(i);
            if (getPositionForView(adapterView, child) == position) {
                downView = child;
            }
        }
        return downView;
    }
}
