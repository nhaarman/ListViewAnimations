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
package com.nhaarman.listviewanimations.appearance.simple;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.appearance.SingleAnimationRecyclerViewAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

/**
 * An implementation of the AnimationAdapter class which applies a
 * swing-in-from-the-right-animation to views.
 */
public class SwingRightInAnimationRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends SingleAnimationRecyclerViewAdapter<T> {

    private static final String TRANSLATION_X = "translationX";

    public SwingRightInAnimationRecyclerViewAdapter(@NonNull final RecyclerView.Adapter<T> adapter) {
        super(adapter);
    }

    @NonNull
    @Override
    protected Animator getAnimator(@NonNull final ViewGroup parent, @NonNull final View view) {
        return ObjectAnimator.ofFloat(view, TRANSLATION_X, parent.getWidth(), 0);
    }
}
