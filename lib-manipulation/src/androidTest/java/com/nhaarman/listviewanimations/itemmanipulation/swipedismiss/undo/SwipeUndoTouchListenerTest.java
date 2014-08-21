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

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListenerTestActivity;
import com.nhaarman.listviewanimations.util.AbsListViewWrapper;

import org.mockito.*;

import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.dispatchSwipeMotionEvents;
import static com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.MotionEventUtils.dispatchSwipeMotionEventsAndWait;
import static org.mockito.AdditionalMatchers.*;
import static org.mockito.Mockito.*;

public class SwipeUndoTouchListenerTest extends ActivityInstrumentationTestCase2<SwipeTouchListenerTestActivity> {

    /**
     * An Activity hosting a ListView with items.
     */
    private SwipeTouchListenerTestActivity mActivity;

    /**
     * The AbsListView that is hosted in mActivity.
     */
    private AbsListView mAbsListView;

    @Mock
    private UndoCallback mUndoCallback;

    private SwipeUndoTouchListener mSwipeUndoTouchListener;


    public SwipeUndoTouchListenerTest() {
        super(SwipeTouchListenerTestActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        when(mUndoCallback.getUndoView(any(View.class))).thenReturn(new View(getActivity()));
        when(mUndoCallback.getPrimaryView(any(View.class))).thenReturn(new View(getActivity()));

        mActivity = getActivity();
        mAbsListView = mActivity.getAbsListView();

        mSwipeUndoTouchListener = new SwipeUndoTouchListener(new AbsListViewWrapper(mAbsListView), mUndoCallback);
        mAbsListView.setOnTouchListener(mSwipeUndoTouchListener);

        getInstrumentation().waitForIdleSync();
    }

    /**
     * Tests whether swiping an item once triggers UndoCallback#onUndoShown.
     */
    public void testUndoShown() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));
    }

    /**
     * Tests whether swiping an item twice triggers UndoCallback#onDismiss.
     */
    public void testDismiss() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));
        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        verify(mUndoCallback).onDismiss(any(View.class), eq(0));
    }

    /**
     * Tests whether swiping multiple items triggers onUndoShown, but not onDismiss.
     */
    public void testMultipleUndo() throws InterruptedException {
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(0));

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 1);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(1));

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 2);

        verify(mUndoCallback).onUndoShown(any(View.class), eq(2));

        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());
    }

    /**
     * Tests whether multiple dismisses are correctly handled.
     */
    public void testMultipleDismisses() throws InterruptedException {
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 1);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 2);

        verify(mUndoCallback, times(3)).onUndoShown(any(View.class), anyInt());
        verify(mUndoCallback, never()).onDismiss(any(View.class), anyInt());

        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 1);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 2);

        verify(mUndoCallback, times(3)).onDismiss(any(View.class), anyInt());
        verify(mUndoCallback).onDismiss(eq(mAbsListView), aryEq(new int[]{2, 1, 0}));
    }

    /**
     * Tests whether the last item is dismissable after some other items have been dismissed.
     */
    public void testLastItemDismissable_itemsDismissed() throws InterruptedException {
        /* Given some items are dismissed */
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 1);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 2);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 0);
        dispatchSwipeMotionEvents(getInstrumentation(), mAbsListView, 1);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 2);

        /* When trying to dismiss the last item */
        int lastPosition = mAbsListView.getAdapter().getCount() - 1;
        mAbsListView.smoothScrollToPosition(lastPosition);

        Thread.sleep(15000); // Wait for the smooth scroll to settle;

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, lastPosition); // Swipe to show undo
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, lastPosition); // Swipe to dismiss

        /* Then I should be notified of dismissing the last item. */
        verify(mUndoCallback).onDismiss(any(View.class), eq(lastPosition));
    }

    /**
     * Tests whether the last item is dismissable after some an item has been dismissed and undone.
     */
    public void testLastItemDismissable_itemUndone() throws InterruptedException {
        /* Given an item is dismissed and undone */
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, 0);
        mSwipeUndoTouchListener.undo(mAbsListView.getChildAt(0));

        /* When trying to dismiss the last item */
        int lastPosition = mAbsListView.getAdapter().getCount() - 1;
        mAbsListView.smoothScrollToPosition(lastPosition);

        Thread.sleep(5000); // Wait for the smooth scroll to settle;

        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, lastPosition); // Swipe to show undo
        mAbsListView.smoothScrollToPosition(lastPosition);
        Thread.sleep(5000);
        dispatchSwipeMotionEventsAndWait(getInstrumentation(), mAbsListView, lastPosition); // Swipe to dismiss

        /* Then I should be notified of dismissing the last item. */
        verify(mUndoCallback).onDismiss(any(View.class), eq(lastPosition));
    }
}