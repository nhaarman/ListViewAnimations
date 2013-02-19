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
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ListView;

public abstract class AnimationAdapter<T> extends ArrayAdapter<T> {

	private Context mContext;
	private Handler mHandler;

	private ListView mListView;

	private int mPreviousLastVisiblePosition;
	private long mAnimationStartMillis;
	private int mAnimatingViewsSinceAnimationStart;
	private int mDoneAnimatingViewsSinceAnimationStart;

	public AnimationAdapter(Context context) {
		this(context, null);
	}

	public AnimationAdapter(Context context, ArrayList<T> items) {
		super(items);
		mContext = context;
		mHandler = new Handler();

		mPreviousLastVisiblePosition = -1;
		mAnimationStartMillis = -1;
		mAnimatingViewsSinceAnimationStart = 0;
		mDoneAnimatingViewsSinceAnimationStart = 0;
	}

	public void setListView(ListView listView) {
		mListView = listView;
		mListView.setDivider(null);
	}

	@Override
	public final View getView(int position, View convertView, ViewGroup parent) {
		Assert.assertNotNull("Call setListView() on this AnimationAdapter before setAdapter()!", mListView);

		View itemView = getItemView(position, convertView, parent);
		animateViewIfNecessary(itemView, position);
		return itemView;
	}

	private void animateViewIfNecessary(View view, int position) {
		if (position > mPreviousLastVisiblePosition && position >= mListView.getFirstVisiblePosition()) {
			animateView(view);
		}
	}

	private void animateView(View view) {
		if (mAnimationStartMillis == -1) {
			mAnimationStartMillis = AnimationUtils.currentAnimationTimeMillis();
		}

		long delayMillis = calculateAnimationDelay();
		Animation animation = AnimationUtils.loadAnimation(mContext, getRowInAnimationResId());
		animation.setStartTime(mAnimationStartMillis);
		animation.setStartOffset(delayMillis);

		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				mDoneAnimatingViewsSinceAnimationStart++;

				if (mAnimatingViewsSinceAnimationStart == mDoneAnimatingViewsSinceAnimationStart) {
					mAnimatingViewsSinceAnimationStart = 0;
					mDoneAnimatingViewsSinceAnimationStart = 0;
					mAnimationStartMillis = -1;
					mPreviousLastVisiblePosition = mListView.getLastVisiblePosition();
				}
			}
		}, delayMillis - AnimationUtils.currentAnimationTimeMillis() + mAnimationStartMillis + animation.getDuration());

		view.setAnimation(animation);
		mAnimatingViewsSinceAnimationStart++;
	}

	private long calculateAnimationDelay() {
		return mAnimatingViewsSinceAnimationStart * getAnimationDelayMillis();
	}

	public Context getContext() {
		return mContext;
	}

	protected abstract View getItemView(int position, View convertView, ViewGroup parent);

	protected abstract int getRowInAnimationResId();

	protected abstract long getAnimationDelayMillis();

}
