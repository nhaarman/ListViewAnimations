/*
 * Copyright 2012 Roman Nurik
 * Copyright 2013 Frankie Sardo
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

package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeOnTouchListener;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * An {@link OnTouchListener} for the {@link ContextualUndoAdapter}. Don't use
 * this class directly, use ContextualUndoAdapter to wrap your
 * {@link BaseAdapter}s.
 */
public class ContextualUndoListViewTouchListener implements SwipeOnTouchListener {
    // Cached ViewConfiguration and system-wide constant values
    private final int mSlop;
    private final int mMinFlingVelocity;
    private final int mMaxFlingVelocity;
    private final long mAnimationTime;

    // Fixed properties
    private final AbsListView mListView;
    private final Callback mCallback;
    private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

    // Transient properties
    private float mDownX;
    private float mDownY;
    private boolean mSwiping;
    private VelocityTracker mVelocityTracker;
    private int mDownPosition;
    private View mDownView;
    private boolean mPaused;
    private boolean mDisallowSwipe;

    private boolean mIsParentHorizontalScrollContainer;
    private int mResIdOfTouchChild;
    private boolean mTouchChildTouched;

    private DismissableManager mDismissableManager;

    public interface Callback {

        void onViewSwiped(long dismissViewItemId, int dismissPosition);

        void onListScrolled();
    }

    public ContextualUndoListViewTouchListener(final AbsListView listView, final Callback callback) {
        ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mListView = listView;
        mCallback = callback;
    }

    public void setEnabled(final boolean enabled) {
        mPaused = !enabled;
    }

    /**
     * Set the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} to specify which views can or cannot be swiped.
     * @param dismissableManager null for no restrictions.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setDismissableManager(final DismissableManager dismissableManager) {
        mDismissableManager = dismissableManager;
    }

    public AbsListView.OnScrollListener makeScrollListener() {
        return new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(final AbsListView absListView, final int scrollState) {
                setEnabled(scrollState != AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                if (mPaused) {
                    mCallback.onListScrolled();
                }
                if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    mDisallowSwipe = true;
                }

            }

            @Override
            public void onScroll(final AbsListView absListView, final int firstVisibleItem, final int visibleItemCount,
                                 final int totalItemCount) {
            }
        };
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent motionEvent) {
        if (mViewWidth < 2) {
            mViewWidth = mListView.getWidth();
        }

        boolean result;
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                result = handleDownEvent(view, motionEvent);
                break;
            case MotionEvent.ACTION_MOVE:
                result = handleMoveEvent(view, motionEvent);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                result = handleUpCancelEvent(view, motionEvent);
                break;
            default:
                result = false;
        }
        return result;
    }

    private boolean handleDownEvent(final View view, final MotionEvent motionEvent) {
        mDisallowSwipe = false;
        if (mPaused) {
            return false;
        }

        // Find the child view that was touched (perform a hit test)
        Rect rect = new Rect();
        int childCount = mListView.getChildCount();
        int[] listViewCoords = new int[2];
        mListView.getLocationOnScreen(listViewCoords);
        int x = (int) motionEvent.getRawX() - listViewCoords[0];
        int y = (int) motionEvent.getRawY() - listViewCoords[1];
        View child;
        for (int i = 0; i < childCount; i++) {
            child = mListView.getChildAt(i);
            child.getHitRect(rect);
            if (rect.contains(x, y)) {
                mDownView = child;
                break;
            }
        }

        if (mDownView != null && mDownView instanceof ContextualUndoView) {
            mDownX = motionEvent.getRawX();
            mDownY = motionEvent.getRawY();
            int downPosition = AdapterViewUtil.getPositionForView(mListView, mDownView);

            if (mDismissableManager != null) {
                long downId = mListView.getAdapter().getItemId(downPosition);
                if (!mDismissableManager.isDismissable(downId, downPosition)) {
                    /* Cancel, not dismissable */
                    return false;
                }
            }

            mTouchChildTouched = !mIsParentHorizontalScrollContainer && mResIdOfTouchChild == 0;

            if (mResIdOfTouchChild != 0) {
                mIsParentHorizontalScrollContainer = false;

                final View childView = mDownView.findViewById(mResIdOfTouchChild);
                if (childView != null) {
                    final Rect childRect = getChildViewRect(mListView, childView);
                    if (childRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                        mTouchChildTouched = true;
                        mListView.requestDisallowInterceptTouchEvent(true);
                    }
                }
            }

            if (mIsParentHorizontalScrollContainer) {
                // Do it now and don't wait until the user moves more than
                // the slop factor.
                mTouchChildTouched = true;
                mListView.requestDisallowInterceptTouchEvent(true);
            }

            mDownY = motionEvent.getRawY();
            mDownPosition = AdapterViewUtil.getPositionForView(mListView, mDownView);

            if (mTouchChildTouched) {
                mVelocityTracker = VelocityTracker.obtain();
                mVelocityTracker.addMovement(motionEvent);
            } else {
                mVelocityTracker = null;
            }
        }
        view.onTouchEvent(motionEvent);
        return true;
    }

    @SuppressWarnings("UnusedParameters")
    private boolean handleMoveEvent(final View view, final MotionEvent motionEvent) {
        if (mVelocityTracker == null || mPaused) {
            return false;
        }

        mVelocityTracker.addMovement(motionEvent);
        float deltaX = motionEvent.getRawX() - mDownX;
        float deltaY = motionEvent.getRawY() - mDownY;
        if (mTouchChildTouched && !mDisallowSwipe && Math.abs(deltaX) > mSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
            mSwiping = true;
            mListView.requestDisallowInterceptTouchEvent(true);

            // Cancel ListView's touch (un-highlighting the item)
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL | motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
            mListView.onTouchEvent(cancelEvent);
            cancelEvent.recycle();
        }

        if (mSwiping) {
            setTranslationX(mDownView, deltaX);
            //noinspection MagicNumber
            setAlpha(mDownView, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
            return true;
        }
        return false;
    }

    @SuppressWarnings("UnusedParameters")
    private boolean handleUpCancelEvent(final View view, final MotionEvent motionEvent) {
        mDisallowSwipe = false;
        if (mVelocityTracker == null) {
            return false;
        }

        float deltaX = motionEvent.getRawX() - mDownX;
        mVelocityTracker.addMovement(motionEvent);
        mVelocityTracker.computeCurrentVelocity(1000);
        float velocityX = Math.abs(mVelocityTracker.getXVelocity());
        float velocityY = Math.abs(mVelocityTracker.getYVelocity());
        boolean dismiss = false;
        boolean dismissRight = false;
        final float absDeltaX = Math.abs(deltaX);
        if (absDeltaX > mViewWidth / 2) {
            dismiss = true;
            dismissRight = deltaX > 0;
        } else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX && absDeltaX > mSlop) {
            dismiss = true;
            dismissRight = mVelocityTracker.getXVelocity() > 0;
        }
        if (dismiss) {
            // dismiss
            final long itemId = ((ContextualUndoView) mDownView).getItemId();
            // before animation ends
            final int downPosition = mDownPosition;
            animate(mDownView).translationX(dismissRight ? mViewWidth : -mViewWidth).alpha(0).setDuration(mAnimationTime).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    mCallback.onViewSwiped(itemId, downPosition);
                }
            });
        } else {
            // cancel
            animate(mDownView).translationX(0).alpha(1).setDuration(mAnimationTime).setListener(null);
        }

        mVelocityTracker.recycle();
        mVelocityTracker = null;
        mDownX = 0;
        mDownView = null;
        mDownPosition = AdapterView.INVALID_POSITION;
        mSwiping = false;
        return false;
    }

    @Override
    public boolean isSwiping() {
        return mSwiping;
    }

    private Rect getChildViewRect(final View parentView, View childView) {
        final Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (parentView == childView) {
            return childRect;

        }

        ViewGroup parent;
        while ((parent = (ViewGroup) childView.getParent()) != parentView) {
            childRect.offset(parent.getLeft(), parent.getTop());
            childView = parent;
        }

        return childRect;
    }

    void setIsParentHorizontalScrollContainer(final boolean isParentHorizontalScrollContainer) {
        mIsParentHorizontalScrollContainer = mResIdOfTouchChild == 0 && isParentHorizontalScrollContainer;
    }

    void setTouchChild(final int childResId) {
        mResIdOfTouchChild = childResId;
        if (childResId != 0) {
            setIsParentHorizontalScrollContainer(false);
        }
    }
}
