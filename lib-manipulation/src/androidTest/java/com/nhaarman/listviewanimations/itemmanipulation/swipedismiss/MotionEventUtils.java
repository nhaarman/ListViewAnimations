package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.app.Activity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;

import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

public class MotionEventUtils {

    private MotionEventUtils() {
    }

    public static void dispatchSwipeMotionEventsAndWait(final Activity activity, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEventsAndWait(activity, absListView, createSwipeMotionEvents(absListView, position));
    }

    public static void dispatchReverseSwipeMotionEventsAndWait(final Activity activity, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEventsAndWait(activity, absListView, createReverseSwipeMotionEvents(absListView, position));
    }

    public static void dispatchMotionEventsAndWait(final Activity activity, final View view, final Iterable<MotionEvent> motionEvents) throws InterruptedException {
        dispatchMotionEvents(activity, view, motionEvents, true);
    }

    public static void dispatchSwipeMotionEvents(final Activity activity, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEvents(activity, absListView, createSwipeMotionEvents(absListView, position));
    }

    public static void dispatchReverseSwipeMotionEvents(final Activity activity, final AbsListView absListView, final int position) throws InterruptedException {
        dispatchMotionEvents(activity, absListView, createReverseSwipeMotionEvents(absListView, position));
    }

    public static void dispatchMotionEvents(final Activity activity, final View view, final Iterable<MotionEvent> motionEvents) throws InterruptedException {
        dispatchMotionEvents(activity, view, motionEvents, false);
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

        View view = absListView.getChildAt(position);
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

    private static void dispatchMotionEvents(final Activity activity, final View view, final Iterable<MotionEvent> motionEvents, final boolean wait) throws InterruptedException {
        for (final MotionEvent event : motionEvents) {
            activity.runOnUiThread(new DispatchTouchEventRunnable(event, view));
            Thread.sleep(100);
        }

        if (wait) {
        /* We need to wait for the fling animation to complete */
            Thread.sleep(1500);
        }
    }

    private static class DispatchTouchEventRunnable implements Runnable {

        private final MotionEvent mEvent;
        private final View mView;

        private DispatchTouchEventRunnable(final MotionEvent event, final View view) {
            mEvent = event;
            mView = view;
        }

        @Override
        public void run() {
            mView.dispatchTouchEvent(mEvent);
        }
    }

}
