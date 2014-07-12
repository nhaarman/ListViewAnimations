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

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

class ViewUtils {

    private ViewUtils() {
    }

    @SuppressWarnings("ObjectEquality")
    static Rect getChildViewRect(final View parentView, final View childView) {
        final Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (parentView == childView) {
            return childRect;
        }

        View view = childView;
        ViewGroup parent;
        while ((parent = (ViewGroup) view.getParent()) != parentView) {
            childRect.offset(parent.getLeft(), parent.getTop());
            view = parent;
        }

        return childRect;
    }

}
