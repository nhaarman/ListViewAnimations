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
 * A {@link BaseAdapterDecorator} class which applies multiple {@link Animator}s at once to
 * views when they are first shown. The Animators applied include the animations
 * specified in {@link #getAnimators(ViewGroup, View)}, plus an alpha transition.
 */
public abstract class AnimationAdapter extends BaseAdapterDecorator {

    protected static final long DEFAULTANIMATIONDELAYMILLIS = 100;
    protected static final long DEFAULTANIMATIONDURATIONMILLIS = 300;
    private static final long INITIALDELAYMILLIS = 150;
    private static final String ALPHA = "alpha";

    private final SparseArray<Animator> mAnimators;
    private long mAnimationStartMillis;
    private int mFirstAnimatedPosition;
    private int mLastAnimatedPosition;
    private boolean mHasParentAnimationAdapter;
    private boolean mShouldAnimate = true;

    private long mInitialDelayMillis = INITIALDELAYMILLIS;
    private long mAnimationDelayMillis = DEFAULTANIMATIONDELAYMILLIS;
    private long mAnimationDurationMillis = DEFAULTANIMATIONDURATIONMILLIS;

    public AnimationAdapter(final BaseAdapter baseAdapter) {
        super(baseAdapter);
        mAnimators = new SparseArray<Animator>();

        mAnimationStartMillis = -1;
        mFirstAnimatedPosition = -1;
        mLastAnimatedPosition = -1;

        if (baseAdapter instanceof AnimationAdapter) {
            ((AnimationAdapter) baseAdapter).setHasParentAnimationAdapter(true);
        }
    }

    /**
     * Call this method to reset animation status on all views. The next time
     * {@link #notifyDataSetChanged()} is called on the base adapter, all views will
     * animate again. Will also call {@link #setShouldAnimate(boolean)} with a value of true.
     */
    public void reset() {
        mAnimators.clear();
        mFirstAnimatedPosition = -1;
        mLastAnimatedPosition = -1;
        mAnimationStartMillis = -1;
        mShouldAnimate = true;

        if (getDecoratedBaseAdapter() instanceof AnimationAdapter) {
            ((AnimationAdapter) getDecoratedBaseAdapter()).reset();
        }
    }

    /**
     * Set whether to animate the {@link View}s or not.
     * @param shouldAnimate true if the Views should be animated.
     */
    public void setShouldAnimate(final boolean shouldAnimate) {
        mShouldAnimate = shouldAnimate;
    }

    /**
     * Set the starting position for which items should animate. Given position will animate as well.
     * Will also call setShouldAnimate(true).
     * @param position the position.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setShouldAnimateFromPosition(final int position) {
        mShouldAnimate = true;
        mFirstAnimatedPosition = position - 1;
        mLastAnimatedPosition = position - 1;
    }

    /**
     * Set the starting position for which items should animate as the first position which isn't currently visible on screen.
     * This call is also valid when the {@link View}s haven't been drawn yet.
     * Will also call setShouldAnimate(true).
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
        if (!mHasParentAnimationAdapter) {
            if (getAbsListView() == null) {
                throw new IllegalStateException("Call setListView() on this AnimationAdapter before setAdapter()!");
            }

            if (convertView != null) {
                cancelExistingAnimation(convertView);
            }
        }

        View itemView = super.getView(position, convertView, parent);

        if (!mHasParentAnimationAdapter) {
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
        boolean isMeasuringGridViewItem = getAbsListView() instanceof GridView && parent.getHeight() == 0;

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
            mAnimationStartMillis = System.currentTimeMillis();
        }

        ViewHelper.setAlpha(view, 0);

        Animator[] childAnimators;
        if (mDecoratedBaseAdapter instanceof AnimationAdapter) {
            childAnimators = ((AnimationAdapter) mDecoratedBaseAdapter).getAnimators(parent, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(parent, view);
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

        AnimatorSet set = new AnimatorSet();
        set.playTogether(concatAnimators(childAnimators, animators, alphaAnimator));
        set.setStartDelay(calculateAnimationDelay());
        set.setDuration(getAnimationDurationMillis());
        set.start();

        mAnimators.put(view.hashCode(), set);
    }

    private Animator[] concatAnimators(final Animator[] childAnimators, final Animator[] animators, final Animator alphaAnimator) {
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

    @SuppressLint("NewApi")
    private long calculateAnimationDelay() {
        long delay;

        int lastVisiblePosition = getAbsListView().getLastVisiblePosition();
        int firstVisiblePosition = getAbsListView().getFirstVisiblePosition();

        int numberOfItemsOnScreen = lastVisiblePosition - firstVisiblePosition;
        int numberOfAnimatedItems = mLastAnimatedPosition - mFirstAnimatedPosition;

        if (numberOfItemsOnScreen + 1 < numberOfAnimatedItems) {
            delay = getAnimationDelayMillis();

            if (getAbsListView() instanceof GridView && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                delay += getAnimationDelayMillis() * ((mLastAnimatedPosition + 1) % ((GridView) getAbsListView()).getNumColumns());
            }
        } else {
            long delaySinceStart = (mLastAnimatedPosition - mFirstAnimatedPosition + 1) * getAnimationDelayMillis();
            delay = mAnimationStartMillis + getInitialDelayMillis() + delaySinceStart - System.currentTimeMillis();
        }
        return Math.max(0, delay);
    }

    /**
     * Set whether this AnimationAdapter is encapsulated by another
     * AnimationAdapter. When this is set to true, this AnimationAdapter does
     * not apply any animations to the views. Should not be set explicitly, the
     * AnimationAdapter class manages this by itself.
     */
    public void setHasParentAnimationAdapter(final boolean hasParentAnimationAdapter) {
        mHasParentAnimationAdapter = hasParentAnimationAdapter;
    }

    /**
     * Get the delay in milliseconds before the first animation should start. Defaults to {@value #INITIALDELAYMILLIS}.
     */
    protected long getInitialDelayMillis() {
        return mInitialDelayMillis;
    }

    /**
     * Set the delay in milliseconds before the first animation should start. Defaults to {@value #INITIALDELAYMILLIS}.
     * @param delayMillis the time in milliseconds.
     */
    public void setInitialDelayMillis(final long delayMillis) {
        mInitialDelayMillis = delayMillis;
    }

    /**
     * Get the delay in milliseconds before an animation of a view should start. Defaults to {@value #DEFAULTANIMATIONDELAYMILLIS}.
     */
    protected long getAnimationDelayMillis() {
        return mAnimationDelayMillis;
    }

    /**
     * Set the delay in milliseconds before an animation of a view should start. Defaults to {@value #DEFAULTANIMATIONDELAYMILLIS}.
     * @param delayMillis the time in milliseconds.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAnimationDelayMillis(final long delayMillis) {
        mAnimationDelayMillis = delayMillis;
    }

    /**
     * Get the duration of the animation in milliseconds. Defaults to {@value #DEFAULTANIMATIONDURATIONMILLIS}.
     */
    protected long getAnimationDurationMillis() {
        return mAnimationDurationMillis;
    }

    /**
     * Set the duration of the animation in milliseconds. Defaults to {@value #DEFAULTANIMATIONDURATIONMILLIS}.
     * @param durationMillis the time in milliseconds.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void setAnimationDurationMillis(final long durationMillis) {
        mAnimationDurationMillis = durationMillis;
    }

    /**
     * Get the Animators to apply to the views. In addition to the returned
     * Animators, an alpha transition will be applied to the view.
     *
     * @param parent
     *            The parent of the view
     * @param view
     *            The view that will be animated, as retrieved by getView()
     */
    public abstract Animator[] getAnimators(ViewGroup parent, View view);
}
