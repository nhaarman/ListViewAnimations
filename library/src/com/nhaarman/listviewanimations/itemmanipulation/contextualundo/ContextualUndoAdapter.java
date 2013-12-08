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
package com.nhaarman.listviewanimations.itemmanipulation.contextualundo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

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
        final ViewHolder vh;
		ContextualUndoView contextualUndoView = (ContextualUndoView) convertView;
		if (contextualUndoView == null) {
			contextualUndoView = new ContextualUndoView(parent.getContext(), mUndoLayoutId, mCountDownTextViewResId);
			contextualUndoView.findViewById(mUndoActionId).setOnClickListener(new UndoListener(contextualUndoView));
            vh = new ViewHolder(contextualUndoView);
		}
        else {
            vh = ViewHolder.getViewHolder(contextualUndoView);
        }

		View contentView = super.getView(position, contextualUndoView.getContentView(), contextualUndoView);
		contextualUndoView.updateContentView(contentView);

		long itemId = getItemId(position);
        vh.mItemId = itemId;

		if (itemId == mCurrentRemovedId) {
			contextualUndoView.displayUndo();
			long millisLeft = mAutoDeleteDelayMillis - (System.currentTimeMillis() - mDismissStartMillis);
			if (mCountDownFormatter != null) {
                contextualUndoView.updateCountDownTimer(mCountDownFormatter.getCountDownString(millisLeft));
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
        listView.setOnHierarchyChangeListener(new HierarchyChangeListener());
	}

	@Override
	public void onViewSwiped(long dismissViewItemId, int dismissPosition) {
        ContextualUndoView contextualUndoView = getContextualUndoView(dismissViewItemId);
        if (contextualUndoView == null) {
            removePreviousContextualUndoIfPresent();
            mCurrentRemovedView = null;
            mCurrentRemovedId = dismissViewItemId;
        }
		else if (contextualUndoView.isContentDisplayed()) {
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

    private ContextualUndoView getContextualUndoView(long dismissViewItemId) {
        ContextualUndoView contextualUndoView = null;

        AbsListView listView = getAbsListView();
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof ContextualUndoView) {
                ContextualUndoView listItem = (ContextualUndoView)child;
                if (listItem.getItemId() == dismissViewItemId) {
                    contextualUndoView = listItem;
                }
            }
        }
        return contextualUndoView;
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
        if (mCurrentRemovedId == -1) {
            return;
        }

        ContextualUndoView currentRemovedView = getCurrentRemovedView(mCurrentRemovedView, mCurrentRemovedId);
		if (currentRemovedView != null) {
			ValueAnimator animator = ValueAnimator.ofInt(currentRemovedView.getHeight(), 1).setDuration(ANIMATION_DURATION);

            RemoveViewAnimatorListenerAdapter listener = new RemoveViewAnimatorListenerAdapter(currentRemovedView, mCurrentRemovedId);
            RemoveViewAnimatorUpdateListener updateListener = new RemoveViewAnimatorUpdateListener(listener);

            animator.addListener(listener);
			animator.addUpdateListener(updateListener);
			animator.start();
		}
        else if (mDeleteItemCallback != null) {
            // The hard way.
            deleteItemGivenId(mCurrentRemovedId);
        }
        clearCurrentRemovedView();
	}

    private void deleteItemGivenId(long deleteItemId) {
        int position = -1;
        int numItems = getCount();
        for (int i = 0; i < numItems; i++) {
            long itemId = getItemId(i);
            if (itemId == deleteItemId) {
                position = i;
                break;
            }
        }

        if (position >= 0) {
            mDeleteItemCallback.deleteItem(position);
        }
    }

    private ContextualUndoView getCurrentRemovedView(ContextualUndoView currentRemovedView, long itemId) {
        if (    (currentRemovedView == null) ||
                (currentRemovedView.getParent() == null) ||
                (currentRemovedView.getItemId() != itemId) ||
                (getAbsListView().getPositionForView(currentRemovedView) < 0)) {
            currentRemovedView = getContextualUndoView(itemId);
        }
        return currentRemovedView;
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
				onViewSwiped(((ContextualUndoView)view).getItemId(), dismissPosition);
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
     * Removes any item that was swiped away.
     * @param animate If true, animates the removal (collapsing the item).
     *                If false, removes item immediately without animation.
     */
    public void removePendingItem(boolean animate) {
        if (animate) {
            removePreviousContextualUndoIfPresent();
        }
        else if ((mCurrentRemovedView != null) || (mCurrentRemovedId >= 0)) {
            new RemoveViewAnimatorListenerAdapter(mCurrentRemovedView, mCurrentRemovedId).onAnimationEnd(null);
            clearCurrentRemovedView();
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

		private ContextualUndoView mDismissView;
        private final long mDismissViewId;
		private final int mOriginalHeight;

		public RemoveViewAnimatorListenerAdapter(ContextualUndoView dismissView, long dismissViewId) {
			mDismissView = dismissView;
            mDismissViewId = dismissViewId;
			mOriginalHeight = dismissView.getHeight();
		}

        @Override
		public void onAnimationEnd(Animator animation) {
            mDismissView = getViewBeingAnimated(animation);
            if (mDismissView == null) {
                deleteItemGivenId(mDismissViewId);
                return;
            }

            restoreViewPosition(mDismissView);
            restoreViewDimension(mDismissView);
            deleteCurrentItem(mDismissView);
		}

		private void restoreViewDimension(View view) {
			ViewGroup.LayoutParams lp;
			lp = view.getLayoutParams();
			lp.height = mOriginalHeight;
			view.setLayoutParams(lp);
		}

		private void deleteCurrentItem(View view) {
            int position = getAbsListView().getPositionForView(view);

			if (getAbsListView() instanceof ListView) {
				position -= ((ListView) getAbsListView()).getHeaderViewsCount();
			}

			mDeleteItemCallback.deleteItem(position);
		}

        private ContextualUndoView getViewBeingAnimated(Animator animator) {
            ContextualUndoView newDismissView = getCurrentRemovedView(mDismissView, mDismissViewId);
            if (newDismissView != mDismissView) {
                restoreViewPosition(mDismissView);
                restoreViewDimension(mDismissView);

                mDismissView = newDismissView;
            }
            return mDismissView;
        }
	}

	private class RemoveViewAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        final RemoveViewAnimatorListenerAdapter mParentAdapter;
		private final ViewGroup.LayoutParams mLayoutParams;

		public RemoveViewAnimatorUpdateListener(RemoveViewAnimatorListenerAdapter parentAdapter) {
            mParentAdapter = parentAdapter;
			mLayoutParams = parentAdapter.mDismissView.getLayoutParams();
		}

		@Override
		public void onAnimationUpdate(ValueAnimator valueAnimator) {
            ContextualUndoView dismissView = mParentAdapter.getViewBeingAnimated(valueAnimator);
            if (dismissView != null) {
                mLayoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(mLayoutParams);
            }
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

    private class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        @Override
        public void onChildViewAdded(View parent, View child) {
            final ViewHolder vh = ViewHolder.getViewHolder(child);
            if ((vh != null) && (mCurrentRemovedId > 0) && (vh.mItemId == mCurrentRemovedId)) {
                mCurrentRemovedView = (ContextualUndoView)child;
            }
        }

        @Override
        public void onChildViewRemoved(View parent, View child) {
            final ViewHolder vh = ViewHolder.getViewHolder(child);
            if ((vh != null) && (mCurrentRemovedId > 0) && (vh.mItemId == mCurrentRemovedId)) {
                mCurrentRemovedView = null;
            }
        }
    }

    private static class ViewHolder {
        final ContextualUndoView mContextualUndoView;

        long mItemId;

        static ViewHolder getViewHolder(View view) {
            return (ViewHolder)view.getTag();
        }

        ViewHolder(ContextualUndoView contextualUndoView) {
            mContextualUndoView = contextualUndoView;
            mContextualUndoView.setTag(this);
        }
    }
}