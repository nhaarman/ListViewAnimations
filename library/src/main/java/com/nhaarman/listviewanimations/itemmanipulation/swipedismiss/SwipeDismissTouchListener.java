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

import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * A {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListener} that directly dismisses the items when swiped.
 */
public class SwipeDismissTouchListener extends SwipeTouchListener {

    /**
     * The callback which gets notified of dismissed items.
     */
    private final OnDismissCallback mCallback;

    /**
     * The duration of the dismiss animation
     */
    private final long mDismissAnimationTime;

    /**
     * The {@link View}s that have been dismissed.
     */
    private final Collection<View> mDismissedViews = new LinkedList<View>();

    /**
     * The dismissed positions.
     */
    private final List<Integer> mDismissedPositions = new LinkedList<Integer>();

    /**
     * The number of active dismiss animations.
     */
    private int mActiveDismissCount;

    /**
     * A handler for posting {@link java.lang.Runnable}s.
     */
    private final Handler mHandler = new Handler();

    /**
     * Constructs a new {@code SwipeDismissTouchListener} for the given {@link android.widget.AbsListView}.
     *  @param absListView
     *            The {@code AbsListView} whose items should be dismissable.
     * @param callback
     *            The callback to trigger when the user has indicated that he
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public SwipeDismissTouchListener(final AbsListView absListView, final OnDismissCallback callback) {
        super(absListView);
        mCallback = callback;
        mDismissAnimationTime = absListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    /**
     * Dismisses the {@link View} corresponding to given position.
     * Calling this method has the same effect as manually swiping an item off the screen.
     * @param position the position of the item in the {@link android.widget.ListAdapter}. Must be visible.
     */
    public void dismiss(final int position) {
        fling(position);
    }

    @Override
    public void fling(final int position) {
        int firstVisiblePosition = getAbsListView().getFirstVisiblePosition();
        int lastVisiblePosition = getAbsListView().getLastVisiblePosition();

        if (firstVisiblePosition <= position && position <= lastVisiblePosition) {
            super.fling(position);
        } else if (position > lastVisiblePosition) {
            directDismiss(position);
        } else {
            dismissAbove(position);
        }
    }

    protected void directDismiss(final int position) {
        mDismissedPositions.add(position);
        finalizeDismiss();
    }

    private void dismissAbove(final int position) {
        View view = AdapterViewUtil.getViewForPosition(getAbsListView(), getAbsListView().getFirstVisiblePosition());
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        final int scrollDistance = view.getMeasuredHeight();

        getAbsListView().smoothScrollBy(scrollDistance, (int) mDismissAnimationTime);
        mHandler.postDelayed(new RestoreScrollRunnable(scrollDistance, position), mDismissAnimationTime);
    }

    @Override
    protected void afterCancelSwipe(final View view, final int position) {
        finalizeDismiss();
    }

    @Override
    protected void afterViewFling(final View view, final int position) {
        performDismiss(view, position);
    }

    /**
     * Animates the dismissed list item to zero-height and fires the dismiss callback when all dismissed list item animations have completed.
     * @param view the dismissed {@link View}.
     */
    protected void performDismiss(final View view, final int position) {
        mDismissedViews.add(view);
        mDismissedPositions.add(position);

        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), 1).setDuration(mDismissAnimationTime);
        animator.addUpdateListener(new DismissAnimatorUpdateListener(view));
        animator.addListener(new DismissAnimatorListener());
        animator.start();

        mActiveDismissCount++;
    }

    /**
     * If necessary, notifies the {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} to remove dismissed object from the adapter,
     * and restores the {@link View} presentations.
     */
    protected void finalizeDismiss() {
        if (mActiveDismissCount == 0 && getActiveSwipeCount() == 0) {
            restoreViewPresentations(mDismissedViews);
            notifyCallback(mDismissedPositions);

            mDismissedViews.clear();
            mDismissedPositions.clear();
        }
    }

    /**
     * Notifies the {@link com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback} of dismissed items.
     * @param dismissedPositions the positions that have been dismissed.
     */
    protected void notifyCallback(final List<Integer> dismissedPositions) {
        if (!dismissedPositions.isEmpty()) {
            Collections.sort(dismissedPositions, Collections.reverseOrder());

            int[] dismissPositions = new int[dismissedPositions.size()];
            int i = 0;
            for (Integer dismissedPosition : dismissedPositions) {
                dismissPositions[i] = dismissedPosition;
                i++;
            }
            mCallback.onDismiss(getAbsListView(), dismissPositions);
        }
    }

    /**
     * Restores the presentation of given {@link View}s by calling {@link #restoreViewPresentation(android.view.View)}.
     */
    protected void restoreViewPresentations(final Iterable<View> views) {
        for (View view : views) {
            restoreViewPresentation(view);
        }
    }

    @Override
    protected void restoreViewPresentation(final View view) {
        super.restoreViewPresentation(view);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 0;
        view.setLayoutParams(layoutParams);
    }

    protected int getActiveDismissCount() {
        return mActiveDismissCount;
    }

    public long getDismissAnimationTime() {
        return mDismissAnimationTime;
    }

    /**
     * An {@link com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener} which applies height animation to given {@link View}.
     */
    private static class DismissAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final View mView;

        DismissAnimatorUpdateListener(final View view) {
            mView = view;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
            layoutParams.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(layoutParams);
        }
    }

    private class DismissAnimatorListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(final Animator animation) {
            mActiveDismissCount--;
            finalizeDismiss();
        }
    }

    /**
     * A {@link Runnable} which applies the dismiss of a position, and restores the scroll position.
     */
    private class RestoreScrollRunnable implements Runnable {
        private final int mScrollDistance;
        private final int mPosition;

        /**
         * Creates a new {@code RestoreScrollRunnable}.
         * @param scrollDistance The scroll distance in pixels to restore.
         * @param position the position to dismiss
         */
        RestoreScrollRunnable(final int scrollDistance, final int position) {
            mScrollDistance = scrollDistance;
            mPosition = position;
        }

        @Override
        public void run() {
            getAbsListView().smoothScrollBy(-mScrollDistance, 1);
            directDismiss(mPosition);
        }
    }
}
