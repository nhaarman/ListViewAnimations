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

import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An EXPERIMENTAL adapter for inserting rows into the {@link android.widget.ListView} with an animation. The root {@link BaseAdapter} should implement {@link Insertable},
 * otherwise an {@link java.lang.IllegalArgumentException} is thrown. This class only works with an instance of {@code ListView}!
 * <p>
 * Usage:<br>
 * - Wrap a new instance of this class around a {@link android.widget.BaseAdapter}. <br>
 * - Set a {@code ListView} to this class using {@link #setListView(android.widget.ListView)}.<br>
 * - Call {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter#insert(int, Object)} to animate the addition of an item.
 * <p>
 * Extend this class and override {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter#getAdditionalAnimators(android.view.View,
 * android.view.ViewGroup)} to provide extra {@link com.nineoldandroids.animation.Animator}s.
 */
@SuppressWarnings("unchecked")
public class AnimateAdditionAdapter<T> extends BaseAdapterDecorator {

    private static final long DEFAULT_SCROLLDOWN_ANIMATION_MS = 300;
    private static final long DEFAULT_INSERTION_ANIMATION_MS = 300;
    private static final String ALPHA = "alpha";

    private final Insertable<T> mInsertable;
    private final InsertQueue<T> mInsertQueue;

    private boolean mShouldAnimateDown = true;

    private long mInsertionAnimationDurationMs = DEFAULT_INSERTION_ANIMATION_MS;
    private long mScrolldownAnimationDurationMs = DEFAULT_SCROLLDOWN_ANIMATION_MS;

    /**
     * Create a new {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter} with given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter should implement {@link com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter.Insertable},
     *                    or be a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator} whose BaseAdapter implements the interface.
     */
    public AnimateAdditionAdapter(final BaseAdapter baseAdapter) {
        super(baseAdapter);

        BaseAdapter rootAdapter = getRootAdapter();
        if (!(rootAdapter instanceof Insertable)) {
            throw new IllegalArgumentException("BaseAdapter should implement Insertable!");
        }

        mInsertable = (Insertable<T>) rootAdapter;
        mInsertQueue = new InsertQueue<T>(mInsertable);
    }

    private BaseAdapter getRootAdapter() {
        BaseAdapter adapter = getDecoratedBaseAdapter();
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }

        return adapter;
    }

    @Override
    @Deprecated
    /**
     * @deprecated AnimateAdditionAdapter requires a ListView instance. Use {@link #setListView(android.widget.ListView)} instead.
     */
    public void setAbsListView(final AbsListView listView) {
        if (!(listView instanceof ListView)) {
            throw new IllegalArgumentException("AnimateAdditionAdapter requires a ListView instance!");
        }
        super.setAbsListView(listView);
    }

    public void setListView(final ListView listView) {
        super.setAbsListView(listView);
    }

    /**
     * Set whether the list should animate downwards when items are added above the first visible item.
     * @param shouldAnimateDown defaults to {@code true}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setShouldAnimateDown(final boolean shouldAnimateDown) {
        mShouldAnimateDown = shouldAnimateDown;
    }

    /**
     * Set the duration of the scrolldown animation <i>per item</i> for when items are inserted above the first visible item.
     * @param scrolldownAnimationDurationMs the duration in ms.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setScrolldownAnimationDuration(final long scrolldownAnimationDurationMs) {
        mScrolldownAnimationDurationMs = scrolldownAnimationDurationMs;
    }

    /**
     * Set the duration of the insertion animation.
     * @param insertionAnimationDurationMs the duration in ms.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setInsertionAnimationDuration(final long insertionAnimationDurationMs) {
        mInsertionAnimationDurationMs = insertionAnimationDurationMs;
    }

    /**
     * Insert an item at given index. Will show an entrance animation for the new item if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link BaseAdapter}.
     *
     * @param index the index the new item should be inserted at
     * @param item  the item to insert
     */
    public void insert(final int index, final T item) {
        insert(new Pair<Integer, T>(index, item));
    }

    /**
     * Insert items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     */
    public void insert(final Pair<Integer, T>... indexItemPairs) {
        insert(Arrays.asList(indexItemPairs));
    }

    /**
     * Insert items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     */
    public void insert(final List<Pair<Integer, T>> indexItemPairs) {
        List<Pair<Integer, T>> visibleViews = new ArrayList<Pair<Integer, T>>();
        List<Integer> insertedPositions = new ArrayList<Integer>();
        List<Integer> insertedBelowPositions = new ArrayList<Integer>();

        int scrollDistance = 0;
        int numInsertedAbove = 0;

        for (Pair<Integer, T> pair : indexItemPairs) {
            if (getAbsListView().getFirstVisiblePosition() > pair.first) {
                /* Inserting an item above the first visible position */
                int index = pair.first;

                /* Correct the index for already inserted positions above the first visible view. */
                for (int insertedPosition : insertedPositions) {
                    if (index >= insertedPosition) {
                        index++;
                    }
                }

                mInsertable.add(index, pair.second);
                insertedPositions.add(index);
                numInsertedAbove++;

                if (mShouldAnimateDown) {
                    View view = getView(pair.first, null, getAbsListView());
                    view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    scrollDistance -= view.getMeasuredHeight();
                }
            } else if (getAbsListView().getLastVisiblePosition() >= pair.first) {
                /* Inserting an item that becomes visible on screen */
                int index = pair.first;

                /* Correct the index for already inserted positions above the first visible view */
                for (int insertedPosition : insertedPositions) {
                    if (index >= insertedPosition) {
                        index++;
                    }
                }
                Pair<Integer, T> newPair = new Pair<Integer, T>(index, pair.second);
                visibleViews.add(newPair);
            } else {
                /* Inserting an item below the last visible item */
                int index = pair.first;

                /* Correct the index for already inserted positions above the first visible view */
                for (int insertedPosition : insertedPositions) {
                    if (index >= insertedPosition) {
                        index++;
                    }
                }

                /* Correct the index for already inserted positions below the last visible view */
                for (int queuedPosition : insertedBelowPositions) {
                    if (index >= queuedPosition) {
                        index++;
                    }
                }

                insertedBelowPositions.add(index);
                mInsertable.add(index, pair.second);
            }
        }

        if (mShouldAnimateDown) {
            getAbsListView().smoothScrollBy(scrollDistance, (int) (mScrolldownAnimationDurationMs * numInsertedAbove));
        }

        mInsertQueue.insert(visibleViews);
        ((ListView) getAbsListView()).setSelectionFromTop(getAbsListView().getFirstVisiblePosition() + numInsertedAbove, getAbsListView().getChildAt(0).getTop());
    }


    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);

        if (mInsertQueue.getActiveIndexes().contains(position)) {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.AT_MOST);
            view.measure(widthMeasureSpec, heightMeasureSpec);

            int originalHeight = view.getMeasuredHeight();

            ValueAnimator heightAnimator = ValueAnimator.ofInt(1, originalHeight);
            heightAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator animation) {
                    ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
                    layoutParams.height = (Integer) animation.getAnimatedValue();
                    view.setLayoutParams(layoutParams);
                }
            });

            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

            AnimatorSet animatorSet = new AnimatorSet();
            Animator[] customAnimators = getAdditionalAnimators(view, parent);
            Animator[] animators = new Animator[customAnimators.length + 2];
            animators[0] = heightAnimator;
            animators[1] = alphaAnimator;
            System.arraycopy(customAnimators, 0, animators, 2, customAnimators.length);
            animatorSet.playTogether(animators);
            animatorSet.setDuration(mInsertionAnimationDurationMs);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(final Animator animation) {
                    mInsertQueue.removeActiveIndex(position);
                }
            });
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
    @SuppressWarnings("UnusedParameters")
    protected Animator[] getAdditionalAnimators(final View view, final ViewGroup parent) {
        return new Animator[]{};
    }

    /**
     * An interface for inserting items at a certain index.
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
}
