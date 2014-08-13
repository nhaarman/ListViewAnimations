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

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

/**
 * A {@code View} which shows a number of colored dots in a grid.
 */
@SuppressWarnings("UnnecessaryFullyQualifiedName")
public class GripView extends View {

    public static final int DEFAULT_DOT_COLOR = android.R.color.darker_gray;
    public static final float DEFAULT_DOT_SIZE_RADIUS_DP = 2;
    public static final int DEFAULT_COLUMN_COUNT = 2;

    private static final int[] ATTRS = {android.R.attr.color};

    /**
     * The {@code Paint} that is used to draw the dots.
     */
    private final Paint mDotPaint;

    /**
     * The radius in pixels of the dots.
     */
    private float mDotSizeRadiusPx;

    /**
     * The calculated top padding to make sure the dots are centered. Calculated in {@link #onSizeChanged(int, int, int, int)}.
     */
    private float mPaddingTop;

    /**
     * The number of columns.
     */
    private int mColumnCount = DEFAULT_COLUMN_COUNT;

    /**
     * The number of rows. Calculated in {@link #onSizeChanged(int, int, int, int)}.
     */
    private int mRowCount;

    public GripView(@NonNull final Context context) {
        this(context, null);
    }

    public GripView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GripView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int color = getResources().getColor(DEFAULT_DOT_COLOR);
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);
            color = a.getColor(0, color);
            a.recycle();
        }
        mDotPaint.setColor(color);

        Resources r = context.getResources();
        mDotSizeRadiusPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_DOT_SIZE_RADIUS_DP, r.getDisplayMetrics());
    }

    /**
     * Sets the color of the dots. Defaults to {@link #DEFAULT_DOT_COLOR}.
     */
    public void setColor(@ColorRes final int colorResId) {
        mDotPaint.setColor(getResources().getColor(colorResId));
    }

    /**
     * Sets the radius in pixels of the dots. Defaults to {@value #DEFAULT_DOT_SIZE_RADIUS_DP} dp.
     */
    public void setDotSizeRadiusPx(final float dotSizeRadiusPx) {
        mDotSizeRadiusPx = dotSizeRadiusPx;
    }

    /**
     * Sets the number of horizontal dots. Defaults to {@value #DEFAULT_COLUMN_COUNT}.
     */
    public void setColumnCount(final int columnCount) {
        mColumnCount = columnCount;
        requestLayout();
    }

    @SuppressWarnings("ParameterNameDiffersFromOverriddenParameter")
    @Override
    protected void onSizeChanged(final int width, final int height, final int oldWidth, final int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);

        mRowCount = (int) ((height - getPaddingTop() - getPaddingBottom()) / (mDotSizeRadiusPx * 4));
        mPaddingTop = (height - mRowCount * mDotSizeRadiusPx * 2 - (mRowCount - 1) * mDotSizeRadiusPx * 2) / 2;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        int width = MeasureSpec.makeMeasureSpec(getPaddingLeft() + getPaddingRight() + (int) (mColumnCount * (mDotSizeRadiusPx * 4 - 2)), MeasureSpec.EXACTLY);
        super.onMeasure(width, heightMeasureSpec);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < mColumnCount; i++) {
            float x = getPaddingLeft() + i * 2 * mDotSizeRadiusPx * 2;
            for (int j = 0; j < mRowCount; j++) {
                float y = mPaddingTop + j * 2 * mDotSizeRadiusPx * 2;
                canvas.drawCircle(x + mDotSizeRadiusPx, y + mDotSizeRadiusPx, mDotSizeRadiusPx, mDotPaint);
            }
        }
    }
}
