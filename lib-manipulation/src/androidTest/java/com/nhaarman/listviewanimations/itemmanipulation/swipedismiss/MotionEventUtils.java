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

import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class MotionEventUtils {

    private MotionEventUtils() {
    }

    public static void dispatchSwipeMotionEventsAndWait(final Instrumentation instrumentation, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEventsAndWait(instrumentation, createSwipeMotionEvents(absListView, position));
    }

    public static void dispatchReverseSwipeMotionEventsAndWait(final Instrumentation instrumentation, final AbsListView absListView,
                                                               final int position) throws InterruptedException {
        dispatchMotionEventsAndWait(instrumentation, createReverseSwipeMotionEvents(absListView, position));
    }

    public static void dispatchMotionEventsAndWait(final Instrumentation instrumentation, final Iterable<MotionEvent> motionEvents) throws InterruptedException {
        dispatchMotionEvents(instrumentation, motionEvents, true);
    }

    public static void dispatchSwipeMotionEvents(final Instrumentation instrumentation, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEvents(instrumentation, createSwipeMotionEvents(absListView, position));
    }

    public static void dispatchReverseSwipeMotionEvents(final Instrumentation instrumentation, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEvents(instrumentation, createReverseSwipeMotionEvents(absListView, position));
    }

    public static void dispatchMotionEvents(final Instrumentation instrumentation, final Iterable<MotionEvent> motionEvents) throws InterruptedException {
        dispatchMotionEvents(instrumentation, motionEvents, false);
    }

    public static List<MotionEvent> createSwipeMotionEvents(final AbsListView absListView, final int position) {
        int viewWidth = absListView.getWidth();
        return createMotionEvents(absListView, position, 10, viewWidth - 10);
    }

    public static List<MotionEvent> createReverseSwipeMotionEvents(final AbsListView absListView, final int position) {
        int viewWidth = absListView.getWidth();
        return createMotionEvents(absListView, position, viewWidth - 10, 10);
    }

    public static List<MotionEvent> createMotionEvents(final AbsListView absListView, final int position, final float fromX, final float toX) {
        int[] listViewCoords = new int[2];
        absListView.getLocationOnScreen(listViewCoords);

        View view = absListView.getChildAt(position - absListView.getFirstVisiblePosition());
        int y = (int) (ViewHelper.getY(view) + view.getHeight() / 2) + listViewCoords[1];

        List<MotionEvent> results = new ArrayList<>();
        results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, fromX, y, 0));

        float diff = fromX - toX;
        for (int i = 1; i < 10; i++) {
            float x = fromX + diff / 10 * i;
            results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x, y, 0));
        }
        results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, toX, y, 0));

        return results;
    }

    public static void dispatchMotionEvents(final Instrumentation instrumentation, final Iterable<MotionEvent> motionEvents, final boolean wait) throws InterruptedException {
        for (final MotionEvent event : motionEvents) {
            int i = 0;
            boolean success = false;
            do {
                try {
                    instrumentation.sendPointerSync(event);
                    success = true;
                } catch (SecurityException ignored) {
                    i++;
                }
            } while (i < 3 && !success);
            Thread.sleep(100);
        }

        if (wait) {
        /* We need to wait for the fling animation to complete */
            Thread.sleep(1500);
        }
    }

}
