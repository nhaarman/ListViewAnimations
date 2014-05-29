package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.test.ActivityInstrumentationTestCase2;
import android.view.MotionEvent;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@SuppressWarnings("AnonymousInnerClass")
public class SwipeDismissTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    private static final int ANIMATION_SLEEP_DURATION = 1000;
    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * The SwipeTouchListener under test.
     */
    private SwipeDismissTouchListener mSwipeTouchListener;

    @Mock
    private OnDismissCallback mOnDismissCallback;

    public SwipeDismissTouchListenerTest() {
        super(SwipeTouchListenerTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mActivity = getActivity();
        mSwipeTouchListener = new SwipeDismissTouchListener(mActivity.getAbsListView(), mOnDismissCallback);
        mActivity.getAbsListView().setOnTouchListener(mSwipeTouchListener);

        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests whether dismissing an item triggers a call to OnDismissCallback#onDismiss.
     */
    public void testSimpleDismiss() throws InterruptedException {
        List<MotionEvent> swipeMotionEvents = MotionEventUtils.createSwipeMotionEvents(mActivity.getAbsListView(), 0);
        MotionEventUtils.dispatchMotionEvents(mActivity, mActivity.getAbsListView(), swipeMotionEvents);

        /* We need to wait for the animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        verify(mOnDismissCallback).onDismiss(eq(mActivity.getAbsListView()), aryEq(new int[]{0}));
    }

    /**
     * Tests whether dismissing the first and second items triggers a correct call to OnDismissCallback#onDismiss.
     */
    public void testDoubleDismiss() throws InterruptedException {
        List<MotionEvent> swipeMotionEvents = MotionEventUtils.createSwipeMotionEvents(mActivity.getAbsListView(), 0);
        MotionEventUtils.dispatchMotionEvents(mActivity, mActivity.getAbsListView(), swipeMotionEvents);

        List<MotionEvent> swipeMotionEvents2 = MotionEventUtils.createSwipeMotionEvents(mActivity.getAbsListView(), 1);
        MotionEventUtils.dispatchMotionEvents(mActivity, mActivity.getAbsListView(), swipeMotionEvents2);

        /* We need to wait for the animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        verify(mOnDismissCallback).onDismiss(eq(mActivity.getAbsListView()), aryEq(new int[]{1, 0}));
    }

    /**
     * Tests whether calling SwipeTouchListener#fling triggers a call to OnDismissCallback#onDismiss.
     */
    public void testFling() throws InterruptedException {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeTouchListener.fling(0);
            }
        });

        /* We need to wait for the animation to complete */
        Thread.sleep(ANIMATION_SLEEP_DURATION);

        verify(mOnDismissCallback).onDismiss(eq(mActivity.getAbsListView()), aryEq(new int[]{0}));
    }
}