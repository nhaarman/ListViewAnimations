// THIS IS A BETA! I DON'T RECOMMEND USING IT IN PRODUCTION CODE JUST YET

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
package com.haarman.listviewanimations.itemmanipulation;

//import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * A {@link android.view.View.OnTouchListener} that makes the list items in a
 * {@link ListView} dismissable. {@link ListView} is given special treatment
 * because by default it handles touches for its list items... i.e. it's in
 * charge of drawing the pressed state (the list selector), handling list item
 * clicks, etc.
 *
 * For performance reasons, do not use this class directly, but use the {@link SwipeDismissAdapter}.
 */
@SuppressLint("Recycle")
public class SwipeDismissListViewTouchListener implements SwipeOnTouchListener {

	// Cached ViewConfiguration and system-wide constant values
	private int mSlop;
	private int mMinFlingVelocity;
	private int mMaxFlingVelocity;
	protected long mAnimationTime;

	// Fixed properties
	private AbsListView mListView;
	private OnDismissCallback mCallback;
	private int mViewWidth = 1; // 1 and not 0 to prevent dividing by zero

	// Transient properties
	protected List<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
	private int mDismissAnimationRefCount = 0;
	private float mDownX;
	private float mDownY;
	private boolean mSwiping;

	/**
	 * A state to help determine the first loop/time we are swiping
	 */
	private boolean mSwipeInitiated;
	private VelocityTracker mVelocityTracker;
	private boolean mPaused;
	private PendingDismissData mCurrentDismissData;

	private int mVirtualListCount = -1;

	private boolean mDisallowSwipe;
	private boolean mIsParentHorizontalScrollContainer;
	private int mResIdOfTouchChild;
	private boolean mTouchChildTouched;

	/**
	 * Constructs a new swipe-to-dismiss touch listener for the given list view.
	 *
	 * @param listView
	 *            The list view whose items should be dismissable.
	 * @param callback
	 *            The callback to trigger when the user has indicated that she
	 *            would like to dismiss one or more list items.
	 */
	public SwipeDismissListViewTouchListener(AbsListView listView, OnDismissCallback callback, SwipeOnScrollListener onScroll) {
		ViewConfiguration vc = ViewConfiguration.get(listView.getContext());
		mSlop = vc.getScaledTouchSlop();
		mMinFlingVelocity = vc.getScaledMinimumFlingVelocity() * 16;
		mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
		mAnimationTime = listView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
		mListView = listView;
		mCallback = callback;

		onScroll.setTouchListener(this);
		mListView.setOnScrollListener(onScroll);
	}

	public void disallowSwipe() {
		mDisallowSwipe = true;
	}

	public void allowSwipe() {
		mDisallowSwipe = false;
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		if (mVirtualListCount == -1) {
			mVirtualListCount = mListView.getAdapter().getCount();
		}

		if (mViewWidth < 2) {
			mViewWidth = mListView.getWidth();
		}

		switch (motionEvent.getActionMasked()) {
		case MotionEvent.ACTION_DOWN:

			mDisallowSwipe = false;
			view.onTouchEvent(motionEvent);
			return handleDownEvent(motionEvent);

		case MotionEvent.ACTION_MOVE:

			return handleMoveEvent(motionEvent);

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:

			mDisallowSwipe = false;
			mTouchChildTouched = false;
			return handleUpEvent(motionEvent);

		}
		return false;
	}

	@Override
	public boolean isSwiping() {
		return mSwiping;
	}

	/**
	 * Factory to allow override of dismiss data
	 */
	protected PendingDismissData createPendingDismissData(int position, View view) {
		return new PendingDismissData(position, view);
	}

	private boolean handleDownEvent(MotionEvent motionEvent) {
		if (mPaused) {
			return false;
		}

		mSwipeInitiated = false;

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

		if (downView != null) {
			Log.d("SwipeDismissListViewTouchListener", "hit child !");
			mDownX = motionEvent.getRawX();
			mDownY = motionEvent.getRawY();
			int downPosition = mListView.getPositionForView(downView);

			mCurrentDismissData = createPendingDismissData(downPosition, downView);

			if (mPendingDismisses.contains(mCurrentDismissData) || downPosition >= mVirtualListCount) {
				// Cancel, we're already processing this position
				mCurrentDismissData = null;
				return false;
			} else {
				mTouchChildTouched = !mIsParentHorizontalScrollContainer && (mResIdOfTouchChild == 0);

				if (mResIdOfTouchChild != 0) {
					mIsParentHorizontalScrollContainer = false;

					final View childView = downView.findViewById(mResIdOfTouchChild);
					if (childView != null) {
						final Rect childRect = getChildViewRect(mListView, childView);
						if (childRect.contains((int) mDownX, (int) mDownY)) {
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

				mVelocityTracker = VelocityTracker.obtain();
				mVelocityTracker.addMovement(motionEvent);
			}
		}
		return true;
	}

	private Rect getChildViewRect(View parentView, View childView) {
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

	private boolean handleMoveEvent(MotionEvent motionEvent) {
		if (mPaused || (mVelocityTracker == null)) {
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
			cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
			mListView.onTouchEvent(cancelEvent);
		}

		if (mSwiping) {
			if (!mSwipeInitiated) {
				Log.d("SwipeDismissListViewTouchListener", "swipe/begin");
			}
			mSwipeInitiated = true;
			ViewHelper.setTranslationX(mCurrentDismissData.view, deltaX);
			ViewHelper.setAlpha(mCurrentDismissData.view, Math.max(0f, Math.min(1f, 1f - 2f * Math.abs(deltaX) / mViewWidth)));
			return true;
		}
		return false;
	}

	private boolean handleUpEvent(MotionEvent motionEvent) {
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
		if (Math.abs(deltaX) > mViewWidth / 2) {
			dismiss = true;
			dismissRight = deltaX > 0;
		} else if (mMinFlingVelocity <= velocityX && velocityX <= mMaxFlingVelocity && velocityY < velocityX) {
			dismiss = true;
			dismissRight = mVelocityTracker.getXVelocity() > 0;
		}

		if (mSwiping) {
			if (dismiss) {
				Log.d("SwipeDismissListViewTouchListener", "swipe/confimed");
				// mDownView gets null'd before animation ends
				final PendingDismissData pendingDismissData = mCurrentDismissData;
				++mDismissAnimationRefCount;

				animate(mCurrentDismissData.view).translationX(dismissRight ? mViewWidth : -mViewWidth).alpha(0).setDuration(mAnimationTime).setListener(new AnimatorListenerAdapter() {

					@Override
					public void onAnimationEnd(Animator animation) {
						onDismiss(pendingDismissData);
					}
				});

				mVirtualListCount--;
				mPendingDismisses.add(mCurrentDismissData);
			} else {
				Log.d("SwipeDismissListViewTouchListener", "swipe/cancelled");
				// cancel
				animate(mCurrentDismissData.view).translationX(0).alpha(1).setDuration(mAnimationTime).setListener(null);
			}
		}

		mVelocityTracker.recycle();
		mVelocityTracker = null;
		mDownX = 0;
		mCurrentDismissData = null;
		mSwiping = false;
		return false;
	}

	protected class PendingDismissData implements Comparable<PendingDismissData> {
		public int position;
		public View view;

		public PendingDismissData(int position, View view) {
			this.position = position;
			this.view = view;
		}

		@Override
		public int compareTo(PendingDismissData other) {
			// Sort by descending position
			return other.position - position;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + position;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (((Object) this).getClass() != obj.getClass())
				return false;
			PendingDismissData other = (PendingDismissData) obj;
			if (position != other.position)
				return false;
			return true;
		}

	}

	protected void onDismiss(final PendingDismissData data) {
		// default behaviour
		performDismiss(data);
	}

	protected void performDismiss(final PendingDismissData data) {
		// Animate the dismissed list item to zero-height and fire the
		// dismiss callback when all dismissed list item animations have
		// completed.

		Log.d("SwipeDismissListViewTouchListener", "performDismiss");

		final ViewGroup.LayoutParams lp = data.view.getLayoutParams();
		final int originalHeight = data.view.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(mAnimationTime);

		animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				data.view.setLayoutParams(lp);
			}
		});

		animator.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				finalizeDismiss();
			}
		});
		animator.start();
	}

	/**
	 * Here you can manage dismissed View.
	 */
	protected void recycleDismissedViewsItems(List<PendingDismissData> pendingDismisses) {
		ViewGroup.LayoutParams lp;
		for (PendingDismissData pendingDismiss : pendingDismisses) {
			// Reset view presentation
			ViewHelper.setAlpha(pendingDismiss.view, 1f);
			ViewHelper.setTranslationX(pendingDismiss.view, 0);
			lp = pendingDismiss.view.getLayoutParams();
			lp.height = 0;
			pendingDismiss.view.setLayoutParams(lp);
		}
	}

	protected void finalizeDismiss() {

		--mDismissAnimationRefCount;
		if (mDismissAnimationRefCount == 0) {
			// No active animations, process all pending dismisses.
			// Sort by descending position
			Collections.sort(mPendingDismisses);

			int[] dismissPositions = new int[mPendingDismisses.size()];
			for (int i = mPendingDismisses.size() - 1; i >= 0; i--) {
				dismissPositions[i] = mPendingDismisses.get(i).position;
			}
			mCallback.onDismiss(mListView, dismissPositions);

			recycleDismissedViewsItems(mPendingDismisses);

			mPendingDismisses.clear();
		}
	}

	void setIsParentHorizontalScrollContainer(boolean isParentHorizontalScrollContainer) {
		mIsParentHorizontalScrollContainer = (mResIdOfTouchChild == 0) ? isParentHorizontalScrollContainer : false;
	}

	void setTouchChild(int childResId) {
		mResIdOfTouchChild = childResId;
		if (childResId != 0) {
			setIsParentHorizontalScrollContainer(false);
		}
	}

	public void notifyDataSetChanged() {
		mVirtualListCount = mListView.getAdapter().getCount();
	}
}
