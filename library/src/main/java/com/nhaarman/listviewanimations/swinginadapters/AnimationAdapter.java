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
package com.nhaarman.listviewanimations.swinginadapters;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * A {@link BaseAdapterDecorator} class which applies multiple {@link Animator}s at once to views when they are first shown. The Animators applied include the animations specified
 * in {@link #getAnimators(ViewGroup, View)}, plus an alpha transition.
 */
public abstract class AnimationAdapter extends BaseAdapterDecorator {

    /* Key values for saving instance states */
    private static final String SAVEDINSTANCESTATE_FIRSTANIMATEDPOSITION = "savedinstancestate_firstanimatedposition";
    private static final String SAVEDINSTANCESTATE_LASTANIMATEDPOSITION = "savedinstancestate_lastanimatedposition";
    private static final String SAVEDINSTANCESTATE_SHOULDANIMATE = "savedinstancestate_shouldanimate";

    private static final String ALPHA = "alpha";

    /**
     * The default delay in millis before the first animation starts.
     */
    private static final long INITIAL_DELAY_MILLIS = 150;

    /**
     * The default delay in millis between view animations.
     */
    private static final long DEFAULT_ANIMATION_DELAY_MILLIS = 100;

    /**
     * The default duration in millis of the animations.
     */
    private static final long DEFAULT_ANIMATION_DURATION_MILLIS = 300;

    /**
     * The active Animators. Keys are hashcodes of the Views that are animated.
     */
    private final SparseArray<Animator> mAnimators = new SparseArray<>();

    /**
     * Whether this instance does not wrap another AnimationAdapter. When this is set to false, animation is not applied to the views,
     * since the wrapped AnimationAdapter will take care of that.
     */
    private final boolean mIsRootAnimationAdapter;

    /**
     * The delay in millis before the first animation starts.
     */
    private long mInitialDelayMillis = INITIAL_DELAY_MILLIS;

    /**
     * The delay in millis between view animations.
     */
    private long mAnimationDelayMillis = DEFAULT_ANIMATION_DELAY_MILLIS;

    /**
     * The duration in millis of the animations.
     */
    private long mAnimationDurationMillis = DEFAULT_ANIMATION_DURATION_MILLIS;

    /**
     * The start timestamp of the first animation, as returned by {@link android.os.SystemClock#uptimeMillis()}.
     */
    private long mAnimationStartMillis;

    /**
     * The position of the item that is the first that was animated.
     */
    private int mFirstAnimatedPosition;

    /**
     * The position of the last item that was animated.
     */
    private int mLastAnimatedPosition;

    /**
     * Whether animation is enabled. When this is set to false, no animation is applied to the views.
     */
    private boolean mShouldAnimate = true;

    /**
     * When the AbsListView instance is an instance of GridView, this boolean represents whether it has measured it first item yet. When this is set to true,
     * animation is not applied to the view, as another call to getView will be made for the same position.
     */
    private boolean mIsMeasuringGridView = true;

    /**
     * Creates a new AnimationAdapter, wrapping given BaseAdapter.
     * @param baseAdapter the BaseAdapter to wrap.
     */
    protected AnimationAdapter(final BaseAdapter baseAdapter) {
        super(baseAdapter);

        mAnimationStartMillis = -1;
        mFirstAnimatedPosition = -1;
        mLastAnimatedPosition = -1;
        mIsMeasuringGridView = true;

        mIsRootAnimationAdapter = !(baseAdapter instanceof AnimationAdapter);
    }

    /**
     * Call this method to reset animation status on all views. The next time {@link #notifyDataSetChanged()} is called on the base adapter, all views will animate again. Will also
     * call {@link #setShouldAnimate(boolean)} with a value of true.
     */
    public void reset() {
        mAnimators.clear();
        mFirstAnimatedPosition = -1;
        mLastAnimatedPosition = -1;
        mAnimationStartMillis = -1;
        mShouldAnimate = true;
        mIsMeasuringGridView = true;

        if (getDecoratedBaseAdapter() instanceof AnimationAdapter) {
            ((AnimationAdapter) getDecoratedBaseAdapter()).reset();
        }
    }

    /**
     * Set whether to animate the {@link View}s or not.
     *
     * @param shouldAnimate true if the Views should be animated.
     */
    public void setShouldAnimate(final boolean shouldAnimate) {
        mShouldAnimate = shouldAnimate;
    }

    /**
     * Set the starting position for which items should animate. Given position will animate as well. Will also call setShouldAnimate(true).
     *
     * @param position the position.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setShouldAnimateFromPosition(final int position) {
        mShouldAnimate = true;
        mFirstAnimatedPosition = position - 1;
        mLastAnimatedPosition = position - 1;
    }

    /**
     * Set the starting position for which items should animate as the first position which isn't currently visible on screen. This call is also valid when the {@link View}s
     * haven't been drawn yet. Will also call setShouldAnimate(true).
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setShouldAnimateNotVisible() {
        if (getAbsListView() == null) {
            throw new IllegalStateException("Call setListView() on this AnimationAdapter before setShouldAnimateNotVisible()!");
        }

        mShouldAnimate = true;
        mFirstAnimatedPosition = getAbsListView().getLastVisiblePosition();
        mLastAnimatedPosition = getAbsListView().getLastVisiblePosition();
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        if (mIsRootAnimationAdapter) {
            if (getAbsListView() == null) {
                throw new IllegalStateException("Call setAbsListView() on this AnimationAdapter before setAdapter()!");
            }

            if (convertView != null) {
                cancelExistingAnimation(convertView);
            }
        }

        View itemView = super.getView(position, convertView, parent);

        if (mIsRootAnimationAdapter) {
            animateViewIfNecessary(position, itemView, parent);
        }
        return itemView;
    }

    private void cancelExistingAnimation(final View convertView) {
        int hashCode = convertView.hashCode();
        Animator animator = mAnimators.get(hashCode);
        if (animator != null) {
            animator.end();
            mAnimators.remove(hashCode);
        }
    }

    private void animateViewIfNecessary(final int position, final View view, final ViewGroup parent) {
        /* GridView measures the first View which is returned by getView(int, View, ViewGroup), but does not use that View. */
        boolean isMeasuringGridViewItem = getAbsListView() instanceof GridView && mIsMeasuringGridView;
        mIsMeasuringGridView = false;

        if (position > mLastAnimatedPosition && mShouldAnimate && !isMeasuringGridViewItem) {
            if (mFirstAnimatedPosition == -1) {
                mFirstAnimatedPosition = position;
            }

            animateView(parent, view);
            mLastAnimatedPosition = position;
        }
    }

    private void animateView(final ViewGroup parent, final View view) {
        if (mAnimationStartMillis == -1) {
            mAnimationStartMillis = SystemClock.uptimeMillis();
        }

        ViewHelper.setAlpha(view, 0);

        Animator[] childAnimators;
        if (getDecoratedBaseAdapter() instanceof AnimationAdapter) {
            childAnimators = ((AnimationAdapter) getDecoratedBaseAdapter()).getAnimators(parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(parent, view);
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(concatAnimators(childAnimators, animators, alphaAnimator));
        set.setStartDelay(calculateAnimationDelay());
        set.setDuration(mAnimationDurationMillis);
        set.start();

        mAnimators.put(view.hashCode(), set);
    }

    @SuppressLint("NewApi")
    private long calculateAnimationDelay() {
        long delay;

        int lastVisiblePosition = getAbsListView().getLastVisiblePosition();
        int firstVisiblePosition = getAbsListView().getFirstVisiblePosition();

        int numberOfItemsOnScreen = lastVisiblePosition - firstVisiblePosition;
        int numberOfAnimatedItems = mLastAnimatedPosition - mFirstAnimatedPosition;

        if (numberOfItemsOnScreen + 1 < numberOfAnimatedItems) {
            delay = mAnimationDelayMillis;

            if (getAbsListView() instanceof GridView && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                delay += mAnimationDelayMillis * ((mLastAnimatedPosition + 1) % ((GridView) getAbsListView()).getNumColumns());
            }
        } else {
            long delaySinceStart = (mLastAnimatedPosition - mFirstAnimatedPosition + 1) * mAnimationDelayMillis;
            delay = mAnimationStartMillis + mInitialDelayMillis + delaySinceStart - SystemClock.uptimeMillis();
        }
        return Math.max(0, delay);
    }

    /**
     * Sets the delay in milliseconds before the first animation should start. Defaults to {@value #INITIAL_DELAY_MILLIS}.
     *
     * @param delayMillis the time in milliseconds.
     */
    public void setInitialDelayMillis(final long delayMillis) {
        mInitialDelayMillis = delayMillis;
    }

    /**
     * Sets the delay in milliseconds before an animation of a view should start. Defaults to {@value #DEFAULT_ANIMATION_DELAY_MILLIS}.
     *
     * @param delayMillis the time in milliseconds.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAnimationDelayMillis(final long delayMillis) {
        mAnimationDelayMillis = delayMillis;
    }

    /**
     * Sets the duration of the animation in milliseconds. Defaults to {@value #DEFAULT_ANIMATION_DURATION_MILLIS}.
     *
     * @param durationMillis the time in milliseconds.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAnimationDurationMillis(final long durationMillis) {
        mAnimationDurationMillis = durationMillis;
    }

    /**
     * Returns the Animators to apply to the views. In addition to the returned Animators, an alpha transition will be applied to the view.
     *
     * @param parent The parent of the view
     * @param view   The view that will be animated, as retrieved by getView().
     */
    protected abstract Animator[] getAnimators(ViewGroup parent, View view);

    /**
     * Returns a Parcelable object containing the AnimationAdapter's current dynamic state.
     */
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();

        bundle.putInt(SAVEDINSTANCESTATE_FIRSTANIMATEDPOSITION, mFirstAnimatedPosition);
        bundle.putInt(SAVEDINSTANCESTATE_LASTANIMATEDPOSITION, mLastAnimatedPosition);
        bundle.putBoolean(SAVEDINSTANCESTATE_SHOULDANIMATE, mShouldAnimate);

        return bundle;
    }

    /**
     * Restores this AnimationAdapter's state.
     *
     * @param parcelable the Parcelable object previously returned by {@link #onSaveInstanceState()}.
     */
    public void onRestoreInstanceState(final Parcelable parcelable) {
        if (parcelable instanceof Bundle) {
            Bundle bundle = (Bundle) parcelable;
            mFirstAnimatedPosition = bundle.getInt(SAVEDINSTANCESTATE_FIRSTANIMATEDPOSITION);
            mLastAnimatedPosition = bundle.getInt(SAVEDINSTANCESTATE_LASTANIMATEDPOSITION);
            mShouldAnimate = bundle.getBoolean(SAVEDINSTANCESTATE_SHOULDANIMATE);
        }
    }

    private static Animator[] concatAnimators(final Animator[] childAnimators, final Animator[] animators, final Animator alphaAnimator) {
        Animator[] allAnimators = new Animator[childAnimators.length + animators.length + 1];
        int i;

        for (i = 0; i < animators.length; ++i) {
            allAnimators[i] = animators[i];
        }

        for (Animator childAnimator : childAnimators) {
            allAnimators[i] = childAnimator;
            ++i;
        }

        allAnimators[allAnimators.length - 1] = alphaAnimator;
        return allAnimators;
    }
}
