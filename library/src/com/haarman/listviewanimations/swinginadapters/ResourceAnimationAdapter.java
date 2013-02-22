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

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorInflater;

/**
 * An implementation of AnimationAdapter which bases the animations on
 * resources.
 */
public abstract class ResourceAnimationAdapter<T> extends AnimationAdapter<T> {

	public ResourceAnimationAdapter(Context context) {
		super(context);
	}

	public ResourceAnimationAdapter(Context context, ArrayList<T> items) {
		super(context, items);
	}

	@Override
	protected Animator getAnimator(ViewGroup parent, View view) {
		return AnimatorInflater.loadAnimator(getContext(), getAnimationResourceId());
	}

	/**
	 * Get the resource id of the animation to apply to the rows.
	 */
	protected abstract int getAnimationResourceId();

}
