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
package com.haarman.listviewanimations.itemmanipulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import android.view.View;
import android.view.ViewGroup;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.ArrayAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

/**
 * An ArrayAdapterDecorator class that provides animations to the removal of
 * items in the given ArrayAdapter.
 */
public class AnimateDismissAdapter<T> extends ArrayAdapterDecorator<T> {

	/**
	 * Create a new AnimateDismissAdapter based on the given ArrayAdapter.
	 */
	public AnimateDismissAdapter(ArrayAdapter<T> arrayAdapter) {
		super(arrayAdapter);
	}

	@Override
	public void remove(T item) {
		int index = indexOf(item);
		remove(index);
	}

	@Override
	public void remove(final int index) {
		removePositions(Arrays.asList(index));
	}

	@Override
	public void removeAll(Collection<T> items) {
		List<Integer> positions = new ArrayList<Integer>();

		for (T item : items) {
			positions.add(indexOf(item));
		}

		removePositions(positions);
	}

	@Override
	public void removePositions(final Collection<Integer> positions) {
		Assert.assertNotNull("Call setListView() on this AnimateDismissAdapter before calling setAdapter()!", getListView());

		List<View> views = getVisibleViewsForPositions(positions);

		if (!views.isEmpty()) {
			List<Animator> animators = new ArrayList<Animator>();
			for (final View view : views) {
				animators.add(createAnimatorForView(view));
			}

			AnimatorSet animatorSet = new AnimatorSet();

			Animator[] animatorsArray = new Animator[animators.size()];
			for (int i = 0; i < animatorsArray.length; i++) {
				animatorsArray[i] = animators.get(i);
			}

			animatorSet.playTogether(animatorsArray);
			animatorSet.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator arg0) {
				}

				@Override
				public void onAnimationRepeat(Animator arg0) {
				}

				@Override
				public void onAnimationEnd(Animator arg0) {
					AnimateDismissAdapter.super.removePositions(positions);
				}

				@Override
				public void onAnimationCancel(Animator arg0) {
				}
			});
			animatorSet.setDuration(2000);
			animatorSet.start();
		} else {
			AnimateDismissAdapter.super.removePositions(positions);
		}
	}

	private List<View> getVisibleViewsForPositions(Collection<Integer> positions) {
		List<View> views = new ArrayList<View>();
		for (int i = 0; i < getListView().getChildCount(); i++) {
			View child = getListView().getChildAt(i);
			if (positions.contains(getListView().getPositionForView(child))) {
				views.add(child);
			}
		}
		return views;
	}

	private Animator createAnimatorForView(final View view) {
		final ViewGroup.LayoutParams lp = view.getLayoutParams();
		final int originalHeight = view.getHeight();

		ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
		animator.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {
			}

			@Override
			public void onAnimationRepeat(Animator arg0) {
			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				lp.height = 0;
				view.setLayoutParams(lp);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {
			}
		});

		animator.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator valueAnimator) {
				lp.height = (Integer) valueAnimator.getAnimatedValue();
				view.setLayoutParams(lp);
			}
		});

		return animator;
	}

}
