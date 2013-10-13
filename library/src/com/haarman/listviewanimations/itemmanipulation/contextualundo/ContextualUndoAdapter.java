/*
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
package com.haarman.listviewanimations.itemmanipulation.contextualundo;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Warning: a stable id for each item in the adapter is required. The decorated
 * adapter should not try to cast convertView to a particular view. The
 * undoLayout should have the same height as the content row.
 * <p>
 * Usage: <br>
 * * Create a new instance of this class providing the {@link BaseAdapter} to wrap, the undo layout, and the undo button id, optionally a delay time millis, a count down TextView res id, and a delay in milliseconds before deleting the item .<br>
 * * Call {@link #setDeleteItemCallback(DeleteItemCallback)} to be notified of when items should be removed from your collection.<br>
 * * Set your {@link ListView} to this ContextualUndoAdapter, and set this ContextualUndoAdapter to your ListView.<br>
 */
public class ContextualUndoAdapter extends BaseAdapterDecorator implements ContextualUndoListViewTouchListener.Callback {

	private static final int ANIMATION_DURATION = 150;
	private static final String EXTRA_ACTIVE_REMOVED_ID = "removedId";

	private final int mUndoLayoutId;
	private final int mUndoActionId;
	private final int mCountDownTextViewResId;
	private final int mAutoDeleteDelayMillis;

	private long mDismissStartMillis;

	private ContextualUndoView mCurrentRemovedView;
	private long mCurrentRemovedId;

	private Map<View, Animator> mActiveAnimators = new ConcurrentHashMap<View, Animator>();

	private Handler mHandler;

	private CountDownRunnable mCountDownRunnable;

	private DeleteItemCallback mDeleteItemCallback;
	private CountDownFormatter mCountDownFormatter;

	private ContextualUndoListViewTouchListener mContextualUndoListViewTouchListener;

	/**
	 * Create a new ContextualUndoAdapter based on given parameters.
	 *
	 * @param baseAdapter  The {@link BaseAdapter} to wrap
	 * @param undoLayoutId The layout resource id to show as undo
	 * @param undoActionId The id of the component which undoes the dismissal
	 *            The layout resource id to show as undo
	 * @param undoActionId
	 *            The id of the component which undoes the dismissal
	 */
	public ContextualUndoAdapter(BaseAdapter baseAdapter, int undoLayoutId, int undoActionId) {
		this(baseAdapter, undoLayoutId, undoActionId, -1, -1, null);
	}

	/**
	 * Create a new ContextualUndoAdapter based on given parameters.
	 * Will automatically remove the swiped item after autoDeleteTimeMillis milliseconds.
	 *
	 * @param baseAdapter  The {@link BaseAdapter} to wrap
	 * @param undoLayoutResId The layout resource id to show as undo
	 * @param undoActionResId The id of the component which undoes the dismissal
	 * @param autoDeleteTimeMillis The time in milliseconds that the adapter will wait for he user to hit undo before automatically deleting the item
	 */
	public ContextualUndoAdapter(BaseAdapter baseAdapter, int undoLayoutResId, int undoActionResId, int autoDeleteTimeMillis) {
		this(baseAdapter, undoLayoutResId, undoActionResId, autoDeleteTimeMillis, -1, null);
	}

	/**
	 * Create a new ContextualUndoAdapter based on given parameters.
	 * Will automatically remove the swiped item after autoDeleteTimeMillis milliseconds.
	 *
	 * @param baseAdapter  The {@link BaseAdapter} to wrap
	 * @param undoLayoutResId The layout resource id to show as undo
	 * @param undoActionResId The resource id of the component which undoes the dismissal
	 * @param autoDeleteTime The time in milliseconds that adapter will wait for user to hit undo before automatically deleting item
	 * @param countDownTextViewResId The resource id of the {@link TextView} in the undoLayoutResId that will show the time left
	 * @param countDownFormatter the {@link CountDownFormatter} which provides text to be shown in the {@link TextView} as specified by countDownTextViewResId
	 */
	public ContextualUndoAdapter(BaseAdapter baseAdapter, int undoLayoutResId, int undoActionResId, int autoDeleteTime, int countDownTextViewResId, CountDownFormatter countDownFormatter) {
		super(baseAdapter);

		mHandler = new Handler();
		mCountDownRunnable = new CountDownRunnable();

		mUndoLayoutId = undoLayoutResId;
		mUndoActionId = undoActionResId;
		mCurrentRemovedId = -1;
		mAutoDeleteDelayMillis = autoDeleteTime;
		mCountDownTextViewResId = countDownTextViewResId;
		mCountDownFormatter = countDownFormatter;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		ContextualUndoView contextualUndoView = (ContextualUndoView) convertView;
		if (contextualUndoView == null) {
			contextualUndoView = new ContextualUndoView(parent.getContext(), mUndoLayoutId, mCountDownTextViewResId);
			contextualUndoView.findViewById(mUndoActionId).setOnClickListener(new UndoListener(contextualUndoView));
		}

		View contentView = super.getView(position, contextualUndoView.getContentView(), contextualUndoView);
		contextualUndoView.updateContentView(contentView);

		long itemId = getItemId(position);

		if (itemId == mCurrentRemovedId) {
			contextualUndoView.displayUndo();
			mCurrentRemovedView = contextualUndoView;
			long millisLeft = mAutoDeleteDelayMillis - (System.currentTimeMillis() - mDismissStartMillis);
			if (mCountDownFormatter != null) {
				mCurrentRemovedView.updateCountDownTimer(mCountDownFormatter.getCountDownString(millisLeft));
			}
		} else {
			contextualUndoView.displayContentView();
		}

		contextualUndoView.setItemId(itemId);
		return contextualUndoView;
	}

	@Override
	public void setAbsListView(AbsListView listView) {
		super.setAbsListView(listView);
		mContextualUndoListViewTouchListener = new ContextualUndoListViewTouchListener(listView, this);
		mContextualUndoListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer());
		mContextualUndoListViewTouchListener.setTouchChild(getTouchChild());
		listView.setOnTouchListener(mContextualUndoListViewTouchListener);
		listView.setOnScrollListener(mContextualUndoListViewTouchListener.makeScrollListener());
		listView.setRecyclerListener(new RecycleViewListener());
	}

	@Override
	public void onViewSwiped(View dismissView, int dismissPosition) {
		ContextualUndoView contextualUndoView = (ContextualUndoView) dismissView;
		if (contextualUndoView.isContentDisplayed()) {
			restoreViewPosition(contextualUndoView);
			contextualUndoView.displayUndo();
			removePreviousContextualUndoIfPresent();
			setCurrentRemovedView(contextualUndoView);

			if (mAutoDeleteDelayMillis > 0) {
				startAutoDeleteTimer();
			}
		} else {
			performRemovalIfNecessary();
		}
	}

	private void startAutoDeleteTimer() {
		mHandler.removeCallbacks(mCountDownRunnable);

		if (mCountDownFormatter != null) {
			mCurrentRemovedView.updateCountDownTimer(mCountDownFormatter.getCountDownString(mAutoDeleteDelayMillis));
		}

		mDismissStartMillis = System.currentTimeMillis();
		mHandler.postDelayed(mCountDownRunnable, Math.min(1000, mAutoDeleteDelayMillis));
	}

	private void restoreViewPosition(View view) {
		setAlpha(view, 1f);
		setTranslationX(view, 0);
	}

	private void removePreviousContextualUndoIfPresent() {
		if (mCurrentRemovedView != null) {
			performRemovalIfNecessary();
		}
	}

	private void setCurrentRemovedView(ContextualUndoView currentRemovedView) {
		mCurrentRemovedView = currentRemovedView;
		mCurrentRemovedId = currentRemovedView.getItemId();
	}

	private void clearCurrentRemovedView() {
		mCurrentRemovedView = null;
		mCurrentRemovedId = -1;
		mHandler.removeCallbacks(mCountDownRunnable);
	}

	@Override
	public void onListScrolled() {
		performRemovalIfNecessary();
	}

	private void performRemovalIfNecessary() {
		if (mCurrentRemovedView != null && mCurrentRemovedView.getParent() != null) {
			ValueAnimator animator = ValueAnimator.ofInt(mCurrentRemovedView.getHeight(), 1).setDuration(ANIMATION_DURATION);
			animator.addListener(new RemoveViewAnimatorListenerAdapter(mCurrentRemovedView));
			animator.addUpdateListener(new RemoveViewAnimatorUpdateListener(mCurrentRemovedView));
			animator.start();
			mActiveAnimators.put(mCurrentRemovedView, animator);
			clearCurrentRemovedView();
		}
	}

	/**
	 * Set the DeleteItemCallback for this ContextualUndoAdapter. This is called when an item should be deleted from your collection.
	 */
	public void setDeleteItemCallback(DeleteItemCallback deleteItemCallback) {
		mDeleteItemCallback = deleteItemCallback;
	}

	/**
	 * This method should be called in your {@link Activity}'s {@link Activity#onSaveInstanceState(Bundle)} to remember dismissed statuses.
	 * @param outState the {@link Bundle} provided by Activity.onSaveInstanceState(Bundle).
	 */
	public void onSaveInstanceState(Bundle outState) {
		outState.putLong(EXTRA_ACTIVE_REMOVED_ID, mCurrentRemovedId);
	}

	/**
	 * This method should be called in your {@link Activity#onRestoreInstanceState(Bundle)} to remember dismissed statuses.
	 * @param savedInstanceState
	 */
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		mCurrentRemovedId = savedInstanceState.getLong(EXTRA_ACTIVE_REMOVED_ID, -1);
	}

	/**
	 * Animate the item at given position away and show the undo {@link View}.
	 * @param position the position.
	 */
	public void swipeViewAtPosition(int position) {
		mCurrentRemovedId = getItemId(position);
		for (int i = 0; i < getAbsListView().getChildCount(); i++) {
			int positionForView = getAbsListView().getPositionForView(getAbsListView().getChildAt(i));
			if (positionForView == position) {
				swipeView(getAbsListView().getChildAt(i), positionForView);
			}
		}
	}

	private void swipeView(final View view, final int dismissPosition) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "x", view.getMeasuredWidth());
		animator.addListener(new AnimatorListenerAdapter() {

			@Override
			public void onAnimationEnd(Animator animator) {
				onViewSwiped(view, dismissPosition);
			}
		});
		animator.start();
	}

	@Override
	public void setIsParentHorizontalScrollContainer(boolean isParentHorizontalScrollContainer) {
		super.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
		if (mContextualUndoListViewTouchListener != null) {
			mContextualUndoListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
		}
	}

	@Override
	public void setTouchChild(int childResId) {
		super.setTouchChild(childResId);
		if (mContextualUndoListViewTouchListener != null) {
			mContextualUndoListViewTouchListener.setTouchChild(childResId);
		}
	}

	/**
	 * A callback interface which is used to notify when items should be removed from the collection.
	 */
	public interface DeleteItemCallback {
		/**
		 * Called when an item should be removed from the collection.
		 *
		 * @param position
		 *            the position of the item that should be removed.
		 */
		public void deleteItem(int position);
	}

	/**
	 * A callback interface which is used to provide the text to display when counting down.
	 */
	public interface CountDownFormatter {
		/**
		 * Called each tick of the CountDownTimer
		 * @param millisLeft time in milliseconds remaining before the item is automatically removed
		 */
		public String getCountDownString(final long millisLeft);
	}

	private class CountDownRunnable implements Runnable {

		@Override
		public void run() {
			long millisRemaining = mAutoDeleteDelayMillis - (System.currentTimeMillis() - mDismissStartMillis);
			if (mCountDownFormatter != null) {
				mCurrentRemovedView.updateCountDownTimer(mCountDownFormatter.getCountDownString(millisRemaining));
			}

			if (millisRemaining <= 0) {
				performRemovalIfNecessary();
			} else {
				mHandler.postDelayed(this, Math.min(millisRemaining, 1000));
			}
		}
	}

	private class RemoveViewAnimatorListenerAdapter extends AnimatorListenerAdapter {

		private final View mDismissView;
		private final int mOriginalHeight;

		public RemoveViewAnimatorListenerAdapter(View dismissView) {
			mDismissView = dismissView;
			mOriginalHeight = dismissView.getHeight();
		}

		@Override
		public void onAnimationEnd(Animator animation) {
			mActiveAnimators.remove(mDismissView);
			restoreViewPosition(mDismissView);
			restoreViewDimension(mDismissView);
			deleteCurrentItem();
		}

		private void restoreViewDimension(View view) {
			ViewGroup.LayoutParams lp;
			lp = view.getLayoutParams();
			lp.height = mOriginalHeight;
			view.setLayoutParams(lp);
		}

		private void deleteCurrentItem() {
			int position = getAbsListView().getPositionForView(mDismissView);
			mDeleteItemCallback.deleteItem(position);
		}
	}

	private class RemoveViewAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

		private final View mDismissView;
		private final ViewGroup.LayoutParams mLayoutParams;

		public RemoveViewAnimatorUpdateListener(View dismissView) {
			mDismissView = dismissView;
			mLayoutParams = dismissView.getLayoutParams();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
			mLayoutParams.height = (Integer) valueAnimator.getAnimatedValue();
			mDismissView.setLayoutParams(mLayoutParams);
		}
	}

	private class UndoListener implements View.OnClickListener {

		private final ContextualUndoView mContextualUndoView;

		public UndoListener(ContextualUndoView contextualUndoView) {
			mContextualUndoView = contextualUndoView;
		}

		@Override
		public void onClick(View v) {
			clearCurrentRemovedView();
			mContextualUndoView.displayContentView();
			moveViewOffScreen();
			animateViewComingBack();
		}

		private void moveViewOffScreen() {
			ViewHelper.setTranslationX(mContextualUndoView, mContextualUndoView.getWidth());
		}

		private void animateViewComingBack() {
			animate(mContextualUndoView).translationX(0).setDuration(ANIMATION_DURATION).setListener(null);
		}
	}

	private class RecycleViewListener implements AbsListView.RecyclerListener {
		@Override
		public void onMovedToScrapHeap(View view) {
			Animator animator = mActiveAnimators.get(view);
			if (animator != null) {
				animator.cancel();
			}
		}
	}
}