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
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
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
    @NonNull
    private final OnDismissCallback mCallback;

    /**
     * The duration of the dismiss animation
     */
    private final long mDismissAnimationTime;

    /**
     * The {@link android.view.View}s that have been dismissed.
     */
    @NonNull
    private final Collection<View> mDismissedViews = new LinkedList<>();

    /**
     * The dismissed positions.
     */
    @NonNull
    private final List<Integer> mDismissedPositions = new LinkedList<>();

    /**
     * The number of active dismiss animations.
     */
    private int mActiveDismissCount;

    /**
     * A handler for posting {@link Runnable}s.
     */
    @NonNull
    private final Handler mHandler = new Handler();

    /**
     * Constructs a new {@code SwipeDismissTouchListener} for the given {@link android.widget.AbsListView}.
     *
     * @param listViewWrapper The {@code ListViewWrapper} containing the ListView whose items should be dismissable.
     * @param callback    The callback to trigger when the user has indicated that he
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public SwipeDismissTouchListener(@NonNull final ListViewWrapper listViewWrapper, @NonNull final OnDismissCallback callback) {
        super(listViewWrapper);
        mCallback = callback;
        mDismissAnimationTime = listViewWrapper.getListView().getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    /**
     * Dismisses the {@link android.view.View} corresponding to given position.
     * Calling this method has the same effect as manually swiping an item off the screen.
     *
     * @param position the position of the item in the {@link android.widget.ListAdapter}. Must be visible.
     */
    public void dismiss(final int position) {
        fling(position);
    }

    @Override
    public void fling(final int position) {
        int firstVisiblePosition = getListViewWrapper().getFirstVisiblePosition();
        int lastVisiblePosition = getListViewWrapper().getLastVisiblePosition();

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
        View view = AdapterViewUtil.getViewForPosition(getListViewWrapper(), getListViewWrapper().getFirstVisiblePosition());

        if (view != null) {
            view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            int scrollDistance = view.getMeasuredHeight();

            getListViewWrapper().smoothScrollBy(scrollDistance, (int) mDismissAnimationTime);
            mHandler.postDelayed(new RestoreScrollRunnable(scrollDistance, position), mDismissAnimationTime);
        }
    }

    @Override
    protected void afterCancelSwipe(@NonNull final View view, final int position) {
        finalizeDismiss();
    }

    @Override
    protected boolean willLeaveDataSetOnFling(@NonNull final View view, final int position) {
        return true;
    }

    @Override
    protected void afterViewFling(@NonNull final View view, final int position) {
        performDismiss(view, position);
    }

    /**
     * Animates the dismissed list item to zero-height and fires the dismiss callback when all dismissed list item animations have completed.
     *
     * @param view the dismissed {@link android.view.View}.
     */
    protected void performDismiss(@NonNull final View view, final int position) {
        mDismissedViews.add(view);
        mDismissedPositions.add(position);

        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), 1).setDuration(mDismissAnimationTime);
        animator.addUpdateListener(new DismissAnimatorUpdateListener(view));
        animator.addListener(new DismissAnimatorListener());
        animator.start();

        mActiveDismissCount++;
    }

    /**
     * If necessary, notifies the {@link OnDismissCallback} to remove dismissed object from the adapter,
     * and restores the {@link android.view.View} presentations.
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
     * Notifies the {@link OnDismissCallback} of dismissed items.
     *
     * @param dismissedPositions the positions that have been dismissed.
     */
    protected void notifyCallback(@NonNull final List<Integer> dismissedPositions) {
        if (!dismissedPositions.isEmpty()) {
            Collections.sort(dismissedPositions, Collections.reverseOrder());

            int[] dismissPositions = new int[dismissedPositions.size()];
            int i = 0;
            for (Integer dismissedPosition : dismissedPositions) {
                dismissPositions[i] = dismissedPosition;
                i++;
            }
            mCallback.onDismiss(getListViewWrapper().getListView(), dismissPositions);
        }
    }

    /**
     * Restores the presentation of given {@link android.view.View}s by calling {@link #restoreViewPresentation(android.view.View)}.
     */
    protected void restoreViewPresentations(@NonNull final Iterable<View> views) {
        for (View view : views) {
            restoreViewPresentation(view);
        }
    }

    @Override
    protected void restoreViewPresentation(@NonNull final View view) {
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
     * An {@link com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener} which applies height animation to given {@link android.view.View}.
     */
    private static class DismissAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        @NonNull
        private final View mView;

        DismissAnimatorUpdateListener(@NonNull final View view) {
            mView = view;
        }

        @Override
        public void onAnimationUpdate(@NonNull final ValueAnimator animation) {
            ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
            layoutParams.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(layoutParams);
        }
    }

    private class DismissAnimatorListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(@NonNull final Animator animation) {
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
         *
         * @param scrollDistance The scroll distance in pixels to restore.
         * @param position       the position to dismiss
         */
        RestoreScrollRunnable(final int scrollDistance, final int position) {
            mScrollDistance = scrollDistance;
            mPosition = position;
        }

        @Override
        public void run() {
            getListViewWrapper().smoothScrollBy(-mScrollDistance, 1);
            directDismiss(mPosition);
        }
    }
}
