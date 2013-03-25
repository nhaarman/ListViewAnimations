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

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * Warning: a stable id for each item in the adapter is required. The decorated
 * adapter should not try to cast convertView to a particular view. The
 * undoLayout should have the same height as the content row.
 */
public class ContextualUndoAdapter extends BaseAdapterDecorator implements ContextualUndoListViewTouchListener.Callback {

	private final int mUndoLayoutId;
	private final int mUndoActionId;
	private final int mAnimationTime = 150;
	private ContextualUndoView mCurrentRemovedView;
	private long mCurrentRemovedId;
	private Map<View, Animator> mActiveAnimators = new ConcurrentHashMap<View, Animator>();
	private DeleteItemCallback mDeleteItemCallback;

	public ContextualUndoAdapter(BaseAdapter baseAdapter, int undoLayoutId, int undoActionId) {
		super(baseAdapter);

		mUndoLayoutId = undoLayoutId;
		mUndoActionId = undoActionId;
		mCurrentRemovedId = -1;
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		ContextualUndoView contextualUndoView = (ContextualUndoView) convertView;
		if (contextualUndoView == null) {
			contextualUndoView = new ContextualUndoView(parent.getContext(), mUndoLayoutId);
			contextualUndoView.findViewById(mUndoActionId).setOnClickListener(new UndoListener(contextualUndoView));
			convertView = contextualUndoView;
		}

		View contentView = super.getView(position, contextualUndoView.getContentView(), parent);
		contextualUndoView.updateContentView(contentView);

		long itemId = getItemId(position);

		if (itemId == mCurrentRemovedId) {
			contextualUndoView.displayUndo();
			mCurrentRemovedView = contextualUndoView;
		} else {
			contextualUndoView.displayContentView();
		}

		contextualUndoView.setItemId(itemId);
		return contextualUndoView;
	}

	@Override
	public void setListView(ListView listView) {
		super.setListView(listView);
		ContextualUndoListViewTouchListener contextualUndoListViewTouchListener = new ContextualUndoListViewTouchListener(listView, this);
		listView.setOnTouchListener(contextualUndoListViewTouchListener);
		listView.setOnScrollListener(contextualUndoListViewTouchListener.makeScrollListener());
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
		} else {
			if (mCurrentRemovedView != null) {
				performRemoval();
			}
		}
	}

	private void restoreViewPosition(View view) {
		setAlpha(view, 1f);
		setTranslationX(view, 0);
	}

	private void removePreviousContextualUndoIfPresent() {
		if (mCurrentRemovedView != null) {
			performRemoval();
		}
	}

	private void setCurrentRemovedView(ContextualUndoView currentRemovedView) {
		mCurrentRemovedView = currentRemovedView;
		mCurrentRemovedId = currentRemovedView.getItemId();
	}

	private void clearCurrentRemovedView() {
		mCurrentRemovedView = null;
		mCurrentRemovedId = -1;
	}

	@Override
	public void onListScrolled() {
		if (mCurrentRemovedView != null) {
			performRemoval();
		}
	}

	private void performRemoval() {
		ValueAnimator animator = ValueAnimator.ofInt(mCurrentRemovedView.getHeight(), 1).setDuration(mAnimationTime);
		animator.addListener(new RemoveViewAnimatorListenerAdapter(mCurrentRemovedView));
		animator.addUpdateListener(new RemoveViewAnimatorUpdateListener(mCurrentRemovedView));
		animator.start();
		mActiveAnimators.put(mCurrentRemovedView, animator);
		clearCurrentRemovedView();
	}

	public void setDeleteItemCallback(DeleteItemCallback deleteItemCallback) {
		mDeleteItemCallback = deleteItemCallback;
	}

	public Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putLong("mCurrentRemovedId", mCurrentRemovedId);
		return bundle;
	}

	public void onRestoreInstanceState(Parcelable state) {
		Bundle bundle = (Bundle) state;
		mCurrentRemovedId = bundle.getLong("mCurrentRemovedId", -1);
	}

	public interface DeleteItemCallback {
		public void deleteItem(int position);
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
			ContextualUndoView contextualUndoView = (ContextualUndoView) mDismissView;
			int position = getListView().getPositionForView(contextualUndoView);
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
	};

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
			animate(mContextualUndoView).translationX(0).setDuration(mAnimationTime).setListener(null);
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