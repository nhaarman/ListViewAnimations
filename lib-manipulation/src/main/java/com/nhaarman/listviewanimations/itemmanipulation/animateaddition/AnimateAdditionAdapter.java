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
import android.widget.SectionIndexer;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListItemView;
import com.nhaarman.listviewanimations.util.AbsListViewWrapper;
import com.nhaarman.listviewanimations.util.Insertable;
import com.nhaarman.listviewanimations.util.Removable;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import se.emilsjolander.stickylistheaders.WrapperView;

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
 * int, android.view.ViewGroup)} to provide extra {@link com.nineoldandroids.animation.Animator}s.
 */
public class AnimateAdditionAdapter<T> extends BaseAdapterDecorator {

    private static final long DEFAULT_SCROLLDOWN_ANIMATION_MS = 300;

    private long mScrolldownAnimationDurationMs = DEFAULT_SCROLLDOWN_ANIMATION_MS;

    private static final long DEFAULT_INSERTION_ANIMATION_MS = 300;

    private long mInsertionAnimationDurationMs = DEFAULT_INSERTION_ANIMATION_MS;
    private long mRemovalAnimationDurationMs = DEFAULT_INSERTION_ANIMATION_MS;

    @NonNull
    private final Insertable<T> mInsertable;

    @NonNull
    private final Removable<T> mRemovable;

    /**
     * The {@link android.view.View}s that have been removed.
     */
    @NonNull
    private final Collection<View> mRemovedViews = new LinkedList<>();

    /**
     * The removed positions.
     */
    @NonNull
    private final List<Integer> mRemovedPositions = new LinkedList<>();

    /**
     * The number of active remove animations.
     */
    private int mActiveRemoveCount;

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
        mRemovable = (Removable<T>) rootAdapter;
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

    public void removeItem(final int position) {
        removeItem(position, 1);
    }

    /**
     * Remove items, starting at given index. Will show an leaving animation for the item.
     * Will also call {@link Removable#remove(int)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param position first item to remove.
     * @param count number of items to remove
     */
    public void removeItem(final int position, final int count) {
        int headerViewsCount = getListViewWrapper().getHeaderViewsCount();
        int  posRemovable;
        int firstVisiblePosition = getListViewWrapper().getFirstVisiblePosition();
        int lastVisiblePosition = getListViewWrapper().getLastVisiblePosition();

        List<Animator> allAnimators = new ArrayList<Animator>();
        for (int i = position; i < position + count; i++) {
            if (mRemovedPositions.contains(i)) {
                //already removing
                continue;
            }
            if (i < firstVisiblePosition || i > lastVisiblePosition) {
                mRemovedPositions.add(i);
            } else {

                View view = getListViewWrapper().getChildAt(i - firstVisiblePosition + headerViewsCount);
                mRemovedPositions.add(i);
                if (view == null) {
                    continue;
                }
                allAnimators.add(animatorSetForViewRemoval(i, view, (ViewGroup) view.getParent()));
            }
        }
        if (allAnimators.size() > 0) {
            AnimatorSet allAnimatorSet = new AnimatorSet();
            allAnimatorSet.playTogether(allAnimators);
            allAnimatorSet.setDuration(mRemovalAnimationDurationMs);
            allAnimatorSet.start();
        }
        else {
            finalizeRemove();
        }
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

            Animator[] customAnimators = getAdditionalAnimators(view, position, parent);
            Animator[] animators = new Animator[customAnimators.length + 1];
            animators[0] = heightAnimator;
            System.arraycopy(customAnimators, 0, animators, 1, customAnimators.length);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playTogether(animators);

            animatorSet.setDuration(mInsertionAnimationDurationMs);
            animatorSet.addListener(new ExpandAnimationListener(position, view));
            animatorSet.start();
        } else if (mRemovedPositions.contains(position)) {
            animatorSetForViewRemoval(position, view, parent).start();
        }

        return view;
    }
    
    private AnimatorSet animatorSetForViewRemoval(final int position, View view, final ViewGroup parent) {
        if (view instanceof WrapperView) {
            //StickyListHeadersView WrapperView
            final BaseAdapter rootAdapter = getRootAdapter();
            final WrapperView wrapperView =  ((WrapperView)view);
            boolean animateOnlyItem = true;
            if (rootAdapter instanceof SectionIndexer) {
                SectionIndexer sectionIndexer = (SectionIndexer) rootAdapter;
                if (wrapperView.hasHeader() && 
                        
                    ( position+1 >= rootAdapter.getCount() || 
                            (sectionIndexer.getSectionForPosition(position) != sectionIndexer.getSectionForPosition(position+1)))) {
                    animateOnlyItem = false; 
                }
            }
            if (animateOnlyItem) {
                view = wrapperView.getItem();
            }
        }
        
        
        int originalHeight = view.getMeasuredHeight();

        if (view instanceof DynamicListItemView) {
            //this is to get a nice "closing" animation by making sure the contentView
            //height remain fix during the animation
            View containerView = ((DynamicListItemView) view).getContainerView();
            ViewGroup.LayoutParams params = containerView.getLayoutParams();
            params.height = originalHeight;
            containerView.setLayoutParams(params);
        }

        ValueAnimator heightAnimator = ValueAnimator.ofInt(originalHeight, 0);
        heightAnimator.addUpdateListener(new HeightUpdater(view));

        ValueAnimator[] customAnimators = getAdditionalAnimators(view, position, parent);
        ValueAnimator[] animators = new ValueAnimator[customAnimators.length + 1];
        animators[0] = heightAnimator;
        System.arraycopy(customAnimators, 0, animators, 1, customAnimators.length);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setDuration(mInsertionAnimationDurationMs);
        animatorSet.addListener(new ShrinkToRemoveAnimationListener(position));
        
        mRemovedViews.add(view);
        mActiveRemoveCount++;
        return animatorSet;
    }

    /**
     * Override this method to provide additional animators on top of the default height and alpha animation.
     *
     * @param view   The {@link android.view.View} that will get animated.
     * @param position position in adapter
     * @param parent The parent that this view will eventually be attached to.
     *
     * @return a non-null array of Animators.
     */
    @SuppressWarnings({"MethodMayBeStatic", "UnusedParameters"})
    @NonNull
    protected ValueAnimator[] getAdditionalAnimators(@NonNull final View view, final int position, @NonNull final ViewGroup parent) {
        return new ValueAnimator[]{};
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
            if (layoutParams == null) return;
            layoutParams.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(layoutParams);
        }
    }

    /**
     *
     * @param removedPositions the positions that have been dismissed.
     */
    protected void handleRemoval(@NonNull final List<Integer> removedPositions) {
        if (!removedPositions.isEmpty()) {
            Collections.sort(removedPositions, Collections.reverseOrder());

            int[] removePositions = new int[removedPositions.size()];
            int i = 0;
            for (Integer removedPosition : removedPositions) {
                if (removedPosition < mRemovable.getCount()) {
                    removePositions[i] = removedPosition;
                    mRemovable.remove(removedPosition);
                    i++;
                }
            }
        }
    }

    /**
     * If necessary, remove removed object from the adapter,
     * and restores the {@link android.view.View} presentations.
     */
    protected void finalizeRemove() {
        if (mActiveRemoveCount == 0) {
            restoreViewPresentations(mRemovedViews);
            handleRemoval(mRemovedPositions);

            mRemovedViews.clear();
            mRemovedPositions.clear();
        }
    }

    /**
     * Restores the presentation of given {@link android.view.View}s by calling {@link #restoreViewPresentation(android.view.View)}.
     */
    protected void restoreViewPresentations(@NonNull final Iterable<View> views) {
        for (View view : views) {
            restoreViewPresentation(view);
        }
    }

    protected void restoreViewPresentation(@NonNull final View view) {
        if (view instanceof DynamicListItemView) {
            View containerView = ((DynamicListItemView) view).getContainerView();
            ViewGroup.LayoutParams params = containerView.getLayoutParams();
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            containerView.setLayoutParams(params);
        }
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        view.setLayoutParams(layoutParams);
    }

    private void handleRemoveItem(final int position) {
        mRemovable.remove(position);
    }

    protected void directRemove(final int position) {
        mRemovedPositions.add(position);
        finalizeRemove();
    }

    /**
     * A class which removes the active index from the {@code InsertQueue} when the animation has finished.
     */
    private class ExpandAnimationListener extends AnimatorListenerAdapter {

        protected final int mPosition;
        protected final View mView;

        ExpandAnimationListener(final int position, final View view) {
            mPosition = position;
            mView = view;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            //restore view height to wrap_content
            restoreViewPresentation(mView);
            mInsertQueue.removeActiveIndex(mPosition);
        }
    }

    private class ShrinkToRemoveAnimationListener extends AnimatorListenerAdapter {

        protected final int mPosition;
        ShrinkToRemoveAnimationListener(final int position) {
            mPosition = position;
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            super.onAnimationEnd(animation);
            mActiveRemoveCount--;
            finalizeRemove();
        }
    }
}
