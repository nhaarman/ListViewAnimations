package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SwipeDismissTouchListener extends SwipeTouchListener {

    /**
     * The callback which gets notified of dismissed items.
     */
    private final OnDismissCallback mCallback;

    /**
     * The duration of the fling animation.
     */
    private final long mAnimationTime;

    /**
     * The {@link View}s that have been dismissed.
     */
    private final Collection<View> mDismissedViews = new ArrayList<View>();

    /**
     * The dismissed positions.
     */
    private final List<Integer> mDismissedPositions = new ArrayList<Integer>();

    /**
     * The number of active dismiss animations.
     */
    private int mActiveDismissCount;

    /**
     * Constructs a new {@code SwipeDismissTouchListener} for the given {@link android.widget.AbsListView}.
     *  @param absListView
     *            The {@code AbsListView} whose items should be dismissable.
     * @param callback
     *            The callback to trigger when the user has indicated that he
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    public SwipeDismissTouchListener(final AbsListView absListView, final OnDismissCallback callback) {
        super(absListView);
        mCallback = callback;
        mAnimationTime = absListView.getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
    }

    @Override
    protected void afterCancelSwipe(final View view, final int position) {
        finalizeDismiss();
    }

    @Override
    protected void afterViewFling(final View view, final int position) {
        performDismiss(view, position);
    }

    /**
     * Animates the dismissed list item to zero-height and fires the dismiss callback when all dismissed list item animations have completed.
     * @param view the dismissed {@link View}.
     */
    private void performDismiss(final View view, final int position) {
        Log.v("SDTL", "Performing dismiss - " + position);

        mDismissedViews.add(view);
        mDismissedPositions.add(position);

        ValueAnimator animator = ValueAnimator.ofInt(view.getHeight(), 1).setDuration(mAnimationTime);
        animator.addUpdateListener(new DismissAnimatorUpdateListener(view));
        animator.addListener(new DismissAnimatorListener());
        animator.start();

        mActiveDismissCount++;
    }

    private void finalizeDismiss() {
        if (mActiveDismissCount == 0 && getActiveSwipeCount() == 0) {
            restoreViewPresentations();
            notifyCallback();

            mDismissedViews.clear();
            mDismissedPositions.clear();
        }
    }

    private void notifyCallback() {
        if (!mDismissedPositions.isEmpty()) {
            Collections.sort(mDismissedPositions, Collections.reverseOrder());

            int[] dismissPositions = new int[mDismissedPositions.size()];
            for (int i = 0; i < mDismissedPositions.size(); i++) {
                dismissPositions[i] = mDismissedPositions.get(i);
            }
            mCallback.onDismiss(getAbsListView(), dismissPositions);
        }
    }

    private void restoreViewPresentations() {
        for (View view : mDismissedViews) {
            restoreViewPresentation(view);
        }
    }

    @Override
    protected void restoreViewPresentation(final View view) {
        super.restoreViewPresentation(view);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = 0;
        view.setLayoutParams(layoutParams);
    }

    /**
     * An {@link com.nineoldandroids.animation.ValueAnimator.AnimatorUpdateListener} which applies height animation to given {@link View}.
     */
    private static class DismissAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        private final View mView;

        DismissAnimatorUpdateListener(final View view) {
            mView = view;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            ViewGroup.LayoutParams layoutParams = mView.getLayoutParams();
            layoutParams.height = (Integer) animation.getAnimatedValue();
            mView.setLayoutParams(layoutParams);
        }
    }

    private class DismissAnimatorListener extends AnimatorListenerAdapter {

        @Override
        public void onAnimationEnd(final Animator animation) {
            mActiveDismissCount--;
            finalizeDismiss();
        }
    }
}
