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

import android.test.ActivityInstrumentationTestCase2;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.util.AbsListViewWrapper;

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

        mSwipeTouchListener = new SwipeDismissTouchListener(new AbsListViewWrapper(mAbsListView), mOnDismissCallback);
        mAbsListView.setOnTouchListener(mSwipeTouchListener);

        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests whether dismissing an item triggers a call to OnDismissCallback#onDismiss.
     */
    public void testSimpleDismiss() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{0}));
    }

    /**
     * Tests whether dismissing the first and second items triggers a correct call to OnDismissCallback#onDismiss.
     */
    public void testDoubleDismiss() throws InterruptedException {
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 1);

        verify(mOnDismissCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{1, 0}));
    }

    /**
     * Tests whether dismissing mixed positions triggers a correct call to OnDismissCallback#onDismiss.
     */
    public void testComplexDismiss() throws InterruptedException {
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 3);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 2);

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