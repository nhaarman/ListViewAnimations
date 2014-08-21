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
package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissTouchListener;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * A {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissTouchListener} that adds an undo stage to the item swiping.
 */
public class SwipeUndoTouchListener extends SwipeDismissTouchListener {

    private static final String ALPHA = "alpha";

    private static final String TRANSLATION_X = "translationX";

    /**
     * The callback which gets notified of events.
     */
    @NonNull
    private final UndoCallback mCallback;

    /**
     * The positions that are in the undo state.
     */
    @NonNull
    private final Collection<Integer> mUndoPositions = new LinkedList<>();

    /**
     * The {@link android.view.View}s that are in the undo state.
     */
    @NonNull
    private final Map<Integer, View> mUndoViews = new HashMap<>();

    /**
     * The positions that have been dismissed.
     */
    @NonNull
    private final List<Integer> mDismissedPositions = new LinkedList<>();

    /**
     * The {@link android.view.View}s that have been dismissed.
     */
    @NonNull
    private final Collection<View> mDismissedViews = new LinkedList<>();

    public SwipeUndoTouchListener(@NonNull final ListViewWrapper listViewWrapper, @NonNull final UndoCallback callback) {
        super(listViewWrapper, callback);
        mCallback = callback;
    }

    @Override
    protected boolean willLeaveDataSetOnFling(@NonNull final View view, final int position) {
        return mUndoPositions.contains(position);
    }

    @Override
    protected void afterViewFling(@NonNull final View view, final int position) {
        if (mUndoPositions.contains(position)) {
            mUndoPositions.remove(position);
            mUndoViews.remove(position);
            performDismiss(view, position);
            hideUndoView(view);
        } else {
            mUndoPositions.add(position);
            mUndoViews.put(position, view);
            mCallback.onUndoShown(view, position);
            showUndoView(view);
            restoreViewPresentation(view);
        }
    }

    @Override
    protected void afterCancelSwipe(@NonNull final View view, final int position) {
        finalizeDismiss();
    }

    /**
     * Animates the dismissed list item to zero-height and fires the dismiss callback when all dismissed list item animations have completed.
     *
     * @param view the dismissed {@link android.view.View}.
     */
    @Override
    protected void performDismiss(@NonNull final View view, final int position) {
        super.performDismiss(view, position);

        mDismissedViews.add(view);
        mDismissedPositions.add(position);

        mCallback.onDismiss(view, position);
    }

    public boolean hasPendingItems() {
        return !mUndoPositions.isEmpty();
    }

    /**
     * Dismisses all items that are in the undo state.
     */
    public void dimissPending() {
        for (int position : mUndoPositions) {
            performDismiss(mUndoViews.get(position), position);
        }
    }

    /**
     * Sets the visibility of the primary {@link android.view.View} to {@link android.view.View#GONE}, and animates the undo {@code View} in to view.
     *
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     */
    private void showUndoView(@NonNull final View view) {
        mCallback.getPrimaryView(view).setVisibility(View.GONE);

        View undoView = mCallback.getUndoView(view);
        undoView.setVisibility(View.VISIBLE);
        ObjectAnimator.ofFloat(undoView, ALPHA, 0f, 1f).start();
    }

    /**
     * Sets the visibility of the primary {@link android.view.View} to {@link android.view.View#VISIBLE}, and that of the undo {@code View} to {@link android.view.View#GONE}.
     *
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     */
    private void hideUndoView(@NonNull final View view) {
        mCallback.getPrimaryView(view).setVisibility(View.VISIBLE);
        mCallback.getUndoView(view).setVisibility(View.GONE);
    }


    /**
     * If necessary, notifies the {@link UndoCallback} to remove dismissed object from the adapter,
     * and restores the {@link android.view.View} presentations.
     */
    @Override
    protected void finalizeDismiss() {
        if (getActiveDismissCount() == 0 && getActiveSwipeCount() == 0) {
            restoreViewPresentations(mDismissedViews);
            notifyCallback(mDismissedPositions);

            Collection<Integer> newUndoPositions = Util.processDeletions(mUndoPositions, mDismissedPositions);
            mUndoPositions.clear();
            mUndoPositions.addAll(newUndoPositions);

            mDismissedViews.clear();
            mDismissedPositions.clear();
        }
    }

    /**
     * Restores the height of given {@code View}.
     * Also calls its super implementation.
     */
    @Override
    protected void restoreViewPresentation(@NonNull final View view) {
        super.restoreViewPresentation(view);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 0;
        view.setLayoutParams(layoutParams);
    }

    /**
     * Performs the undo animation and restores the original state for given {@link android.view.View}.
     *
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     */
    public void undo(@NonNull final View view) {
        int position = AdapterViewUtil.getPositionForView(getListViewWrapper(), view);
        mUndoPositions.remove(position);

        View primaryView = mCallback.getPrimaryView(view);
        View undoView = mCallback.getUndoView(view);

        primaryView.setVisibility(View.VISIBLE);

        ObjectAnimator undoAlphaAnimator = ObjectAnimator.ofFloat(undoView, ALPHA, 1f, 0f);
        ObjectAnimator primaryAlphaAnimator = ObjectAnimator.ofFloat(primaryView, ALPHA, 0f, 1f);
        ObjectAnimator primaryXAnimator = ObjectAnimator.ofFloat(primaryView, TRANSLATION_X, primaryView.getWidth(), 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(undoAlphaAnimator, primaryAlphaAnimator, primaryXAnimator);
        animatorSet.addListener(new UndoAnimatorListener(undoView));
        animatorSet.start();

        mCallback.onUndo(view, position);
    }

    @Override
    protected void directDismiss(final int position) {
        mDismissedPositions.add(position);
        finalizeDismiss();
    }

    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} which finalizes the undo when the animation is finished.
     */
    private class UndoAnimatorListener extends AnimatorListenerAdapter {

        @NonNull
        private final View mUndoView;

        UndoAnimatorListener(@NonNull final View undoView) {
            mUndoView = undoView;
        }

        @Override
        public void onAnimationEnd(@NonNull final Animator animation) {
            mUndoView.setVisibility(View.GONE);
            finalizeDismiss();
        }
    }
}
