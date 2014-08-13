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
import android.test.AndroidTestCase;
import android.view.MotionEvent;
import android.view.View;

import org.mockito.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("ALL")
public class HoverDrawableTest extends AndroidTestCase {

    private static final int START_Y = 29;

    private HoverDrawable mHoverDrawable;

    private View mView;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mView = new MyMockView(getContext());
        mView.measure(0, 0);
        mHoverDrawable = new HoverDrawable(mView, START_Y);
    }

    public void testInitialState() {
        assertThat(mHoverDrawable.getDeltaY(), is(0));
        assertThat(mHoverDrawable.getBounds().top, is(0));
        assertThat(mHoverDrawable.isMovingUpwards(), is(false));
    }

    public void testMovedState() {
        MotionEvent motionEvent = createMotionEvent(START_Y + 10);
        mHoverDrawable.handleMoveEvent(motionEvent);

        assertThat(mHoverDrawable.getDeltaY(), is(10));
        assertThat(mHoverDrawable.getBounds().top, is(10));
        assertThat(mHoverDrawable.isMovingUpwards(), is(false));
    }

    public void testDoubleMovedState() {
        MotionEvent motionEvent = createMotionEvent(START_Y + 10);
        mHoverDrawable.handleMoveEvent(motionEvent);

        MotionEvent motionEvent2 = createMotionEvent(START_Y + 20);
        mHoverDrawable.handleMoveEvent(motionEvent2);

        assertThat(mHoverDrawable.getDeltaY(), is(20));
        assertThat(mHoverDrawable.getBounds().top, is(20));
        assertThat(mHoverDrawable.isMovingUpwards(), is(false));
    }

    public void testReversedMovedState() {
        MotionEvent motionEvent = createMotionEvent(START_Y - 10);
        mHoverDrawable.handleMoveEvent(motionEvent);

        assertThat(mHoverDrawable.getDeltaY(), is(-10));
        assertThat(mHoverDrawable.getBounds().top, is(-10));
        assertThat(mHoverDrawable.isMovingUpwards(), is(true));
    }

    public void testReversedDoubleMovedState() {
        MotionEvent motionEvent = createMotionEvent(START_Y - 10);
        mHoverDrawable.handleMoveEvent(motionEvent);

        MotionEvent motionEvent2 = createMotionEvent(START_Y - 20);
        mHoverDrawable.handleMoveEvent(motionEvent2);

        assertThat(mHoverDrawable.getDeltaY(), is(-20));
        assertThat(mHoverDrawable.getBounds().top, is(-20));
        assertThat(mHoverDrawable.isMovingUpwards(), is(true));
    }

    public void testShift() {
        MotionEvent motionEvent = createMotionEvent(START_Y - 10);
        mHoverDrawable.handleMoveEvent(motionEvent);

        mHoverDrawable.shift(10);

        assertThat(mHoverDrawable.getDeltaY(), is(0));
        assertThat(mHoverDrawable.isMovingUpwards(), is(false));
        assertThat(mHoverDrawable.getBounds().top, is(-10));
    }

    public void testScroll() {
        mHoverDrawable.onScroll(20);

        assertThat(mHoverDrawable.getDeltaY(), is(-20));
        assertThat(mHoverDrawable.isMovingUpwards(), is(true));
        assertThat(mHoverDrawable.getBounds().top, is(0));
    }

    private static MotionEvent createMotionEvent(final float y) {
        return MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, 0, y, 0);
    }

    private static class MyMockView extends View {

        MyMockView(final Context context) {
            super(context);
        }

        @Override
        protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
            setMeasuredDimension(100, 100);
        }
    }
}