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
package com.nhaarman.listviewanimations.itemmanipulation;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A {@link BaseAdapterDecorator} class that provides animations to the removal
 * of items in the given {@link BaseAdapter}.
 */
public class AnimateDismissAdapter extends BaseAdapterDecorator {

    @NonNull
    private final OnDismissCallback mCallback;

    /**
     * Create a new AnimateDismissAdapter based on the given {@link BaseAdapter}.
     *
     * @param callback The {@link OnDismissCallback} to trigger when the user has
     *                 indicated that she would like to dismiss one or more list
     *                 items.
     */
    public AnimateDismissAdapter(@NonNull final BaseAdapter baseAdapter, @NonNull final OnDismissCallback callback) {
        super(baseAdapter);
        mCallback = callback;
    }

    /**
     * Animate dismissal of the item at given position.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void animateDismiss(final int position) {
        animateDismiss(Collections.singletonList(position));
    }

    /**
     * Animate dismissal of the items at given positions.
     */
    public void animateDismiss(@NonNull final Collection<Integer> positions) {
        final Collection<Integer> positionsCopy = new ArrayList<>(positions);
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setAbsListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        List<View> views = getVisibleViewsForPositions(positionsCopy);

        if (views.isEmpty()) {
            invokeCallback(positionsCopy);
        } else {
            List<Animator> animators = new ArrayList<>();
            for (final View view : views) {
                animators.add(createAnimatorForView(view));
            }

            AnimatorSet animatorSet = new AnimatorSet();

            Animator[] animatorsArray = new Animator[animators.size()];
            for (int i = 0; i < animatorsArray.length; i++) {
                animatorsArray[i] = animators.get(i);
            }

            animatorSet.playTogether(animatorsArray);
            animatorSet.addListener(
                    new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            invokeCallback(positionsCopy);
                        }
                    }
            );
            animatorSet.start();
        }
    }

    private void invokeCallback(@NonNull final Collection<Integer> positions) {
        if (getAbsListView() == null) {
            throw new IllegalArgumentException("Call setAbsListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        ArrayList<Integer> positionsList = new ArrayList<>(positions);
        Collections.sort(positionsList);

        int[] dismissPositions = new int[positionsList.size()];
        for (int i = 0; i < positionsList.size(); i++) {
            dismissPositions[i] = positionsList.get(positionsList.size() - 1 - i);
        }
        mCallback.onDismiss(getAbsListView(), dismissPositions);
    }

    @NonNull
    private List<View> getVisibleViewsForPositions(@NonNull final Collection<Integer> positions) {
        if (getAbsListView() == null) {
            throw new IllegalArgumentException("Call setAbsListView() on this AnimateDismissAdapter before calling setAdapter()!");
        }

        List<View> views = new ArrayList<>();
        for (int i = 0; i < getAbsListView().getChildCount(); i++) {
            View child = getAbsListView().getChildAt(i);
            if (positions.contains(AdapterViewUtil.getPositionForView(getAbsListView(), child))) {
                views.add(child);
            }
        }
        return views;
    }

    @NonNull
    private Animator createAnimatorForView(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        final int originalHeight = view.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 0);
        animator.addListener(
                new AnimatorListenerAdapter() {

                    @Override
                    public void onAnimationEnd(final Animator animation) {
                        lp.height = 0;
                        view.setLayoutParams(lp);
                    }
                }
        );

        animator.addUpdateListener(
                new AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(final ValueAnimator animation) {
                        lp.height = (Integer) animation.getAnimatedValue();
                        view.setLayoutParams(lp);
                    }
                }
        );

        return animator;
    }
}
