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

import android.support.annotation.NonNull;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import org.mockito.*;

import static org.mockito.Mockito.*;

public class DynamicListViewDragAndDropTest extends ActivityInstrumentationTestCase2<DynamicListViewTestActivity> {

    private DynamicListView mDynamicListView;

    @Mock
    private OnItemMovedListener mOnItemMovedListener;


    public DynamicListViewDragAndDropTest() {
        super(DynamicListViewTestActivity.class);
    }

    @Override
    public void setUp() throws Exception, InterruptedException {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mDynamicListView = getActivity().getDynamicListView();
        mDynamicListView.enableDragAndDrop();
        mDynamicListView.setDraggableManager(new MyDraggableManager());
        mDynamicListView.setOnItemMovedListener(mOnItemMovedListener);

        getInstrumentation().waitForIdleSync();
        Thread.sleep(5000);
    }

    public void testOnItemMovedListenerCalled() throws InterruptedException {
        MotionEventUtils.dispatchDragMotionEvents(getInstrumentation(), mDynamicListView, 0, 1);
        verify(mOnItemMovedListener).onItemMoved(0, 1);
    }

    public void testReverseOnItemMovedListenerCalled() throws InterruptedException {
        MotionEventUtils.dispatchDragMotionEvents(getInstrumentation(), mDynamicListView, 2, 1);
        verify(mOnItemMovedListener).onItemMoved(2, 1);
    }

    public void testOnItemMovedListenerCalledMultipleItems() throws InterruptedException {
        MotionEventUtils.dispatchDragMotionEvents(getInstrumentation(), mDynamicListView, 1, 5);
        verify(mOnItemMovedListener).onItemMoved(1, 5);
    }

    public void testScroll() throws InterruptedException {
        MotionEventUtils.dispatchDragScrollDownMotionEvents(getInstrumentation(), mDynamicListView, 1);
        verify(mOnItemMovedListener).onItemMoved(1, 19);
    }

    private static class MyDraggableManager implements DraggableManager {

        @Override
        public boolean isDraggable(@NonNull final View view, final int position, final float x, final float y) {
            return true;
        }
    }
}