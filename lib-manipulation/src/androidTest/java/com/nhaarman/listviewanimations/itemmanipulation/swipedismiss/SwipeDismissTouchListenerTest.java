package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.AbsListView;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.dispatchSwipeMotionEvents;
import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.dispatchSwipeMotionEventsAndWait;
import static org.mockito.AdditionalMatchers.aryEq;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@SuppressWarnings("AnonymousInnerClass")
public class SwipeDismissTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * The AbsListView hosted by mActivity.
     */
    private AbsListView mAbsListView;

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
        mAbsListView = mActivity.getAbsListView();

        mSwipeTouchListener = new SwipeDismissTouchListener(mAbsListView, mOnDismissCallback);
        mAbsListView.setOnTouchListener(mSwipeTouchListener);

        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests whether dismissing an item triggers a call to OnDismissCallback#onDismiss.
     */
    public void testSimpleDismiss() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 0);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{0}));
    }

    /**
     * Tests whether dismissing the first and second items triggers a correct call to OnDismissCallback#onDismiss.
     */
    public void testDoubleDismiss() throws InterruptedException {
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 0);
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 1);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{1, 0}));
    }

    /**
     * Tests whether dismissing mixed positions triggers a correct call to OnDismissCallback#onDismiss.
     */
    public void testComplexDismiss() throws InterruptedException {
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 0);
        dispatchSwipeMotionEvents(mActivity, mAbsListView, 3);
        dispatchSwipeMotionEventsAndWait(mActivity, mAbsListView, 2);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{3, 2, 0}));
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
        Thread.sleep(1500);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{0}));
    }

    /**
     * Tests whether calling SwipeTouchListener#dismiss triggers a call to OnDismissCallback#onDismiss.
     */
    public void testDismiss() throws InterruptedException {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSwipeTouchListener.dismiss(0);
            }
        });

        /* We need to wait for the animation to complete */
        Thread.sleep(1500);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{0}));
    }
}