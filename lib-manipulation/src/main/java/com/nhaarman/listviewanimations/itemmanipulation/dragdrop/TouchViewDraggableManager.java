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

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

public class TouchViewDraggableManager implements DraggableManager {

    @IdRes
    private final int mTouchViewResId;

    public TouchViewDraggableManager(@IdRes final int touchViewResId) {
        mTouchViewResId = touchViewResId;
    }

    @Override
    public boolean isDraggable(@NonNull final View view, final int position, final float x, final float y) {
        View touchView = view.findViewById(mTouchViewResId);
        if (touchView != null) {
            boolean xHit = touchView.getLeft() <= x && touchView.getRight() >= x;
            boolean yHit = touchView.getTop() <= y && touchView.getBottom() >= y;
            return xHit && yHit;
        } else {
            return false;
        }
    }
}
