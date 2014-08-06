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

package com.nhaarman.listviewanimations.itemmanipulation.animateaddition;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.util.AbsListViewWrapper;
import com.nhaarman.listviewanimations.util.Insertable;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * An adapter for inserting rows into the {@link android.widget.ListView} with an animation. The root {@link android.widget.BaseAdapter} should implement {@link Insertable},
 * otherwise an {@link IllegalArgumentException} is thrown. This class only works with an instance of {@code ListView}!
 * <p/>
 * Usage:<br>
 * - Wrap a new instance of this class around a {@link android.widget.BaseAdapter}. <br>
 * - Set a {@code ListView} to this class using {@link #setListView(android.widget.ListView)}.<br>
 * - Call {@link AnimateAdditionAdapter#insert(int, Object)} to animate the addition of an item.
 * <p/>
 * Extend this class and override {@link AnimateAdditionAdapter#getAdditionalAnimators(android.view.View,
 * android.view.ViewGroup)} to provide extra {@link com.nineoldandroids.animation.Animator}s.
 */
public class AnimateAdditionAdapter<T> extends BaseAdapterDecorator {

    private static final long DEFAULT_SCROLLDOWN_ANIMATION_MS = 300;

    private long mScrolldownAnimationDurationMs = DEFAULT_SCROLLDOWN_ANIMATION_MS;

    private static final long DEFAULT_INSERTION_ANIMATION_MS = 300;

    private long mInsertionAnimationDurationMs = DEFAULT_INSERTION_ANIMATION_MS;

    private static final String ALPHA = "alpha";

    @NonNull
    private final Insertable<T> mInsertable;

    @NonNull
    private final InsertQueue<T> mInsertQueue;

    /**
     * Describes whether the list should animate downwards when items are added above the first visible item.
     */
    private boolean mShouldAnimateDown = true;

    /**
     * Create a new {@code AnimateAdditionAdapter} with given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter should implement {@link Insertable},
     *                    or be a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator} whose BaseAdapter implements the interface.
     */
    public AnimateAdditionAdapter(@NonNull final BaseAdapter baseAdapter) {
        super(baseAdapter);

        BaseAdapter rootAdapter = getRootAdapter();
        if (!(rootAdapter instanceof Insertable)) {
            throw new IllegalArgumentException("BaseAdapter should implement Insertable!");
        }

        mInsertable = (Insertable<T>) rootAdapter;
        mInsertQueue = new InsertQueue<>(mInsertable);
    }

    /**
     * @deprecated use {@link #setListView(android.widget.ListView)} instead.
     */
    @Override
    @Deprecated
    public void setAbsListView(@NonNull final AbsListView absListView) {
        if (absListView instanceof ListView) {
            setListView((ListView) absListView);
        } else {
            throw new IllegalArgumentException("AnimateAdditionAdapter requires a ListView!");
        }
    }

    /**
     * Sets the {@link android.widget.ListView} that is used for this {@code AnimateAdditionAdapter}.
     */
    public void setListView(@NonNull final ListView listView) {
        setListViewWrapper(new AbsListViewWrapper(listView));
    }

    /**
     * Sets whether the list should animate downwards when items are added above the first visible item.
     *
     * @param shouldAnimateDown defaults to {@code true}.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setShouldAnimateDown(final boolean shouldAnimateDown) {
        mShouldAnimateDown = shouldAnimateDown;
    }

    /**
     * Set the duration of the scrolldown animation <i>per item</i> for when items are inserted above the first visible item.
     *
     * @param scrolldownAnimationDurationMs the duration in ms.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setScrolldownAnimationDuration(final long scrolldownAnimationDurationMs) {
        mScrolldownAnimationDurationMs = scrolldownAnimationDurationMs;
    }

    /**
     * Set the duration of the insertion animation.
     *
     * @param insertionAnimationDurationMs the duration in ms.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setInsertionAnimationDuration(final long insertionAnimationDurationMs) {
        mInsertionAnimationDurationMs = insertionAnimationDurationMs;
    }

    /**
     * Inserts an item at given index. Will show an entrance animation for the new item if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param index the index the new item should be inserted at.
     * @param item  the item to insert.
     */
    public void insert(final int index, @NonNull final T item) {
        insert(new Pair<>(index, item));
    }

    /**
     * Inserts items, starting at given index. Will show an entrance animation for the new items if the newly added items are visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param index the starting index the new items should be inserted at.
     * @param items the items to insert.
     */
    public void insert(final int index, @NonNull final T... items) {
        Pair<Integer, T>[] pairs = new Pair[items.length];
        for (int i = 0; i < items.length; i++) {
            pairs[i] = new Pair<>(index + i, items[i]);
        }
        insert(pairs);
    }

    /**
     * Inserts items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     */
    public void insert(@NonNull final Pair<Integer, T>... indexItemPairs) {
        insert(Arrays.asList(indexItemPairs));
    }

    /**
     * Inserts items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     */
    public void insert(@NonNull final Iterable<Pair<Integer, T>> indexItemPairs) {
        if (getListViewWrapper() == null) {
            throw new IllegalStateException("Call setListView on this AnimateAdditionAdapter!");
        }

        Collection<Pair<Integer, T>> visibleViews = new ArrayList<>();
        Collection<Integer> insertedPositions = new ArrayList<>();
        Collection<Integer> insertedBelowPositions = new ArrayList<>();

        int scrollDistance = 0;
        int numInsertedAbove = 0;

        for (Pair<Integer, T> pair : indexItemPairs) {
            if (getListViewWrapper().getFirstVisiblePosition() > pair.first) {
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
                    View view = getView(pair.first, null, getListViewWrapper().getListView());
                    view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED), View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                    scrollDistance -= view.getMeasuredHeight();
                }
            } else if (getListViewWrapper().getLastVisiblePosition() >= pair.first || getListViewWrapper().getLastVisiblePosition() == AdapterView.INVALID_POSITION ||
                    !childrenFillAbsListView()) {
                /* Inserting an item that becomes visible on screen */
                int index = pair.first;

                /* Correct the index for already inserted positions above the first visible view */
                for (int insertedPosition : insertedPositions) {
                    if (index >= insertedPosition) {
                        index++;
                    }
                }
                Pair<Integer, T> newPair = new Pair<>(index, pair.second);
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
            ((AbsListView) getListViewWrapper().getListView()).smoothScrollBy(scrollDistance, (int) (mScrolldownAnimationDurationMs * numInsertedAbove));
        }

        mInsertQueue.insert(visibleViews);

        int firstVisiblePosition = getListViewWrapper().getFirstVisiblePosition();
        View firstChild = getListViewWrapper().getChildAt(0);
        int childTop = firstChild == null ? 0 : firstChild.getTop();
        ((ListView) getListViewWrapper().getListView()).setSelectionFromTop(firstVisiblePosition + numInsertedAbove, childTop);
    }

    /**
     * @return true if the children completely fill up the AbsListView.
     */
    private boolean childrenFillAbsListView() {
        if (getListViewWrapper() == null) {
            throw new IllegalStateException("Call setListView on this AnimateAdditionAdapter first!");
        }

        int childrenHeight = 0;
        for (int i = 0; i < getListViewWrapper().getCount(); i++) {
            View child = getListViewWrapper().getChildAt(i);
            if (child != null) {
                childrenHeight += child.getHeight();
            }
        }
        return getListViewWrapper().getListView().getHeight() <= childrenHeight;
    }

    @Override
    @NonNull
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        final View view = super.getView(position, convertView, parent);

        if (mInsertQueue.getActiveIndexes().contains(position)) {
            int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.AT_MOST);
            int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
            view.measure(widthMeasureSpec, heightMeasureSpec);

            int originalHeight = view.getMeasuredHeight();

            ValueAnimator heightAnimator = ValueAnimator.ofInt(1, originalHeight);
            heightAnimator.addUpdateListener(new HeightUpdater(view));

            Animator[] customAnimators = getAdditionalAnimators(view, parent);
            Animator[] animators = new Animator[customAnimators.length + 1];
            animators[0] = heightAnimator;
            System.arraycopy(customAnimators, 0, animators, 1, customAnimators.length);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);

            ViewHelper.setAlpha(view, 0);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

            AnimatorSet allAnimatorsSet = new AnimatorSet();
            allAnimatorsSet.playSequentially(animatorSet, alphaAnimator);

            allAnimatorsSet.setDuration(mInsertionAnimationDurationMs);
            allAnimatorsSet.addListener(new ExpandAnimationListener(position));
            allAnimatorsSet.start();
        }

        return view;
    }

    /**
     * Override this method to provide additional animators on top of the default height and alpha animation.
     *
     * @param view   The {@link android.view.View} that will get animated.
     * @param parent The parent that this view will eventually be attached to.
     *
     * @return a non-null array of Animators.
     */
    @SuppressWarnings({"MethodMayBeStatic", "UnusedParameters"})
    @NonNull
    protected Animator[] getAdditionalAnimators(@NonNull final View view, @NonNull final ViewGroup parent) {
        return new Animator[]{};
    }

    /**
     * A class which applies the animated height value to a {@code View}.
     */
    private static class HeightUpdater implements ValueAnimator.AnimatorUpdateListener {

        private final View mView;

        HeightUpdater(final View view) {
            mView = view;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
            layoutParams.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(layoutParams);
        }
    }

    /**
     * A class which removes the active index from the {@code InsertQueue} when the animation has finished.
     */
    private class ExpandAnimationListener extends AnimatorListenerAdapter {

        private final int mPosition;

        ExpandAnimationListener(final int position) {
            mPosition = position;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mInsertQueue.removeActiveIndex(mPosition);
        }
    }
}
