/*
 * Copyright 2014 Niek Haarman
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
package com.nhaarman.listviewanimations.appearance;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nineoldandroids.animation.Animator;

import android.support.annotation.NonNull;

/**
 * An implementation of AnimationAdapter which applies a single Animator to
 * views.
 */
public abstract class SingleAnimationAdapter extends AnimationAdapter {

    protected SingleAnimationAdapter(@NonNull final BaseAdapter baseAdapter) {
        super(baseAdapter);
    }

    @NonNull
    @Override
    public Animator[] getAnimators(@NonNull final ViewGroup parent, @NonNull final View view) {
        Animator animator = getAnimator(parent, view);
        return new Animator[]{animator};
    }

    /**
     * Get the {@link Animator} to apply to the {@link View}.
     *
     * @param parent the {@link ViewGroup} which is the parent of the View.
     * @param view   the View that will be animated, as retrieved by
     *               {@link #getView(int, View, ViewGroup)}.
     */
    @NonNull
    protected abstract Animator getAnimator(@NonNull ViewGroup parent, @NonNull View view);

}
