/*
 * Copyright 2013 Niek Haarman
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
package com.haarman.listviewanimations.swinginadapters;

import junit.framework.Assert;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * An ArrayAdapter class which applies multiple Animators at once to views when
 * they are first shown. The Animators applied are to be specified in
 * getAnimators(ViewGroup, View), plus an alpha transition.
 */
public abstract class AnimationAdapter extends BaseAdapterDecorator {

	private static final long INITIALDELAYMILLIS = 150;

	private ListView mListView;

	private SparseArray<Animator> mAnimators;
	private long mAnimationStartMillis;
	private int mLastAnimatedPosition;

	private boolean mHasParentAnimationAdapter;

	public AnimationAdapter(BaseAdapter baseAdapter) {
		super(baseAdapter);
		mAnimators = new SparseArray<Animator>();

		mAnimationStartMillis = -1;
		mLastAnimatedPosition = -1;

		if (baseAdapter instanceof AnimationAdapter) {
			((AnimationAdapter) baseAdapter).setHasParentAnimationAdapter(true);
		}
	}

	public void setListView(ListView listView) {
		mListView = listView;
		if (mListView.getDivider() != null) {
			int dividerHeight = mListView.getDividerHeight();
			mListView.setDivider(null);
			mListView.setDividerHeight(dividerHeight);
		}
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		if (!mHasParentAnimationAdapter) {
			Assert.assertNotNull("Call setListView() on this AnimationAdapter before setAdapter()!", mListView);

			if (convertView != null) {
				int previousPosition = (Integer) convertView.getTag();
				Animator animator = mAnimators.get(previousPosition);
				if (animator != null) {
					animator.end();
				}
				mAnimators.remove(previousPosition);
			}
		}

		View itemView = super.getView(position, convertView, parent);
		if (!mHasParentAnimationAdapter) {
			itemView.setTag(position);
			animateViewIfNecessary(position, itemView, parent);
		}
		return itemView;
	}

	private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
		if (position > mLastAnimatedPosition && !mHasParentAnimationAdapter) {
			animateView(parent, view);
			mLastAnimatedPosition = position;
		}
	}

	private void animateView(ViewGroup parent, View view) {
		if (mAnimationStartMillis == -1) {
			mAnimationStartMillis = System.currentTimeMillis();
		}

		hideView(view);

		Animator[] childAnimators;
		if (decoratedBaseAdapter instanceof AnimationAdapter) {
			childAnimators = ((AnimationAdapter) decoratedBaseAdapter).getAnimators(parent, view);
		} else {
			childAnimators = new Animator[0];
		}
		Animator[] animators = getAnimators(parent, view);
		Animator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);

		AnimatorSet set = new AnimatorSet();
		set.playTogether(concatAnimators(childAnimators, animators, alphaAnimator));
		set.setStartDelay(calculateAnimationDelay());
		set.start();

		mAnimators.put((Integer) view.getTag(), set);
	}

	private void hideView(View view) {
		ObjectAnimator animator = ObjectAnimator.ofFloat(view, "alpha", 0);
		AnimatorSet set = new AnimatorSet();
		set.play(animator);
		set.setDuration(0);
		set.start();
	}

	private Animator[] concatAnimators(Animator[] childAnimators, Animator[] animators, Animator alphaAnimator) {
		Animator[] allAnimators = new Animator[childAnimators.length + animators.length + 1];
		int i;

		for (i = 0; i < animators.length; ++i) {
			allAnimators[i] = animators[i];
		}

		for (int j = 0; j < childAnimators.length; ++j) {
			allAnimators[i] = childAnimators[j];
			++i;
		}

		allAnimators[allAnimators.length - 1] = alphaAnimator;
		return allAnimators;
	}

	private long calculateAnimationDelay() {
		long delay;
		int numberOfItems = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition();
		if (numberOfItems + 1 < mLastAnimatedPosition) {
			delay = getAnimationDelayMillis();
		} else {
			long delaySinceStart = (mLastAnimatedPosition + 1) * getAnimationDelayMillis();
			delay = mAnimationStartMillis + INITIALDELAYMILLIS + delaySinceStart - System.currentTimeMillis();
		}
		return Math.max(0, delay);
	}

	/**
	 * Set whether this AnimationAdapter is encapsulated by another
	 * AnimationAdapter. When this is set to true, this AnimationAdapter does
	 * not apply any animations to the views. Should not be set explicitly, the
	 * AnimationAdapter class manages this by itself.
	 */
	public void setHasParentAnimationAdapter(boolean hasParentAnimationAdapter) {
		mHasParentAnimationAdapter = hasParentAnimationAdapter;
	}

	/**
	 * Get the delay in milliseconds before an animation of a view should start.
	 */
	protected abstract long getAnimationDelayMillis();

	/**
	 * Get the Animators to apply to the views. In addition to the returned
	 * Animators, an alpha transition will be applied to the view.
	 * 
	 * @param parent
	 *            The parent of the view
	 * @param view
	 *            The view that will be animated, as retrieved by getView()
	 */
	public abstract Animator[] getAnimators(ViewGroup parent, View view);

}
