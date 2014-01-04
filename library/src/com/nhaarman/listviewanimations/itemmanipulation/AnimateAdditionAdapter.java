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

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * An adapter for inserting rows into the {@link android.widget.AbsListView} with an animation. The root {@link BaseAdapter} should implement {@link Insertable},
 * otherwise an {@link java.lang.IllegalArgumentException} is thrown.
 * </p>
 * Usage:
 * Wrap a new instance of this class around a {@link android.widget.BaseAdapter}. Call {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter#insert(int, Object)} to animate the addition of an item.
 * </p>
 * Extend this class and override {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter#getAdditionalAnimators(android.view.View,
 * android.view.ViewGroup)} to provide extra {@link com.nineoldandroids.animation.Animator}s.
 */
public class AnimateAdditionAdapter<T> extends BaseAdapterDecorator {

    /**
     * An interface for inserting items at a certain index.
     *
     * @param <T>
     */
    public interface Insertable<T> {

        /**
         * Will be called to insert given {@code item} at given {@code index} in the list.
         *
         * @param index the index the new item should be inserted at
         * @param item  the item to insert
         */
        public void add(int index, T item);
    }

    private int mInsertedPosition = -1;

    /**
     * Create a new {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter} with given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter should implement {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter.Insertable},
     *                    or be a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator} whose BaseAdapter implements the interface.
     */
    public AnimateAdditionAdapter(BaseAdapter baseAdapter) {
        super(baseAdapter);

        if (!(getRootAdapter() instanceof Insertable)) {
            throw new IllegalArgumentException("BaseAdapter should implement Insertable!");
        }
    }

    private BaseAdapter getRootAdapter() {
        BaseAdapter adapter = getDecoratedBaseAdapter();
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }

        return adapter;
    }

    /**
     * Insert an item at given index. Will show an entrance animation for the new item.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link BaseAdapter}.
     *
     * @param index the index the new item should be inserted at
     * @param item  the item to insert
     */
    @SuppressWarnings("unchecked")
    public void insert(int index, T item) {
        mInsertedPosition = index;
        ((Insertable<T>) getRootAdapter()).add(index, item);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);

        if (position == mInsertedPosition) {
            mInsertedPosition = -1;

            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);
            view.measure(widthMeasureSpec, heightMeasureSpec);

            int originalHeight = view.getMeasuredHeight();

            ValueAnimator heightAnimator = ValueAnimator.ofInt(1, originalHeight);
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = (Integer) animation.getAnimatedValue();
                    view.setLayoutParams(layoutParams);
                }
            });

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 0, 1);

            AnimatorSet animatorSet = new AnimatorSet();
            Animator[] customAnimators = getAdditionalAnimators(view, parent);
            Animator[] animators = new Animator[customAnimators.length + 2];
            animators[0] = heightAnimator;
            animators[1] = alphaAnimator;
            for (int i = 0; i < customAnimators.length; i++) {
                animators[i + 2] = customAnimators[i];
            }
            animatorSet.playTogether(animators);
            animatorSet.start();
        }

        return view;
    }

    /**
     * Override this method to provide additional animators on top of the default height and alpha animation.
     *
     * @param view   The {@link View} that will get animated.
     * @param parent The parent that this view will eventually be attached to.
     * @return a non-null array of Animators.
     */
    protected Animator[] getAdditionalAnimators(View view, ViewGroup parent) {
        return new Animator[]{};
    }
}
