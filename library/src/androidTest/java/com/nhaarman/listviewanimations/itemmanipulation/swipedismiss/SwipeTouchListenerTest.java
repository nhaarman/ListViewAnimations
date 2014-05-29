package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.nineoldandroids.view.ViewHelper;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;


public class SwipeTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    private static final int ANIMATION_SLEEP_DURATION = 1000;

    /**
     * The SwipeTouchListener under test.
     */
    private TestSwipeTouchListener mSwipeTouchListener;

    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * Lists of MotionEvents that can be a applied.
     */
    private List<MotionEvent> mFirstViewMotionEvents;
    private List<MotionEvent> mFirstViewReversedMotionEvents;
    private List<MotionEvent> mLastViewMotionEvents;

    public SwipeTouchListenerTest() {
        super(SwipeTouchListenerTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();
        mSwipeTouchListener = new TestSwipeTouchListener(mActivity.getAbsListView());
        mActivity.getAbsListView().setOnTouchListener(mSwipeTouchListener);

        int viewWidth = mActivity.getAbsListView().getWidth() - 100;
        mFirstViewMotionEvents = createMotionEvents(0, 10, viewWidth);
        mFirstViewReversedMotionEvents = createMotionEvents(0, viewWidth, 10);
        mLastViewMotionEvents = createMotionEvents(mActivity.getAbsListView().getLastVisiblePosition(), 10, viewWidth);

        getInstrumentation().waitForIdleSync();
    }

    private List<MotionEvent> createMotionEvents(final int position, final float fromX, final float toX) {
        int[] listViewCoords = new int[2];
        mActivity.getAbsListView().getLocationOnScreen(listViewCoords);

        View view = mActivity.getAbsListView().getChildAt(position);
        int y = (int) (ViewHelper.getY(view) + view.getHeight() / 2) + listViewCoords[1];

        List<MotionEvent> results = new ArrayList<>();
        results.add(MotionEvent.obtain(0, 0, MotionEvent.ACTION_DOWN, fromX, y, 0));
        for (int i = 0; i < 10; i++) {
            float x = fromX < toX ? toX / 10 * i : fromX - fromX / 10 * i;
            results.add(MotionEvent.obtain(0, 0, MotionEvent.ACTION_MOVE, x, y, 0));
        }
        results.add(MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, toX, y, 0));

        return results;
    }

    /**
     * Tests whether retrieving the AbsListView yields the original AbsListView that was set.
     */
    public void testAbsListViewSet() {
        assertThat("Wrong AbsListView returned", mSwipeTouchListener.getAbsListView(), is(mActivity.getAbsListView()));
    }

    /**
     * Tests whether swiping the first View triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testSwipeCallsAfterFirstViewFling() throws InterruptedException {
        Assert.assertFalse("afterViewFlingCalled == true", mSwipeTouchListener.afterViewFlingCalled);

        for (final MotionEvent event : mFirstViewMotionEvents) {
            mActivity.runOnUiThread(new DispatchTouchEventRunnable(event));
        }

        /* We need to wait for the fling animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        Assert.assertTrue("afterViewFling not called", mSwipeTouchListener.afterViewFlingCalled);
        assertThat("Wrong position called: " + mSwipeTouchListener.position, mSwipeTouchListener.position, is(0));
    }

    /**
     * Tests whether swiping the first View from right to left triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testSwipeCallsAfterFirstReverseViewFling() throws InterruptedException {
        Assert.assertFalse("afterViewFlingCalled == true", mSwipeTouchListener.afterViewFlingCalled);

        for (final MotionEvent event : mFirstViewReversedMotionEvents) {
            mActivity.runOnUiThread(new DispatchTouchEventRunnable(event));
        }

        /* We need to wait for the fling animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        Assert.assertTrue("afterViewFling not called", mSwipeTouchListener.afterViewFlingCalled);
        assertThat("Wrong position called: " + mSwipeTouchListener.position, mSwipeTouchListener.position, is(0));
    }

    /**
     * Tests whether swiping the last View triggers a call to SwipeTouchListener#afterViewFling.
     */
    public void testSwipeCallsAfterLastViewFling() throws InterruptedException {
        Assert.assertFalse("afterViewFlingCalled == true", mSwipeTouchListener.afterViewFlingCalled);

        for (final MotionEvent event : mLastViewMotionEvents) {
            mActivity.runOnUiThread(new DispatchTouchEventRunnable(event));
        }

        /* We need to wait for the fling animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        Assert.assertTrue("afterViewFling not called", mSwipeTouchListener.afterViewFlingCalled);
        assertThat("Wrong position called: " + mSwipeTouchListener.position, mSwipeTouchListener.position, is(mActivity.getAbsListView().getLastVisiblePosition()));
    }

    private static class TestSwipeTouchListener extends SwipeTouchListener {

        boolean afterViewFlingCalled;
        int position;

        TestSwipeTouchListener(final AbsListView absListView) {
            super(absListView);
        }

        @Override
        protected void afterViewFling(final View view, final int position) {
            afterViewFlingCalled = true;
            this.position = position;
        }
    }

    private class DispatchTouchEventRunnable implements Runnable {

        private final MotionEvent mEvent;

        private DispatchTouchEventRunnable(final MotionEvent event) {
            mEvent = event;
        }

        @Override
        public void run() {
            mActivity.getAbsListView().dispatchTouchEvent(mEvent);
        }
    }
}