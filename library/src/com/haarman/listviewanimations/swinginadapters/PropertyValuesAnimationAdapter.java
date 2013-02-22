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
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * An implementation of AnimationAdapter which bases the animations on a
 * PropertyValuesHolder and applies an alpha transition.
 */
public abstract class PropertyValuesAnimationAdapter<T> extends AnimationAdapter<T> {

	public PropertyValuesAnimationAdapter(Context context, ArrayList<T> items) {
		super(context, items);
	}

	public PropertyValuesAnimationAdapter(Context context) {
		super(context);
	}

	@Override
	protected Animator[] getAnimators(ViewGroup parent, View view) {
		Animator translateAnimator = getTranslateAnimator(parent, view);
		Animator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);
		return new Animator[] { translateAnimator, alphaAnimator };
	}

	/**
	 * Get the PropertyValuesHolder which contains translate properties to apply
	 * to rows.
	 * 
	 * @param parent
	 *            the ViewGroup which is the parent of the row.
	 * @param view
	 *            the view that will be animated.
	 */
	protected abstract Animator getTranslateAnimator(ViewGroup parent, View view);

}
