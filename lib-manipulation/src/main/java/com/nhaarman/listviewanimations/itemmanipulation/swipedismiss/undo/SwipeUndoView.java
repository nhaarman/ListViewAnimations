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

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

/**
 * A convenience class which holds a primary and a undo {@link android.view.View}.
 */
class SwipeUndoView extends FrameLayout {

    /**
     * The primary {@link android.view.View}.
     */
    @Nullable
    private View mPrimaryView;

    /**
     * The undo {@link android.view.View}.
     */
    @Nullable
    private View mUndoView;

    /**
     * Creates a new {@code SwipeUndoView}.
     */
    SwipeUndoView(final Context context) {
        super(context);
    }

    /**
     * Sets the primary {@link android.view.View}. Removes any existing primary {@code View} if present.
     */
    void setPrimaryView(@NonNull final View primaryView) {
        if (mPrimaryView != null) {
            removeView(mPrimaryView);
        }
        mPrimaryView = primaryView;
        addView(mPrimaryView);
    }

    /**
     * Sets the undo {@link android.view.View}. Removes any existing primary {@code View} if present, and sets the visibility of the {@code undoView} to {@link #GONE}.
     */
    void setUndoView(@NonNull final View undoView) {
        if (mUndoView != null) {
            removeView(mUndoView);
        }
        mUndoView = undoView;
        mUndoView.setVisibility(GONE);
        addView(mUndoView);
    }

    /**
     * Returns the primary {@link android.view.View}.
     */
    @Nullable
    View getPrimaryView() {
        return mPrimaryView;
    }

    /**
     * Returns the undo {@link android.view.View}.
     */
    @Nullable
    View getUndoView() {
        return mUndoView;
    }
}
