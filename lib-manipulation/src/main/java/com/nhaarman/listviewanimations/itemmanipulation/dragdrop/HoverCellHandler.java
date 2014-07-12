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

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;

class HoverCellHandler {

    private static final String BOUNDS = "bounds";

    @NonNull
    private Drawable mHoverCell;

    @NonNull
    private Rect mHoverCellCurrentBounds;

    @NonNull
    private Rect mHoverCellOriginalBounds;


    HoverCellHandler(@NonNull final View view, @Nullable final OnHoverCellListener onHoverCellListener) {
        int w = view.getWidth();
        int h = view.getHeight();
        int top = view.getTop();
        int left = view.getLeft();

        Bitmap bitmap = BitmapUtils.getBitmapFromView(view);

        mHoverCell = new BitmapDrawable(view.getContext().getResources(), bitmap);
        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);
        mHoverCell.setBounds(mHoverCellCurrentBounds);

        if (onHoverCellListener != null) {
            mHoverCell = onHoverCellListener.onHoverCellCreated(mHoverCell);
        }
    }


    void offset(final int dY) {
        mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top + dY);
        mHoverCell.setBounds(mHoverCellCurrentBounds);
    }

    void offsetTo(final int newY) {
        mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, newY);
    }

    void draw(@NonNull final Canvas canvas) {
        mHoverCell.draw(canvas);
    }

    ObjectAnimator createAnimateToEndPositionAnimator() {
        return ObjectAnimator.ofObject(mHoverCell, BOUNDS, new BoundEvaluator(), mHoverCellCurrentBounds);
    }

    int getTop() {
        return mHoverCellOriginalBounds.top;
    }

    @NonNull
    public Rect getHoverCellCurrentBounds() {
        return mHoverCellCurrentBounds;
    }

    private static class BoundEvaluator implements TypeEvaluator<Rect> {

        @NonNull
        @Override
        public Rect evaluate(final float fraction, @NonNull final Rect startValue, @NonNull final Rect endValue) {
            return new Rect(
                    interpolate(startValue.left, endValue.left, fraction),
                    interpolate(startValue.top, endValue.top, fraction),
                    interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction)
            );
        }

        private static int interpolate(final int start, final int end, final float fraction) {
            return (int) (start + fraction * (end - start));
        }
    }

}
