/*
 * Copyright 2012 Roman Nurik
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * A {@link android.view.View.OnTouchListener} that makes the list items in a
 * {@link AbsListView} dismissable. {@link AbsListView} is given special treatment
 * because by default it handles touches for its list items. I.E. it's in
 * charge of drawing the pressed state (the list selector), handling list item
 * clicks, etc.
 *
 * For performance reasons, do not use this class directly, but use the
 * {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter} class.
 */
public class SwipeDismissTouchListener implements View.OnTouchListener {

    private static final int MIN_FLING_VELOCITY_FACTOR = 16;

    /**
     * The minimum distance in pixels that should be moved before starting horizontal item movement.
     */
    private final int mSlop;

    /**
     * The minimum velocity to initiate a fling, as measured in pixels per second.
     */
    private final int mMinFlingVelocity;

    /**
     * The maximum velocity to initiate a fling, as measured in pixels per second.
     */
    private final int mMaxFlingVelocity;

    /**
     * The duration of the fling animation.
     */
    private final long mAnimationTime;

    /**
     * The {@link android.widget.AbsListView} that is controlled.
     */
    private final AbsListView mListView;

    /**
     * The callback which gets notified of dismissed items.
     */
    private final OnDismissCallback mCallback;

    /**
     * The data of the items that have been dismissed, but not yet notified.
     */
    private final List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();

    /**
     * The width of the {@link android.widget.AbsListView} in pixels.
     */
    private int mViewWidth = 1;

    /**
     * The number of active dismiss animations.
     */
    private int mActiveDismissCount;

    /**
     * The raw X coordinate of the down event.
     */
    private float mDownX;

    /**
     * The raw Y coordinate of the down event.
     */
    private float mDownY;

    /**
     * Indicates whether the user is swiping an item.
     */
    private boolean mSwiping;

    /**
     * The {@code VelocityTracker} used in the swipe movement.
     */
    private VelocityTracker mVelocityTracker;

    /**
     * The data of the current item being swiped.
     */
    private PendingDismissData mCurrentDismissData;

    /**
     * The number of items in the {@code AbsListView}, minus the pending dismissed items.
     */
    private int mVirtualListCount = -1;

    /**
     * Indicates whether the {@link android.widget.AbsListView} is in a horizontal scroll container.
     * If so, this class will prevent the horizontal scroller from receiving any touch events.
     */
    private boolean mParentIsHorizontalScrollContainer;

    /**
     * The resource id of the {@link View} that may steal touch events from their parents. Useful for example
     * when the {@link AbsListView} is in a horizontal scroll container, but not the whole {@code AbsListView} should
     * steal the touch events.
     */
    private int mTouchChildResId;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} which decides
     * whether or not a list item can be swiped.
     */
    private DismissableManager mDismissableManager;

    /**
     * Constructs a new {@code SwipeDismissTouchListener} for the given {@link android.widget.AbsListView}.
     *
     * @param absListView
     *            The {@code AbsListView} whose items should be dismissable.
     * @param callback
     *            The callback to trigger when the user has indicated that he
     *            would like to dismiss one or more list items.
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public SwipeDismissTouchListener(final AbsListView absListView, final OnDismissCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(absListView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * MIN_FLING_VELOCITY_FACTOR;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = absListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mListView = absListView;
        mCallback = callback;
    }

    /**
     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} to specify which views can or cannot be swiped.
     * @param dismissableManager {@code null} for no restrictions.
     */
    public void setDismissableManager(final DismissableManager dismissableManager) {
        mDismissableManager = dismissableManager;
    }

    /**
     * If the {@link AbsListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch-events from reaching the {@code AbsListView}.
     *
     * Call this method to fix this behavior.
     * Note that this will prevent the parent from scrolling horizontally when the user touches anywhere in a list item.
     */
    public void setParentIsHorizontalScrollContainer() {
        mParentIsHorizontalScrollContainer = true;
        mTouchChildResId = 0;
    }

    /**
     * If the {@link AbsListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch events from reaching the {@code AbsListView}.
     *
     * If a {@code AbsListView} view has a child with the given resource id, the user can still swipe the list item by touching that child.
     * If the user touches an area outside that child (but inside the list item view), then the swipe will not happen and the parent
     * will do its job instead (scrolling horizontally).
     *
     * @param childResId The resource id of the list items' child that the user should touch to be able to swipe the list items.
     */
    public void setTouchChild(final int childResId) {
        mTouchChildResId = childResId;
        mParentIsHorizontalScrollContainer = false;
    }

    /**
     * Notifies this {@code SwipeDismissTouchListener} that the adapter contents have changed.
     */
    public void notifyDataSetChanged() {
        mVirtualListCount = mListView.getAdapter().getCount();
    }

    /**
     * Returns whether the user is currently swiping an item.
     * @return {@code true} if the user is swiping an item.
     */
    public boolean isSwiping() {
        return mSwiping;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        if (mVirtualListCount == -1) {
            mVirtualListCount = mListView.getAdapter().getCount();
        }

        if (mViewWidth < 2) {
            mViewWidth = mListView.getWidth();
        }

        boolean result;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                view.onTouchEvent(event);
                result = handleDownEvent(event);
                break;
            case MotionEvent.ACTION_MOVE:
                result = handleMoveEvent(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                result = handleCancelEvent();
                break;
            case MotionEvent.ACTION_UP:
                result = handleUpEvent(event);
                break;
            default:
                result = false;
        }
        return result;
    }

    private boolean handleDownEvent(final MotionEvent motionEvent) {
        View downView = findDownView(motionEvent);
        if (downView == null) {
            return false;
        }

        mDownX = motionEvent.getRawX();
        mDownY = motionEvent.getRawY();
        int downPosition = AdapterViewUtil.getPositionForView(mListView, downView);

        /* Check if the item at this position is dismissable */
        if (mDismissableManager != null) {
            long downId = mListView.getAdapter().getItemId(downPosition);
            if (!mDismissableManager.isDismissable(downId, downPosition)) {
                return false;
            }
        }

        /* Check if we are processing the item at this position */
        mCurrentDismissData = new PendingDismissData(downPosition, downView);
        if (mPendingDismisses.contains(mCurrentDismissData) || downPosition >= mVirtualListCount) {
            mCurrentDismissData = null;
            return false;
        }

        /* Avoid scrolling horizontally if necessary */
        if (mParentIsHorizontalScrollContainer) {
            mListView.requestDisallowInterceptTouchEvent(true);
        } else if (mTouchChildResId != 0) {
            mParentIsHorizontalScrollContainer = false;

            final View childView = downView.findViewById(mTouchChildResId);
            if (childView != null) {
                final Rect childRect = getChildViewRect(mListView, childView);
                if (childRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    mListView.requestDisallowInterceptTouchEvent(true);
                }
            }
        }

        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionEvent);
        return true;
    }

    /**
     * Returns the child {@link View} that was touched, by performing a hit test.
     * @param motionEvent the {@link MotionEvent} to find the {@code View} for.
     * @return the touched {@code View}, or {@code null} if none found.
     */
    private View findDownView(final MotionEvent motionEvent) {
        // Find the child view that was touched (perform a hit test)
        Rect rect = new Rect();
        int childCount = mListView.getChildCount();
        int[] listViewCoords = new int[2];
        mListView.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        View downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = mListView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                downView = child;
            }
        }
        return downView;
    }

    private boolean handleMoveEvent(final MotionEvent motionEvent) {
        if (mVelocityTracker == null) {
            return false;
        }

        mVelocityTracker.addMovement(motionEvent);

        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;

        if (Math.abs(deltaX) > mSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
            mSwiping = true;
            mListView.requestDisallowInterceptTouchEvent(true);

            // Cancel ListView's touch (un-highlighting the item)
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL | motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            mListView.onTouchEvent(cancelEvent);
        }

        if (mSwiping) {
            ViewHelper.setTranslationX(mCurrentDismissData.view, deltaX);
            ViewHelper.setAlpha(mCurrentDismissData.view, Math.max(0, Math.min(1, 1 - 2 * Math.abs(deltaX) / mViewWidth)));
            return true;
        }
        return false;
    }

    private boolean handleCancelEvent() {
        if (mVelocityTracker == null) {
            return false;
        }

        /* Animate the item back to its original position */
        if (mCurrentDismissData != null && mSwiping) {
            restoreCurrentViewTranslation();
        }

        reset();
        return false;
    }


    private boolean handleUpEvent(final MotionEvent motionEvent) {
        if (mVelocityTracker == null) {
            return false;
        }

        if (mSwiping) {
            float deltaX = motionEvent.getRawX() - mDownX;

            mVelocityTracker.addMovement(motionEvent);
            mVelocityTracker.computeCurrentVelocity(1000);

            float velocityX = Math.abs(mVelocityTracker.getXVelocity());
            float velocityY = Math.abs(mVelocityTracker.getYVelocity());

            boolean shouldDismiss = false;
            boolean dismissToRight = false;

            if (Math.abs(deltaX) > mViewWidth / 2) {
                shouldDismiss = true;
                dismissToRight = deltaX > 0;
            } else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
                shouldDismiss = true;
                dismissToRight = mVelocityTracker.getXVelocity() > 0;
            }


            if (shouldDismiss) {
                ++mActiveDismissCount;
                flingCurrentView(dismissToRight);
                mVirtualListCount--;
                mPendingDismisses.add(mCurrentDismissData);
            } else {
                restoreCurrentViewTranslation();
            }
        }

        reset();
        return false;
    }

    /**
     * Flings the pending {@link View} out of sight.
     * @param flingToRight {@code true} if the {@code View} should be flinged to the right, {@code false} if it should be flinged to the left.
     */
    private void flingCurrentView(final boolean flingToRight) {
        ViewPropertyAnimator animator = animate(mCurrentDismissData.view);
        animator.translationX(flingToRight ? mViewWidth : -mViewWidth);
        animator.alpha(0);
        animator.setDuration(mAnimationTime);
        animator.setListener(new FlingAnimatorListener(mCurrentDismissData));
    }

    /**
     * Animates the pending {@link View} back to its original position.
     */
    private void restoreCurrentViewTranslation() {
        ViewPropertyAnimator animator = animate(mCurrentDismissData.view);
        animator.translationX(0);
        animator.alpha(1);
        animator.setDuration(mAnimationTime);
    }

    /**
     * Resets the fields to the initial values, ready to start over.
     */
    private void reset() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mDownX = 0;
        mDownY = 0;
        mCurrentDismissData = null;
        mSwiping = false;
    }

    private static Rect getChildViewRect(final View parentView, final View childView) {
        Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (!parentView.equals(childView)) {
            View workingChildView = childView;
            ViewGroup parent;
            while (!(parent = (ViewGroup) workingChildView.getParent()).equals(parentView)) {
                childRect.offset(parent.getLeft(), parent.getTop());
                workingChildView = parent;
            }
        }
        return childRect;
    }

    @SuppressWarnings({"InstanceVariableNamingConvention", "ParameterHidesMemberVariable", "PublicField", "SubtractionInCompareTo"})
    private static class PendingDismissData implements Comparable<PendingDismissData> {

        public final int position;
        public final View view;

        PendingDismissData(final int position, final View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(final PendingDismissData another) {
            // Sort by descending position
            return another.position - position;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + position;
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj == null) {
                result = false;
            } else if (((Object) this).getClass() != obj.getClass()) {
                result = false;
            } else {
                PendingDismissData other = (PendingDismissData) obj;
                result = position == other.position;
            }
            return result;
        }
    }


    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} that performs the dismissal animation when the current animation has ended.
     */
    private class FlingAnimatorListener extends AnimatorListenerAdapter {

        private final PendingDismissData mPendingDismissData;

        FlingAnimatorListener(final PendingDismissData pendingDismissData) {
            mPendingDismissData = pendingDismissData;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            performDismiss(mPendingDismissData.view);
        }

        /**
         * Animates the dismissed list item to zero-height and fires the dismiss callback when all dismissed list item animations have completed.
         * @param view the dismissed {@link View}.
         */
        private void performDismiss(final View view) {
            ViewGroup.LayoutParams lp = view.getLayoutParams();
            int originalHeight = view.getHeight();

            ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);
            animator.addUpdateListener(new DismissAnimatorUpdateListener(lp, view));
            animator.addListener(new DismissAnimatorListener());
            animator.start();
        }
    }

    /**
     * An {@link com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener} which applies height animation to given {@link View}.
     */
    private static class DismissAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {
        private final ViewGroup.LayoutParams mLp;
        private final View mView;

        DismissAnimatorUpdateListener(final ViewGroup.LayoutParams lp, final View view) {
            mLp = lp;
            mView = view;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            mLp.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(mLp);
        }
    }

    private class DismissAnimatorListener extends AnimatorListenerAdapter {
        @Override
        public void onAnimationEnd(final Animator animation) {
            finalizeDismiss();
        }

        private void finalizeDismiss() {
            --mActiveDismissCount;
            if (mActiveDismissCount == 0) {
                // No active animations, process all pending dismisses.
                // Sort by descending position
                Collections.sort(mPendingDismisses);

                int[] dismissPositions = new int[mPendingDismisses.size()];
                for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
                    dismissPositions[i] = mPendingDismisses.get(i).position;
                }
                mCallback.onDismiss(mListView, dismissPositions);

                for (final PendingDismissData pendingDismiss : mPendingDismisses) {
                    // Reset view presentation
                    ViewHelper.setAlpha(pendingDismiss.view, 1);
                    ViewHelper.setTranslationX(pendingDismiss.view, 0);
                    ViewGroup.LayoutParams lp = pendingDismiss.view.getLayoutParams();
                    lp.height = 0;
                    pendingDismiss.view.setLayoutParams(lp);
                }

                mPendingDismisses.clear();
            }
        }
    }
}
