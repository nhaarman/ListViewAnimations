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
import android.widget.AdapterView;

import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * An {@link android.view.View.OnTouchListener} that makes the list items in a {@link AbsListView} swipeable.
 * Implementations of this class should implement {@link #afterViewFling(android.view.View, int)} to specify what to do after an item has been swiped.
 */
public abstract class SwipeTouchListener implements View.OnTouchListener {

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
    private final AbsListView mAbsListView;

    /**
     * The width of the {@link android.widget.AbsListView} in pixels.
     */
    private int mViewWidth = 1;

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
     * The parent {@link View} being swiped.
     */
    private View mCurrentView;

    /**
     * The {@link View} that is actually being swiped.
     */
    private View mSwipingView;

    /**
     * The current position being swiped.
     */
    private int mCurrentPosition = AdapterView.INVALID_POSITION;

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
     * The number of active swipe animations.
     */
    private int mActiveSwipeCount;

    /**
     * Indicates whether swipe is enabled.
     */
    private boolean mSwipeEnabled = true;

    /**
     * Constructs a new {@code SwipeTouchListener} for the given {@link android.widget.AbsListView}.
     *
     * @param absListView
     *            The {@code AbsListView} whose items should be dismissable.
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public SwipeTouchListener(final AbsListView absListView) {
        ViewConfiguration vc = ViewConfiguration.get(absListView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * MIN_FLING_VELOCITY_FACTOR;
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = absListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mAbsListView = absListView;
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
     * Notifies this {@code SwipeTouchListener} that the adapter contents have changed.
     */
    public void notifyDataSetChanged() {
        mVirtualListCount = mAbsListView.getAdapter().getCount();
    }

    /**
     * Returns whether the user is currently swiping an item.
     * @return {@code true} if the user is swiping an item.
     */
    public boolean isSwiping() {
        return mSwiping;
    }

    /**
     * Returns the {@link android.widget.AbsListView} this class is controlling.
     */
    public AbsListView getAbsListView() {
        return mAbsListView;
    }

    /**
     * Enables the swipe behavior.
     */
    public void enableSwipe() {
        mSwipeEnabled = true;
    }

    /**
     * Disables the swipe behavior.
     */
    public void disableSwipe() {
        mSwipeEnabled = false;
    }

    /**
     * Flings the {@link View} corresponding to given position out of sight.
     * Calling this method has the same effect as manually swiping an item off the screen.
     * @param position the position of the item in the {@link android.widget.ListAdapter}.
     */
    public void fling(final int position) {
        View downView = AdapterViewUtil.getViewForPosition(mAbsListView, position);
        flingView(downView, position, true);

        mActiveSwipeCount++;
        mVirtualListCount--;
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        if (mVirtualListCount == -1) {
            mVirtualListCount = mAbsListView.getAdapter().getCount();
        }

        if (mViewWidth < 2) {
            mViewWidth = mAbsListView.getWidth();
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
        if (!mSwipeEnabled) {
            return false;
        }

        View downView = findDownView(motionEvent);
        if (downView == null) {
            return false;
        }

        int downPosition = AdapterViewUtil.getPositionForView(mAbsListView, downView);
        if (!isDismissable(downPosition)) {
            return false;
        }

        /* Check if we are processing the item at this position */
        if (mCurrentPosition == downPosition || downPosition >= mVirtualListCount) {
            return false;
        }

        disableHorizontalScrollContainerIfNecessary(motionEvent, downView);

        mDownX = motionEvent.getRawX();
        mDownY = motionEvent.getRawY();

        mCurrentView = downView;
        mSwipingView = getSwipeView(downView);
        mCurrentPosition = downPosition;

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
        Rect rect = new Rect();
        int childCount = mAbsListView.getChildCount();
        int[] listViewCoords = new int[2];
        mAbsListView.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        View downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = mAbsListView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                downView = child;
            }
        }
        return downView;
    }

    /**
     * Finds out whether the item represented by given position is dismissable.
     * @param position the position of the item.
     * @return {@code true} if the item is dismissable, false otherwise.
     */
    private boolean isDismissable(final int position) {
        if (mDismissableManager != null) {
            long downId = mAbsListView.getAdapter().getItemId(position);
            if (!mDismissableManager.isDismissable(downId, position)) {
                return false;
            }
        }
        return true;
    }

    private void disableHorizontalScrollContainerIfNecessary(final MotionEvent motionEvent, final View view) {
        if (mParentIsHorizontalScrollContainer) {
            mAbsListView.requestDisallowInterceptTouchEvent(true);
        } else if (mTouchChildResId != 0) {
            mParentIsHorizontalScrollContainer = false;

            final View childView = view.findViewById(mTouchChildResId);
            if (childView != null) {
                final Rect childRect = getChildViewRect(mAbsListView, childView);
                if (childRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    mAbsListView.requestDisallowInterceptTouchEvent(true);
                }
            }
        }
    }

    private boolean handleMoveEvent(final MotionEvent motionEvent) {
        if (mVelocityTracker == null) {
            return false;
        }

        mVelocityTracker.addMovement(motionEvent);

        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;

        if (Math.abs(deltaX) > mSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
            if (!mSwiping) {
                mActiveSwipeCount++;
                onStartSwipe(mCurrentView, mCurrentPosition);
            }
            mSwiping = true;
            mAbsListView.requestDisallowInterceptTouchEvent(true);

            /* Cancel ListView's touch (un-highlighting the item) */
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL | motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            mAbsListView.onTouchEvent(cancelEvent);
        }

        if (mSwiping) {
            ViewHelper.setTranslationX(mSwipingView, deltaX);
            ViewHelper.setAlpha(mSwipingView, Math.max(0, Math.min(1, 1 - 2 * Math.abs(deltaX) / mViewWidth)));
            return true;
        }
        return false;
    }

    private boolean handleCancelEvent() {
        if (mVelocityTracker == null) {
            return false;
        }

        if (mCurrentPosition != AdapterView.INVALID_POSITION && mSwiping) {
            onCancelSwipe(mCurrentView, mCurrentPosition);
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
                beforeViewFling(mCurrentView, mCurrentPosition);
                flingCurrentView(dismissToRight);
                mVirtualListCount--;
            } else {
                onCancelSwipe(mCurrentView, mCurrentPosition);
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
        flingView(mCurrentView, mCurrentPosition, flingToRight);
    }

    /**
     * Flings given {@link View} out of sight.
     * @param view the parent {@link View}.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     * @param flingToRight {@code true} {@code true} if the {@code View} should be flinged to the right, {@code false} if it should be flinged to the left.
     */
    private void flingView(final View view, final int position, final boolean flingToRight) {
        View swipeView = getSwipeView(view);
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(swipeView, "translationX", flingToRight ? mViewWidth : -mViewWidth);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(swipeView, "alpha", 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(xAnimator, alphaAnimator);
        animatorSet.setDuration(mAnimationTime);
        animatorSet.addListener(new FlingAnimatorListener(view, position));
        animatorSet.start();
    }

    /**
     * Animates the pending {@link View} back to its original position.
     */
    private void restoreCurrentViewTranslation() {
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(mSwipingView, "translationX", 0);
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mSwipingView, "alpha", 1);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(xAnimator, alphaAnimator);
        animatorSet.setDuration(mAnimationTime);
        animatorSet.addListener(new RestoreAnimatorListener(mCurrentView, mCurrentPosition));
        animatorSet.start();
    }

    /**
     * Resets the fields to the initial values, ready to start over.
     */
    private void reset() {
        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mDownX = 0;
        mDownY = 0;
        mCurrentView = null;
        mSwipingView = null;
        mCurrentPosition = AdapterView.INVALID_POSITION;
        mSwiping = false;
    }

    /**
     * Called when the user starts swiping a {@link View}.
     * @param view the {@code View} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void onStartSwipe(final View view, final int position) {
    }

    /**
     * Called when the swipe movement is canceled. A restore animation starts at this point.
     * @param view the {@code View} that was swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void onCancelSwipe(final View view, final int position) {
    }

    /**
     * Called after the restore animation of a canceled swipe movement ends.
     * @param view the {@code View} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void afterCancelSwipe(final View view, final int position) {
    }

    /**
     * Called when the user lifted their finger off the screen, and the {@link View} should be swiped away. A fling animation starts at this point.
     * @param view the {@code View} that is being flinged.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void beforeViewFling(final View view, final int position) {
    }

    /**
     * Called after the fling animation of a succesful swipe ends.
     * Users of this class should implement any finalizing behavior at this point, such as notifying the adapter.
     * @param view the {@code View} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected abstract void afterViewFling(View view, int position);

    /**
     * Restores the {@link View}'s {@code alpha} and {@code translationX} values.
     * Users of this class should call this method when recycling {@code View}s.
     * @param view the {@code View} whose presentation should be restored.
     */
    protected void restoreViewPresentation(final View view) {
        View swipedView = getSwipeView(view);
        ViewHelper.setAlpha(swipedView, 1);
        ViewHelper.setTranslationX(swipedView, 0);
    }

    /**
     * Returns the number of active swipe animations.
     */
    protected int getActiveSwipeCount() {
        return mActiveSwipeCount;
    }

    /**
     * Returns the {@link View} that should be swiped away. Must be a child of given {@code View}, or the {@code View} itself.
     * @param view the parent {@link View}.
     */
    protected View getSwipeView(final View view) {
        return view;
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

    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} that notifies when the fling animation has ended.
     */
    private class FlingAnimatorListener extends AnimatorListenerAdapter {

        private final View mView;
        private final int mPosition;

        private FlingAnimatorListener(final View view, final int position) {
            mView = view;
            mPosition = position;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mActiveSwipeCount--;
            afterViewFling(mView, mPosition);
        }
    }

    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} that performs the dismissal animation when the current animation has ended.
     */
    private class RestoreAnimatorListener extends AnimatorListenerAdapter {

        private final View mView;
        private final int mPosition;

        private RestoreAnimatorListener(final View view, final int position) {
            mView = view;
            mPosition = position;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mActiveSwipeCount--;
            afterCancelSwipe(mView, mPosition);
        }
    }
}
