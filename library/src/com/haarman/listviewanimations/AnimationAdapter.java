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
package com.haarman.listviewanimations;

import java.util.ArrayList;

import junit.framework.Assert;
import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.PropertyValuesHolder;

public abstract class AnimationAdapter<T> extends ArrayAdapter<T> {

	private Context mContext;

	private ListView mListView;

	private SparseArray<Animator> mAnimators;
	private int mPreviousLastVisiblePosition;
	private long mAnimationStartMillis;
	private int mAnimatingViewsSinceAnimationStart;

	public AnimationAdapter(Context context) {
		this(context, null);
	}

	public AnimationAdapter(Context context, ArrayList<T> items) {
		super(items);
		mContext = context;
		mAnimators = new SparseArray<Animator>();

		mPreviousLastVisiblePosition = -1;
		mAnimationStartMillis = -1;
		mAnimatingViewsSinceAnimationStart = 0;
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
		Assert.assertNotNull("Call setListView() on this AnimationAdapter before setAdapter()!", mListView);

		if (convertView != null) {
			int previousPosition = (Integer) convertView.getTag();
			Animator animator = mAnimators.get(previousPosition);
			if (animator != null) {
				animator.end();
			}
			mAnimators.remove(previousPosition);
		}

		View itemView = getItemView(position, convertView, parent);
		itemView.setTag(position);
		animateViewIfNecessary(position, itemView, parent);
		return itemView;
	}

	private void animateViewIfNecessary(int position, View view, ViewGroup parent) {
		if (position > mPreviousLastVisiblePosition && position >= mListView.getFirstVisiblePosition()) {
			animateView(view, parent);
		}
	}

	private void animateView(View view, ViewGroup parent) {
		if (mAnimationStartMillis == -1) {
			mAnimationStartMillis = System.currentTimeMillis();
		}

		view.setAlpha(0);

		PropertyValuesHolder translatePropertyValuesHolder = getTranslatePropertyValuesHolder(parent);
		PropertyValuesHolder alphaPropertyValuesHolder = PropertyValuesHolder.ofFloat("alpha", 0, 1);
		ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(view, translatePropertyValuesHolder, alphaPropertyValuesHolder);
		objectAnimator.setStartDelay(calculateAnimationDelay());
		objectAnimator.start();

		mAnimators.put((Integer) view.getTag(), objectAnimator);
		mAnimatingViewsSinceAnimationStart++;
	}

	protected abstract PropertyValuesHolder getTranslatePropertyValuesHolder(ViewGroup parent);

	private long calculateAnimationDelay() {
		long delay;
		int numberOfItems = mListView.getLastVisiblePosition() - mListView.getFirstVisiblePosition();
		if (numberOfItems + 1 < mAnimatingViewsSinceAnimationStart) {
			delay = getAnimationDelayMillis();
		} else {
			long delaySinceStart = mAnimatingViewsSinceAnimationStart * getAnimationDelayMillis();
			delay = mAnimationStartMillis + delaySinceStart - System.currentTimeMillis();
		}
		return delay;
	}

	public Context getContext() {
		return mContext;
	}

	protected abstract View getItemView(int position, View convertView, ViewGroup parent);

	protected abstract long getAnimationDelayMillis();
}
