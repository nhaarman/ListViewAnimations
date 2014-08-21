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

import android.annotation.TargetApi;
import android.app.Instrumentation;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;


import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@TargetApi(14)
public class MotionEventUtils {

    private MotionEventUtils() {
    }

    public static void dispatchDragMotionEvents(final Instrumentation instrumentation, final DynamicListView dynamicListView, final int fromPosition,
                                                final int toPosition) throws InterruptedException {
        int[] location = new int[2];
        dynamicListView.getLocationOnScreen(location);

        View view = dynamicListView.getChildAt(fromPosition);
        float fromY = (int) (view.getY() + view.getHeight() / 2) + location[1];

        View toView = dynamicListView.getChildAt(toPosition);
        float toY = (int) toView.getY() + location[1];

        toY += fromPosition < toPosition ? toView.getHeight() : 0;

        List<MotionEvent> motionEvents = createMotionEvents(dynamicListView, fromY, toY);
        dispatchMotionEvents(instrumentation, motionEvents, true);
    }

    public static void dispatchDragScrollDownMotionEvents(final Instrumentation instrumentation, final DynamicListView dynamicListView,
                                                          final int fromPosition) throws InterruptedException {
        int[] location = new int[2];
        dynamicListView.getLocationOnScreen(location);

        View view = dynamicListView.getChildAt(fromPosition);
        float fromY = (int) (view.getY()) + location[1];

        View toView = dynamicListView.getChildAt(dynamicListView.getLastVisiblePosition());
        float toY = (int) (toView.getY() + toView.getHeight()) + location[1] + 2;

        List<MotionEvent> motionEvents = createMotionEvents(dynamicListView, fromY, toY);
        MotionEvent upEvent = motionEvents.remove(motionEvents.size() - 1);
        dispatchMotionEvents(instrumentation, motionEvents, true);
        Thread.sleep(10000);
        dispatchMotionEvents(instrumentation, Arrays.asList(upEvent), true);
    }

    public static List<MotionEvent> createMotionEvents(final AbsListView absListView, final float fromY, final float toY) {
        int x = (int) (absListView.getX() + absListView.getWidth() / 2);

        List<MotionEvent> results = new ArrayList<>();
        results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, x, fromY, 0));

        float diff = (toY - fromY) / 25;
        float y = fromY;
        for (int i = 0; i < 25; i++) {
            y += diff;
            results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_MOVE, x, y, 0));
        }
        results.add(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, x, toY, 0));

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
                } catch (SecurityException e) {
                    i++;
                    if (i > 3) {
                        throw e;
                    }
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
