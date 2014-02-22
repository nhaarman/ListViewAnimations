/*
 * Copyright 2013 Frankie Sardo
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Warning: a stable id for each item in the adapter is required. The decorated
 * adapter should not try to cast convertView to a particular view. The
 * undoLayout should have the same height as the content row.
 * <p>
 * Usage: <br>
 * * Create a new instance of this class providing the {@link BaseAdapter} to wrap, the undo layout, the undo button id and a {@link DeleteItemCallback}, optionally a delay time millis,
 * a count down TextView res id,
 * , a delay in milliseconds before deleting the item.<br>
 * * Set your {@link ListView} to this ContextualUndoAdapter, and set this ContextualUndoAdapter to your ListView.<br>
 */
@SuppressWarnings("UnusedDeclaration")
public class ContextualUndoAdapter extends BaseAdapterDecorator implements ContextualUndoListViewTouchListener.Callback {

    private static final int ANIMATION_DURATION = 150;
    private static final String EXTRA_ACTIVE_REMOVED_ID = "removedId";
    private static final String X = "x";

    private final int mUndoLayoutId;
    private final int mUndoActionId;
    private final int mCountDownTextViewResId;
    private final int mAutoDeleteDelayMillis;

    private long mDismissStartMillis;

    private ContextualUndoView mCurrentRemovedView;
    private long mCurrentRemovedId;

    private final Handler mHandler;
    private final CountDownRunnable mCountDownRunnable;

    private final DeleteItemCallback mDeleteItemCallback;
    private final CountDownFormatter mCountDownFormatter;

    private ContextualUndoListViewTouchListener mContextualUndoListViewTouchListener;

    /**
     * Create a new ContextualUndoAdapter based on given parameters.
     *
     * @param baseAdapter  The {@link BaseAdapter} to wrap
     * @param undoLayoutId The layout resource id to show as undo
     * @param undoActionId The id of the component which undoes the dismissal
     * @param deleteItemCallback The {@link DeleteItemCallback} which is called when an item should be deleted from your collection.
     */
    public ContextualUndoAdapter(final BaseAdapter baseAdapter, final int undoLayoutId, final int undoActionId, final DeleteItemCallback deleteItemCallback) {
        this(baseAdapter, undoLayoutId, undoActionId, -1, -1, deleteItemCallback, null);
    }

    /**
     * Create a new ContextualUndoAdapter based on given parameters.
     * Will automatically remove the swiped item after autoDeleteTimeMillis milliseconds.
     *
     * @param baseAdapter  The {@link BaseAdapter} to wrap
     * @param undoLayoutResId The layout resource id to show as undo
     * @param undoActionResId The id of the component which undoes the dismissal
     * @param autoDeleteTimeMillis The time in milliseconds that the adapter will wait for he user to hit undo before automatically deleting the item
     * @param deleteItemCallback The {@link DeleteItemCallback} which is called when an item should be deleted from your collection.
     */
    public ContextualUndoAdapter(final BaseAdapter baseAdapter, final int undoLayoutResId, final int undoActionResId, final int autoDeleteTimeMillis, final DeleteItemCallback deleteItemCallback) {
        this(baseAdapter, undoLayoutResId, undoActionResId, autoDeleteTimeMillis, -1, deleteItemCallback, null);
    }

    /**
     * Create a new ContextualUndoAdapter based on given parameters.
     * Will automatically remove the swiped item after autoDeleteTimeMillis milliseconds.
     *
     * @param baseAdapter  The {@link BaseAdapter} to wrap
     * @param undoLayoutResId The layout resource id to show as undo
     * @param undoActionResId The resource id of the component which undoes the dismissal
     * @param autoDeleteTime The time in milliseconds that adapter will wait for user to hit undo before automatically deleting item
     * @param countDownTextViewResId The resource id of the {@link TextView} in the undoLayoutResId that will show the time left
     * @param deleteItemCallback The {@link DeleteItemCallback} which is called when an item should be deleted from your collection.
     * @param countDownFormatter The {@link CountDownFormatter} which provides text to be shown in the {@link TextView} as specified by countDownTextViewResId
     */
    public ContextualUndoAdapter(final BaseAdapter baseAdapter, final int undoLayoutResId, final int undoActionResId, final int autoDeleteTime, final int countDownTextViewResId,
                                 final DeleteItemCallback deleteItemCallback, final CountDownFormatter countDownFormatter) {
        super(baseAdapter);

        mHandler = new Handler();
        mCountDownRunnable = new CountDownRunnable();

        mUndoLayoutId = undoLayoutResId;
        mUndoActionId = undoActionResId;
        mCurrentRemovedId = -1;
        mAutoDeleteDelayMillis = autoDeleteTime;
        mCountDownTextViewResId = countDownTextViewResId;

        mDeleteItemCallback = deleteItemCallback;
        mCountDownFormatter = countDownFormatter;
    }

    @Override
    public final View getView(final int position, final View convertView, final ViewGroup parent) {
        final ViewHolder vh;
        ContextualUndoView contextualUndoView = (ContextualUndoView) convertView;
        if (contextualUndoView == null) {
            contextualUndoView = new ContextualUndoView(parent.getContext(), mUndoLayoutId, mCountDownTextViewResId);
            contextualUndoView.findViewById(mUndoActionId).setOnClickListener(new UndoListener(contextualUndoView));
            vh = new ViewHolder(contextualUndoView);
        } else {
            vh = ViewHolder.getViewHolder(contextualUndoView);
        }

        View contentView = super.getView(position, contextualUndoView.getContentView(), contextualUndoView);
        contextualUndoView.updateContentView(contentView);

        long itemId = getItemId(position);
        vh.mItemId = itemId;

        if (itemId == mCurrentRemovedId) {
            contextualUndoView.displayUndo();
            long millisLeft = mAutoDeleteDelayMillis - (System.currentTimeMillis() - mDismissStartMillis);
            if (mCountDownFormatter != null) {
                contextualUndoView.updateCountDownTimer(mCountDownFormatter.getCountDownString(millisLeft));
            }
        } else {
            contextualUndoView.displayContentView();
        }

        contextualUndoView.setItemId(itemId);
        return contextualUndoView;
    }

    @Override
    public void setAbsListView(final AbsListView listView) {
        super.setAbsListView(listView);
        mContextualUndoListViewTouchListener = new ContextualUndoListViewTouchListener(listView, this);
        mContextualUndoListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer());
        mContextualUndoListViewTouchListener.setTouchChild(getTouchChild());
        listView.setOnTouchListener(mContextualUndoListViewTouchListener);
        listView.setOnScrollListener(mContextualUndoListViewTouchListener.makeScrollListener());
        listView.setOnHierarchyChangeListener(new HierarchyChangeListener());
    }

    @Override
    public void onViewSwiped(final long dismissViewItemId, final int dismissPosition) {
        ContextualUndoView contextualUndoView = getContextualUndoView(dismissViewItemId);
        if (contextualUndoView == null) {
            removePreviousContextualUndoIfPresent();
            mCurrentRemovedView = null;
            mCurrentRemovedId = dismissViewItemId;
        } else if (contextualUndoView.isContentDisplayed()) {
            restoreViewPosition(contextualUndoView);
            contextualUndoView.displayUndo();
            removePreviousContextualUndoIfPresent();
            setCurrentRemovedView(contextualUndoView);

            if (mAutoDeleteDelayMillis > 0) {
                startAutoDeleteTimer();
            }
        } else {
            performRemovalIfNecessary();
        }
    }

    private ContextualUndoView getContextualUndoView(final long dismissViewItemId) {
        ContextualUndoView contextualUndoView = null;

        AbsListView listView = getAbsListView();
        int childCount = listView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = listView.getChildAt(i);
            if (child instanceof ContextualUndoView) {
                ContextualUndoView listItem = (ContextualUndoView) child;
                if (listItem.getItemId() == dismissViewItemId) {
                    contextualUndoView = listItem;
                }
            }
        }
        return contextualUndoView;
    }

    private void startAutoDeleteTimer() {
        mHandler.removeCallbacks(mCountDownRunnable);

        if (mCountDownFormatter != null) {
            mCurrentRemovedView.updateCountDownTimer(mCountDownFormatter.getCountDownString(mAutoDeleteDelayMillis));
        }

        mDismissStartMillis = System.currentTimeMillis();
        mHandler.postDelayed(mCountDownRunnable, Math.min(1000, mAutoDeleteDelayMillis));
    }

    private void restoreViewPosition(final View view) {
        setAlpha(view, 1f);
        setTranslationX(view, 0);
    }

    private void removePreviousContextualUndoIfPresent() {
        if (mCurrentRemovedView != null) {
            performRemovalIfNecessary();
        }
    }

    private void setCurrentRemovedView(final ContextualUndoView currentRemovedView) {
        mCurrentRemovedView = currentRemovedView;
        mCurrentRemovedId = currentRemovedView.getItemId();
    }

    private void clearCurrentRemovedView() {
        mCurrentRemovedView = null;
        mCurrentRemovedId = -1;
        mHandler.removeCallbacks(mCountDownRunnable);
    }

    @Override
    public void onListScrolled() {
        performRemovalIfNecessary();
    }

    private void performRemovalIfNecessary() {
        if (mCurrentRemovedId == -1) {
            return;
        }

        ContextualUndoView currentRemovedView = getCurrentRemovedView(mCurrentRemovedView, mCurrentRemovedId);
        if (currentRemovedView != null) {
            ValueAnimator animator = ValueAnimator.ofInt(currentRemovedView.getHeight(), 1).setDuration(ANIMATION_DURATION);

            RemoveViewAnimatorListenerAdapter listener = new RemoveViewAnimatorListenerAdapter(currentRemovedView, mCurrentRemovedId);
            RemoveViewAnimatorUpdateListener updateListener = new RemoveViewAnimatorUpdateListener(listener);

            animator.addListener(listener);
            animator.addUpdateListener(updateListener);
            animator.start();
        } else {
            // The hard way.
            deleteItemGivenId(mCurrentRemovedId);
        }
        clearCurrentRemovedView();
    }

    private void deleteItemGivenId(final long deleteItemId) {
        int position = -1;
        int numItems = getCount();
        for (int i = 0; i < numItems; i++) {
            long itemId = getItemId(i);
            if (itemId == deleteItemId) {
                position = i;
                break;
            }
        }

        if (position >= 0) {
            mDeleteItemCallback.deleteItem(position);
        }
    }

    private ContextualUndoView getCurrentRemovedView(final ContextualUndoView currentRemovedView, final long itemId) {
        ContextualUndoView result = currentRemovedView;
        if (result == null ||
                result.getParent() == null ||
                result.getItemId() != itemId ||
                AdapterViewUtil.getPositionForView(getAbsListView(), result) < 0) {
            result = getContextualUndoView(itemId);
        }
        return result;
    }

    /**
     * This method should be called in your {@link Activity}'s {@link Activity#onSaveInstanceState(Bundle)} to remember dismissed statuses.
     * @param outState the {@link Bundle} provided by Activity.onSaveInstanceState(Bundle).
     */
    public void onSaveInstanceState(final Bundle outState) {
        outState.putLong(EXTRA_ACTIVE_REMOVED_ID, mCurrentRemovedId);
    }

    /**
     * This method should be called in your {@link Activity#onRestoreInstanceState(Bundle)} to remember dismissed statuses.
     */
    public void onRestoreInstanceState(final Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCurrentRemovedId = savedInstanceState.getLong(EXTRA_ACTIVE_REMOVED_ID, -1);
        }
    }

    /**
     * Animate the item at given position away and show the undo {@link View}.
     * @param position the position.
     */
    public void swipeViewAtPosition(final int position) {
        mCurrentRemovedId = getItemId(position);
        for (int i = 0; i < getAbsListView().getChildCount(); i++) {
            AbsListView absListView = getAbsListView();
            View childView = absListView.getChildAt(i);
            int positionForView = AdapterViewUtil.getPositionForView(absListView, childView);
            if (positionForView == position) {
                swipeView(childView, positionForView);
            }
        }
    }

    private void swipeView(final View view, final int dismissPosition) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, X, view.getMeasuredWidth());
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(final Animator animator) {
                onViewSwiped(((ContextualUndoView) view).getItemId(), dismissPosition);
            }
        });
        animator.start();
    }

    @Override
    public void setIsParentHorizontalScrollContainer(final boolean isParentHorizontalScrollContainer) {
        super.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
        if (mContextualUndoListViewTouchListener != null) {
            mContextualUndoListViewTouchListener.setIsParentHorizontalScrollContainer(isParentHorizontalScrollContainer);
        }
    }

    @Override
    public void setTouchChild(final int childResId) {
        super.setTouchChild(childResId);
        if (mContextualUndoListViewTouchListener != null) {
            mContextualUndoListViewTouchListener.setTouchChild(childResId);
        }
    }

    /**
     * Removes any item that was swiped away.
     * @param animate If true, animates the removal (collapsing the item).
     *                If false, removes item immediately without animation.
     * @deprecated use {@link #removePendingItem()} or {@link #animateRemovePendingItem()} instead.
     */
    @Deprecated
    public void removePendingItem(final boolean animate) {
        if (animate) {
            animateRemovePendingItem();
        } else {
            removePendingItem();
        }
    }

    /**
     * Cancels the count down, and removes any item that was swiped away, without animating. Will cause {@link DeleteItemCallback#deleteItem(int)} to be called.
     */
    public void removePendingItem() {
        if (mCurrentRemovedView != null || mCurrentRemovedId >= 0) {
            new RemoveViewAnimatorListenerAdapter(mCurrentRemovedView, mCurrentRemovedId).onAnimationEnd(null);
            clearCurrentRemovedView();
        }
    }

    /**
     * Removes any item that was swiped away, animating the removal (collapsing the item). {@link DeleteItemCallback#deleteItem(int)} to be called.
     */
    public void animateRemovePendingItem() {
        removePreviousContextualUndoIfPresent();
    }

    /**
     * Cancel the count down. This will not cause the {@link DeleteItemCallback#deleteItem(int)} to be called. Use {@link #removePendingItem()} for that instead.
     */
    public void cancelCountDown() {
        mHandler.removeCallbacks(mCountDownRunnable);
    }

    /**
     * A callback interface which is used to notify when items should be removed from the collection.
     */
    public interface DeleteItemCallback {
        /**
         * Called when an item should be removed from the collection.
         *
         * @param position
         *            the position of the item that should be removed.
         */
        public void deleteItem(int position);
    }

    /**
     * A callback interface which is used to provide the text to display when counting down.
     */
    public interface CountDownFormatter {
        /**
         * Called each tick of the CountDownTimer
         * @param millisLeft time in milliseconds remaining before the item is automatically removed
         */
        public String getCountDownString(final long millisLeft);
    }

    private class CountDownRunnable implements Runnable {

        @Override
        public void run() {
            long millisRemaining = mAutoDeleteDelayMillis - (System.currentTimeMillis() - mDismissStartMillis);
            if (mCountDownFormatter != null) {
                mCurrentRemovedView.updateCountDownTimer(mCountDownFormatter.getCountDownString(millisRemaining));
            }

            if (millisRemaining <= 0) {
                performRemovalIfNecessary();
            } else {
                mHandler.postDelayed(this, Math.min(millisRemaining, 1000));
            }
        }
    }

    private class RemoveViewAnimatorListenerAdapter extends AnimatorListenerAdapter {

        private ContextualUndoView mDismissView;
        private final long mDismissViewId;
        private final int mOriginalHeight;

        public RemoveViewAnimatorListenerAdapter(final ContextualUndoView dismissView, final long dismissViewId) {
            mDismissView = dismissView;
            mDismissViewId = dismissViewId;
            mOriginalHeight = dismissView.getHeight();
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mDismissView = getViewBeingAnimated();
            if (mDismissView == null) {
                deleteItemGivenId(mDismissViewId);
                return;
            }

            restoreViewPosition(mDismissView);
            restoreViewDimension(mDismissView);
            deleteCurrentItem(mDismissView);
        }

        private void restoreViewDimension(final View view) {
            ViewGroup.LayoutParams lp;
            lp = view.getLayoutParams();
            lp.height = mOriginalHeight;
            view.setLayoutParams(lp);
        }

        private void deleteCurrentItem(final View view) {
            int position = AdapterViewUtil.getPositionForView(getAbsListView(), view);
            mDeleteItemCallback.deleteItem(position);
        }

        private ContextualUndoView getViewBeingAnimated() {
            ContextualUndoView newDismissView = getCurrentRemovedView(mDismissView, mDismissViewId);
            if (newDismissView != mDismissView) {
                restoreViewPosition(mDismissView);
                restoreViewDimension(mDismissView);

                mDismissView = newDismissView;
            }
            return mDismissView;
        }
    }

    private class RemoveViewAnimatorUpdateListener implements ValueAnimator.AnimatorUpdateListener {

        final RemoveViewAnimatorListenerAdapter mParentAdapter;
        private final ViewGroup.LayoutParams mLayoutParams;

        public RemoveViewAnimatorUpdateListener(final RemoveViewAnimatorListenerAdapter parentAdapter) {
            mParentAdapter = parentAdapter;
            mLayoutParams = parentAdapter.mDismissView.getLayoutParams();
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator valueAnimator) {
            ContextualUndoView dismissView = mParentAdapter.getViewBeingAnimated();
            if (dismissView != null) {
                mLayoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(mLayoutParams);
            }
        }
    }

    private class UndoListener implements View.OnClickListener {

        private final ContextualUndoView mContextualUndoView;

        public UndoListener(final ContextualUndoView contextualUndoView) {
            mContextualUndoView = contextualUndoView;
        }

        @Override
        public void onClick(final View v) {
            clearCurrentRemovedView();
            mContextualUndoView.displayContentView();
            moveViewOffScreen();
            animateViewComingBack();
        }

        private void moveViewOffScreen() {
            ViewHelper.setTranslationX(mContextualUndoView, mContextualUndoView.getWidth());
        }

        private void animateViewComingBack() {
            animate(mContextualUndoView).translationX(0).setDuration(ANIMATION_DURATION).setListener(null);
        }
    }

    private class HierarchyChangeListener implements ViewGroup.OnHierarchyChangeListener {
        @Override
        public void onChildViewAdded(final View parent, final View child) {
            final ViewHolder vh = ViewHolder.getViewHolder(child);
            if (vh != null && mCurrentRemovedId > 0 && vh.mItemId == mCurrentRemovedId) {
                mCurrentRemovedView = (ContextualUndoView) child;
            }
        }

        @Override
        public void onChildViewRemoved(final View parent, final View child) {
            final ViewHolder vh = ViewHolder.getViewHolder(child);
            if (vh != null && mCurrentRemovedId > 0 && vh.mItemId == mCurrentRemovedId) {
                mCurrentRemovedView = null;
            }
        }
    }

    private static class ViewHolder {
        final ContextualUndoView mContextualUndoView;

        long mItemId;

        static ViewHolder getViewHolder(final View view) {
            return (ViewHolder) view.getTag();
        }

        ViewHolder(final ContextualUndoView contextualUndoView) {
            mContextualUndoView = contextualUndoView;
            mContextualUndoView.setTag(this);
        }
    }
}