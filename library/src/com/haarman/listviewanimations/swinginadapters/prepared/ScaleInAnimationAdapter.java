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
package com.haarman.listviewanimations.swinginadapters.prepared;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public class ScaleInAnimationAdapter extends AnimationAdapter {

	private static final float DEFAULTSCALEFROM = 0.8f;

	private float mScaleFrom;
	private long mAnimationDelayMillis;
	private long mAnimationDurationMillis;

	public ScaleInAnimationAdapter(BaseAdapter baseAdapter) {
		this(baseAdapter, DEFAULTSCALEFROM);
	}

	public ScaleInAnimationAdapter(BaseAdapter baseAdapter, float scaleFrom) {
		this(baseAdapter, scaleFrom, DEFAULTANIMATIONDELAYMILLIS, DEFAULTANIMATIONDURATIONMILLIS);
	}

	public ScaleInAnimationAdapter(BaseAdapter baseAdapter, float scaleFrom, long animationDelayMillis, long animationDurationMillis) {
		super(baseAdapter);
		mScaleFrom = scaleFrom;
		mAnimationDelayMillis = animationDelayMillis;
		mAnimationDurationMillis = animationDurationMillis;
	}

	@Override
	protected long getAnimationDelayMillis() {
		return mAnimationDelayMillis;
	}

	@Override
	protected long getAnimationDurationMillis() {
		return mAnimationDurationMillis;
	}

	@Override
	public Animator[] getAnimators(ViewGroup parent, View view) {
		ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", mScaleFrom, 1f);
		ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", mScaleFrom, 1f);
		return new ObjectAnimator[] { scaleX, scaleY };
	}

}
