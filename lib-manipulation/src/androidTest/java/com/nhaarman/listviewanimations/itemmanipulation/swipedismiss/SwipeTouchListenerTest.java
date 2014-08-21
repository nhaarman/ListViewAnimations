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

package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.support.annotation.NonNull;
import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.util.AbsListViewWrapper;

import java.util.List;

import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.dispatchSwipeMotionEventsAndWait;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


@SuppressWarnings({"AnonymousInnerClass", "AnonymousInnerClassMayBeStatic"})
public class SwipeTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    /**
     * The SwipeTouchListener under test.
     */
    private TestSwipeTouchListener mSwipeTouchListener;

    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * The AbsListView that is hosted in mActivity.
     */
    private AbsListView mAbsListView;

    /**
     * The width of the AbsListView.
     */
    private float mViewWidth;

    public SwipeTouchListenerTest() {
        super(SwipeTouchListenerTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
        mAbsListView = mActivity.getAbsListView();
        mViewWidth = mAbsListView.getWidth();

        mSwipeTouchListener = new TestSwipeTouchListener(new AbsListViewWrapper(mAbsListView));
        mAbsListView.setOnTouchListener(mSwipeTouchListener);

        getInstrumentation().waitForIdleSync();
    }


    /**
     * Tests whether retrieving the AbsListView yields the original AbsListView that was set.
     */
    public void testAbsListViewSet() {
        assertThat(mSwipeTouchListener.getListViewWrapper().getListView(), is((ViewGroup) mAbsListView));
    }

    /**
     * Tests whether swiping the first View triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testSwipeFirstViewCallback() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(true));
        assertThat(mSwipeTouchListener.position, is(0));
    }

    /**
     * Tests whether swiping the first View from right to left triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testReverseSwipeFirstViewCallback() throws InterruptedException {
        MotionEventUtils.dispatchReverseSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(true));
        assertThat(mSwipeTouchListener.position, is(0));
    }

    /**
     * Tests whether swiping the last View triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testSwipeLastViewCallback() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, mAbsListView.getLastVisiblePosition() - 1);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(true));
        assertThat(mSwipeTouchListener.position, is(mAbsListView.getLastVisiblePosition() - 1));
    }

    /**
     * Tests whether swiping shorter than half of the view width doesn't trigger a call to SwipeTouchLister#afterViewFling.
     */
    public void testShortSwipe() throws InterruptedException {
        List<MotionEvent> motionEvents = MotionEventUtils.createMotionEvents(mAbsListView, 0, 10, mViewWidth / 2 - mViewWidth / 10);
        MotionEventUtils.dispatchMotionEventsAndWait(getInstrumentation(), motionEvents);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(false));
    }

    /**
     * Tests whether swiping shorter than half of the view width from right to left doesn't trigger a call to SwipeTouchLister#afterViewFling.
     */
    public void testReverseShortSwipe() throws InterruptedException {
        List<MotionEvent> motionEvents = MotionEventUtils.createMotionEvents(mAbsListView, 0, mViewWidth - 10, mViewWidth / 2 + mViewWidth / 10);
        MotionEventUtils.dispatchMotionEventsAndWait(getInstrumentation(), motionEvents);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(false));
    }

    /**
     * Tests whether calling SwipeTouchListener#fling(int) triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testFling() throws InterruptedException {
        mActivity.runOnUiThread(
                new Runnable() {

                    @Override
                    public void run() {
                        mSwipeTouchListener.fling(0);
                    }
                }
        );

        Thread.sleep(1000);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(true));
        assertThat(mSwipeTouchListener.position, is(0));
    }

    /**
     * Tests whether trying to dismiss an item that is specified not to be dismissable doesn't trigger a call to SwipeTouchListener#afterViewFling.
     */
    public void testDismissableManager() throws InterruptedException {
        mSwipeTouchListener.setDismissableManager(
                new DismissableManager() {

                    @Override
                    public boolean isDismissable(final long id, final int position) {
                        return false;
                    }
                }
        );

        List<MotionEvent> motionEvents = MotionEventUtils.createMotionEvents(mAbsListView, 0, 10, mViewWidth - 10);
        MotionEventUtils.dispatchMotionEvents(getInstrumentation(), motionEvents);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(false));
    }

    /**
     * Tests whether the isSwiping method returns proper values.
     */
    public void testIsSwiping() throws InterruptedException {
        List<MotionEvent> motionEvents = MotionEventUtils.createMotionEvents(mAbsListView, 0, 10, mViewWidth - 10);

        assertThat(mSwipeTouchListener.isSwiping(), is(false));

        /* Send first half of the MotionEvents */
        MotionEventUtils.dispatchMotionEvents(getInstrumentation(), motionEvents.subList(0, motionEvents.size() / 2));

        assertThat(mSwipeTouchListener.isSwiping(), is(true));

        /* Send second half of the MotionEvents */
        MotionEventUtils.dispatchMotionEvents(getInstrumentation(), motionEvents.subList(motionEvents.size() / 2, motionEvents.size()));

        assertThat(mSwipeTouchListener.isSwiping(), is(false));
    }

    /**
     * Test whether disabling swipe and swiping an item does not trigger SwipeTouchListener#afterViewFling, and enabling it again does trigger the call.
     */
    public void testEnableDisableSwipe() throws InterruptedException {
        mSwipeTouchListener.disableSwipe();

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(false));

        mSwipeTouchListener.enableSwipe();

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        assertThat(mSwipeTouchListener.afterViewFlingCalled, is(true));
    }

    private static class TestSwipeTouchListener extends SwipeTouchListener {

        boolean afterViewFlingCalled;

        int position;

        TestSwipeTouchListener(final AbsListViewWrapper absListViewWrapper) {
            super(absListViewWrapper);
        }

        @Override
        protected boolean willLeaveDataSetOnFling(@NonNull final View view, final int position) {
            return true;
        }

        @Override
        protected void afterViewFling(@NonNull final View view, final int position) {
            afterViewFlingCalled = true;
            this.position = position;
        }
    }
}