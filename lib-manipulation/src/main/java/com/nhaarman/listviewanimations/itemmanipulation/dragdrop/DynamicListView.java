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

package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.util.Swappable;

/**
 * A {@link android.widget.ListView} implementation which allows for dragging and reordering items.
 */
@TargetApi(14)
public class DynamicListView extends ListView {

    private static final int INVALID_ID = -1;

    /**
     * The {@link android.widget.ListAdapter} that is assigned. Also implements {@link Swappable}.
     */
    @Nullable
    private ListAdapter mAdapter;

    /**
     * The Drawable that is drawn when the user is dragging an item.
     * This value is null if and only if the user is not dragging.
     */
    @Nullable
    private HoverDrawable mHoverDrawable;

    /**
     * The View that is represented by {@link #mHoverDrawable}.
     * When this value is not null, the View should be invisible.
     * This value is null if and only if the user is not dragging.
     */
    @Nullable
    private View mMobileView;

    /**
     * The id of the item view that is being dragged.
     * This value is {@value #INVALID_ID} if and only if the user is not dragging.
     */
    private long mMobileItemId;

    /**
     * The y coordinate of the last non-final {@code MotionEvent}.
     */
    private float mLastMotionEventY = -1;

    /**
     * The original position of the view that is being dragged.
     * This value is {@value #INVALID_POSITION} if and only if the user is not dragging.
     */
    private int mOriginalMobileItemPosition = INVALID_POSITION;

    /**
     * The {@link ScrollHandler} that handles scrolling when dragging an item.
     */
    @NonNull
    private ScrollHandler mScrollHandler;

    /**
     * The {@link DraggableManager} responsible for deciding if an item can be dragged.
     */
    @NonNull
    private DraggableManager mDraggableManager;

    /**
     * The {@link OnItemMovedListener} that is notified of moved items.
     */
    @Nullable
    private OnItemMovedListener mOnItemMovedListener;

    /**
     * The {@link SwitchViewAnimator} that is responsible for animating the switch views.
     */
    @NonNull
    private SwitchViewAnimator mSwitchViewAnimator;

    /**
     * Specifies whether or not the hover drawable is currently being animated as result of an up / cancel event.
     */
    private boolean mIsSettlingHoverDrawable;

    public DynamicListView(@NonNull final Context context) {
        super(context);
        init();
    }

    public DynamicListView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DynamicListView(@NonNull final Context context, @NonNull final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        mScrollHandler = new ScrollHandler();
        setOnScrollListener(mScrollHandler);

        mDraggableManager = new DefaultDraggableManager();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mSwitchViewAnimator = new KitKatSwitchViewAnimator();
        } else {
            mSwitchViewAnimator = new LSwitchViewAnimator();
        }

        mMobileItemId = INVALID_ID;
    }

    /**
     * @throws java.lang.IllegalStateException    if the adapter does not have stable ids.
     * @throws java.lang.IllegalArgumentException if the adapter does not implement {@link com.nhaarman.listviewanimations.util.Swappable}.
     */
    @Override
    public void setAdapter(@NonNull final ListAdapter adapter) {
        super.setAdapter(adapter);
        if (!adapter.hasStableIds()) {
            throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        }

        if (!(adapter instanceof Swappable)) {
            throw new IllegalArgumentException("Adapter should implement Swappable!");
        }

        mAdapter = adapter;
    }

    /**
     * Sets the scroll speed when dragging an item. Defaults to {@code 1.0f}.
     *
     * @param speed {@code <1.0f} to slow down scrolling, {@code >1.0f} to speed up scrolling.
     */
    public void setScrollSpeed(final float speed) {
        mScrollHandler.setScrollSpeed(speed);
    }

    /**
     * Starts dragging the item at given position. User must be touching this {@code DynamicListView}.
     *
     * @param position the position of the item in the adapter to start dragging. Be sure to subtract any header views.
     *
     * @throws java.lang.IllegalStateException if the user is not touching this {@code DynamicListView},
     *                                         or if there is no adapter set.
     */
    public void startDragging(final int position) {
        if (mMobileItemId != INVALID_ID) {
            /* We are already dragging */
            return;
        }

        if (mLastMotionEventY < 0) {
            throw new IllegalStateException("User must be touching the DynamicListView!");
        }

        if (mAdapter == null) {
            throw new IllegalStateException("This DynamicListView has no adapter set!");
        }

        if (position < 0 || position >= mAdapter.getCount()) {
            /* Out of bounds */
            return;
        }

        mOriginalMobileItemPosition = position;
        mMobileItemId = mAdapter.getItemId(position);
        mMobileView = getChildAt(position - getFirstVisiblePosition() + getHeaderViewsCount());
        mHoverDrawable = new HoverDrawable(mMobileView, mLastMotionEventY);
        mMobileView.setVisibility(INVISIBLE);
    }

    /**
     * Sets the {@link DraggableManager} to be used for determining whether an item should be dragged when the user issues a down {@code MotionEvent}.
     */
    public void setDraggableManager(@NonNull final DraggableManager draggableManager) {
        mDraggableManager = draggableManager;
    }

    /**
     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener} that is notified when user has dropped a dragging item.
     */
    public void setOnItemMovedListener(@Nullable final OnItemMovedListener onItemMovedListener) {
        mOnItemMovedListener = onItemMovedListener;
    }

    /**
     * Dispatches the {@link MotionEvent}s to their proper methods if applicable.
     *
     * @param ev the {@code MotionEvent}.
     *
     * @return {@code true} if the event was handled, {@code false} otherwise.
     */
    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent ev) {
        boolean handled = false;

        /* We are in the process of animating the hover drawable back, do not start a new drag yet. */
        if (!mIsSettlingHoverDrawable) {
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionEventY = ev.getY();
                    handled = handleDownEvent(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mLastMotionEventY = ev.getY();
                    handled = handleMoveEvent(ev);
                    break;
                case MotionEvent.ACTION_UP:
                    handled = handleUpEvent();
                    mLastMotionEventY = -1;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    handled = handleCancelEvent();
                    mLastMotionEventY = -1;
                    break;
                default:
                    handled = false;
                    break;
            }
        }
        return handled || super.onTouchEvent(ev);
    }

    /**
     * Handles the down event.
     * <p/>
     * Finds the position and {@code View} of the touch point and, if allowed by the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DraggableManager},
     * starts dragging the {@code View}.
     *
     * @param ev the {@link android.view.MotionEvent} that was triggered.
     *
     * @return {@code true} if we have started dragging, {@code false} otherwise.
     */
    private boolean handleDownEvent(@NonNull final MotionEvent ev) {
        boolean handled = false;

        int position = pointToPosition((int) ev.getX(), (int) ev.getY());
        if (position != INVALID_POSITION) {
            View downView = getChildAt(position - getFirstVisiblePosition());
            assert downView != null;
            if (mDraggableManager.isDraggable(downView, position - getHeaderViewsCount(), ev.getX() - downView.getX(), ev.getY() - downView.getY())) {
                startDragging(position - getHeaderViewsCount());
                handled = true;
            }
        }

        return handled;
    }

    /**
     * Retrieves the position in the list corresponding to itemId.
     *
     * @return the position of the item in the list, or {@link #INVALID_POSITION} if the {@code View} corresponding to the id was not found.
     */
    private int getPositionForId(final long itemId) {
        View v = getViewForId(itemId);
        if (v == null) {
            return INVALID_POSITION;
        } else {
            return getPositionForView(v);
        }
    }

    /**
     * Retrieves the {@code View} in the list corresponding to itemId.
     *
     * @return the {@code View}, or {@code null} if not found.
     */
    @Nullable
    private View getViewForId(final long itemId) {
        ListAdapter adapter = mAdapter;
        if (itemId == INVALID_ID || adapter == null) {
            return null;
        }

        int firstVisiblePosition = getFirstVisiblePosition();

        View result = null;
        for (int i = 0; i < getChildCount() && result == null; i++) {
            int position = firstVisiblePosition + i;
            if (position - getHeaderViewsCount() >= 0) {
                long id = adapter.getItemId(position - getHeaderViewsCount());
                if (id == itemId) {
                    result = getChildAt(i);
                }
            }
        }
        return result;
    }

    /**
     * Handles the move events.
     * <p/>
     * Applies the {@link MotionEvent} to the hover drawable, and switches {@code View}s if necessary.
     *
     * @param ev the {@code MotionEvent}.
     *
     * @return {@code true} if the event was handled, {@code false} otherwise.
     */
    private boolean handleMoveEvent(@NonNull final MotionEvent ev) {
        if (mHoverDrawable == null) {
            return false;
        }
        mHoverDrawable.handleMoveEvent(ev);

        switchIfNecessary();
        invalidate();

        return true;
    }

    /**
     * Finds the {@code View} that is a candidate for switching, and executes the switch if necessary.
     */
    private void switchIfNecessary() {
        if (mHoverDrawable == null || mAdapter == null) {
            return;
        }

        int position = getPositionForId(mMobileItemId);
        long aboveItemId = position - 1 - getHeaderViewsCount() >= 0 ? mAdapter.getItemId(position - 1 - getHeaderViewsCount()) : INVALID_ID;
        long belowItemId = position + 1 - getHeaderViewsCount() < mAdapter.getCount() ? mAdapter.getItemId(position + 1 - getHeaderViewsCount()) : INVALID_ID;

        final long switchId = mHoverDrawable.isMovingUpwards() ? aboveItemId : belowItemId;
        View switchView = getViewForId(switchId);

        final int deltaY = mHoverDrawable.getDeltaY();
        if (switchView != null && Math.abs(deltaY) > mHoverDrawable.getIntrinsicHeight()) {
            switchViews(switchView, switchId, mHoverDrawable.getIntrinsicHeight() * (deltaY < 0 ? -1 : 1));
        }

        mScrollHandler.handleMobileCellScroll();

        invalidate();
    }

    /**
     * Switches the item that is currently being dragged with the item belonging to given id,
     * by notifying the adapter to swap positions and that the data set has changed.
     *
     * @param switchView   the {@code View} that should be animated towards the old position of the currently dragging item.
     * @param switchId     the id of the item that will take the position of the currently dragging item.
     * @param translationY the distance in pixels the {@code switchView} should animate - i.e. the (positive or negative) height of the {@code View} corresponding to the currently
     *                     dragging item.
     */
    private void switchViews(final View switchView, final long switchId, final float translationY) {
        assert mHoverDrawable != null;
        assert mAdapter != null;

        final int switchViewPosition = getPositionForView(switchView);
        int mobileViewPosition = getPositionForView(mMobileView);

        ((Swappable) mAdapter).swapItems(switchViewPosition - getHeaderViewsCount(), mobileViewPosition - getHeaderViewsCount());
        ((BaseAdapter) mAdapter).notifyDataSetChanged();

        mHoverDrawable.shift(switchView.getHeight());
        mSwitchViewAnimator.animateSwitchView(switchId, translationY);
    }

    /**
     * Handles the up event.
     * <p/>
     * Animates the hover drawable to its final position, and finalizes our drag properties when the animation has finished.
     * Will also notify the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener} set if applicable.
     *
     * @return {@code true} if the event was handled, {@code false} otherwise.
     */
    private boolean handleUpEvent() {
        if (mMobileView == null) {
            return false;
        }
        assert mHoverDrawable != null;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(mHoverDrawable.getTop(), (int) mMobileView.getY());
        SettleHoverDrawableAnimatorListener listener = new SettleHoverDrawableAnimatorListener(mHoverDrawable, mMobileView);
        valueAnimator.addUpdateListener(listener);
        valueAnimator.addListener(listener);
        valueAnimator.start();

        int newPosition = getPositionForId(mMobileItemId) - getHeaderViewsCount();
        if (mOriginalMobileItemPosition != newPosition && mOnItemMovedListener != null) {
            mOnItemMovedListener.onItemMoved(mOriginalMobileItemPosition, newPosition);
        }

        return true;
    }

    /**
     * Handles the cancel event.
     *
     * @return {@code true} if the event was handled, {@code false} otherwise.
     */
    private boolean handleCancelEvent() {
        return handleUpEvent();
    }

    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mHoverDrawable != null) {
            mHoverDrawable.draw(canvas);
        }
    }

    /**
     * An interface for animating the switch views.
     * A distinction is made between API levels because {@link OnScrollListener#onScroll(android.widget.AbsListView, int, int,
     * int)} calling behavior differs.
     */
    private interface SwitchViewAnimator {

        void animateSwitchView(final long switchId, final float translationY);
    }

    /**
     * By default, nothing is draggable. User should set a {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DraggableManager} manually,
     * or use {@link #startDragging(int)} if they want to start a drag (for example using a long click listener).
     */
    private static class DefaultDraggableManager implements DraggableManager {

        @Override
        public boolean isDraggable(@NonNull final View view, final int position, final float x, final float y) {
            return false;
        }
    }

    /**
     * A {@link SwitchViewAnimator} for versions KitKat and below.
     * This class immediately updates {@link #mMobileView} to be the newly mobile view.
     */
    private class KitKatSwitchViewAnimator implements SwitchViewAnimator {

        @Override
        public void animateSwitchView(final long switchId, final float translationY) {
            assert mMobileView != null;
            getViewTreeObserver().addOnPreDrawListener(new AnimateSwitchViewOnPreDrawListener(mMobileView, switchId, translationY));
            mMobileView = getViewForId(mMobileItemId);
        }

        private class AnimateSwitchViewOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

            private final View mPreviousMobileView;

            private final long mSwitchId;

            private final float mTranslationY;

            AnimateSwitchViewOnPreDrawListener(final View previousMobileView, final long switchId, final float translationY) {
                mPreviousMobileView = previousMobileView;
                mSwitchId = switchId;
                mTranslationY = translationY;
            }

            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                View switchView = getViewForId(mSwitchId);
                if (switchView != null) {
                    switchView.setTranslationY(mTranslationY);
                    switchView.animate().translationY(0).start();
                }

                mPreviousMobileView.setVisibility(VISIBLE);

                if (mMobileView != null) {
                    mMobileView.setVisibility(INVISIBLE);
                }
                return true;
            }
        }
    }

    /**
     * A {@link SwitchViewAnimator} for versions L and above.
     * This class updates {@link #mMobileView} only after the next frame has been drawn.
     */
    private class LSwitchViewAnimator implements SwitchViewAnimator {

        @Override
        public void animateSwitchView(final long switchId, final float translationY) {
            getViewTreeObserver().addOnPreDrawListener(new AnimateSwitchViewOnPreDrawListener(switchId, translationY));
        }

        private class AnimateSwitchViewOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

            private final long mSwitchId;

            private final float mTranslationY;

            AnimateSwitchViewOnPreDrawListener(final long switchId, final float translationY) {
                mSwitchId = switchId;
                mTranslationY = translationY;
            }

            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                View switchView = getViewForId(mSwitchId);
                if (switchView != null) {
                    switchView.setTranslationY(mTranslationY);
                    switchView.animate().translationY(0).start();
                }

                assert mMobileView != null;
                mMobileView.setVisibility(VISIBLE);
                mMobileView = getViewForId(mMobileItemId);
                assert mMobileView != null;
                mMobileView.setVisibility(INVISIBLE);
                return true;
            }
        }
    }


    /**
     * A class which handles scrolling for this {@code DynamicListView} when dragging an item.
     * <p/>
     * The {@link #handleMobileCellScroll()} method initiates the scroll and should typically be called on a move {@code MotionEvent}.
     * <p/>
     * The {@link #onScroll(android.widget.AbsListView, int, int, int)} method then takes over the functionality {@link #handleMoveEvent(android.view.MotionEvent)} provides.
     */
    private class ScrollHandler implements OnScrollListener {

        private static final int SMOOTH_SCROLL_DP = 3;

        /**
         * The default scroll amount in pixels.
         */
        private final int mSmoothScrollPx;

        /**
         * The factor to multiply {@link #mSmoothScrollPx} with for scrolling.
         */
        private float mScrollSpeedFactor = 1.0f;

        /**
         * The previous first visible item before checking if we should switch.
         */
        private int mPreviousFirstVisibleItem = -1;

        /**
         * The previous last visible item before checking if we should switch.
         */
        private int mPreviousLastVisibleItem = -1;

        /**
         * The current first visible item.
         */
        private int mCurrentFirstVisibleItem;

        /**
         * The current last visible item.
         */
        private int mCurrentLastVisibleItem;

        ScrollHandler() {
            Resources r = getResources();
            mSmoothScrollPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, SMOOTH_SCROLL_DP, r.getDisplayMetrics());
        }

        /**
         * Sets the scroll speed when dragging an item. Defaults to {@code 1.0f}.
         *
         * @param scrollSpeedFactor {@code <1.0f} to slow down scrolling, {@code >1.0f} to speed up scrolling.
         */
        void setScrollSpeed(final float scrollSpeedFactor) {
            mScrollSpeedFactor = scrollSpeedFactor;
        }

        /**
         * Scrolls the {@code DynamicListView} if the hover drawable is above or below the bounds of the {@code ListView}.
         */
        void handleMobileCellScroll() {
            if (mHoverDrawable == null || mIsSettlingHoverDrawable) {
                return;
            }

            Rect r = mHoverDrawable.getBounds();
            int offset = computeVerticalScrollOffset();
            int height = getHeight();
            int extent = computeVerticalScrollExtent();
            int range = computeVerticalScrollRange();
            int hoverViewTop = r.top;
            int hoverHeight = r.height();

            int scrollPx = (int) Math.max(1, mSmoothScrollPx * mScrollSpeedFactor);
            if (hoverViewTop <= 0 && offset > 0) {
                smoothScrollBy(-scrollPx, 0);
            } else if (hoverViewTop + hoverHeight >= height && offset + extent < range) {
                smoothScrollBy(scrollPx, 0);
            }
        }

        @Override
        public void onScroll(@NonNull final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentLastVisibleItem = firstVisibleItem + visibleItemCount;

            mPreviousFirstVisibleItem = mPreviousFirstVisibleItem == -1 ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
            mPreviousLastVisibleItem = mPreviousLastVisibleItem == -1 ? mCurrentLastVisibleItem : mPreviousLastVisibleItem;

            if (mHoverDrawable != null) {
                assert mMobileView != null;
                float y = mMobileView.getY();
                mHoverDrawable.onScroll(y);
            }

            if (!mIsSettlingHoverDrawable) {
                checkAndHandleFirstVisibleCellChange();
                checkAndHandleLastVisibleCellChange();
            }

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousLastVisibleItem = mCurrentLastVisibleItem;
        }

        @Override
        public void onScrollStateChanged(@NonNull final AbsListView view, final int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE && mHoverDrawable != null) {
                handleMobileCellScroll();
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, switches the newly shown view with the mobile view.
         */
        private void checkAndHandleFirstVisibleCellChange() {
            if (mHoverDrawable == null || mAdapter == null || mCurrentFirstVisibleItem >= mPreviousFirstVisibleItem) {
                return;
            }

            int position = getPositionForId(mMobileItemId);
            if (position == INVALID_POSITION) {
                return;
            }

            long switchItemId = position - 1 - getHeaderViewsCount() >= 0 ? mAdapter.getItemId(position - 1 - getHeaderViewsCount()) : INVALID_ID;
            View switchView = getViewForId(switchItemId);
            if (switchView != null) {
                switchViews(switchView, switchItemId, -switchView.getHeight());
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, switches the newly shown view with the mobile view.
         */
        private void checkAndHandleLastVisibleCellChange() {
            if (mHoverDrawable == null || mAdapter == null || mCurrentLastVisibleItem <= mPreviousLastVisibleItem) {
                return;
            }

            int position = getPositionForId(mMobileItemId);
            if (position == INVALID_POSITION) {
                return;
            }

            long switchItemId = position + 1 - getHeaderViewsCount() < mAdapter.getCount() ? mAdapter.getItemId(position + 1 - getHeaderViewsCount()) : INVALID_ID;
            View switchView = getViewForId(switchItemId);
            if (switchView != null) {
                switchViews(switchView, switchItemId, switchView.getHeight());
            }
        }
    }

    /**
     * Updates the hover drawable's bounds with the animated values.
     * When the animation has finished, it will reset all the drag properties.
     */
    private class SettleHoverDrawableAnimatorListener extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        @NonNull
        private final HoverDrawable mAnimatingHoverDrawable;

        @NonNull
        private final View mAnimatingMobileView;

        private SettleHoverDrawableAnimatorListener(@NonNull final HoverDrawable animatingHoverDrawable, @NonNull final View animatingMobileView) {
            mAnimatingHoverDrawable = animatingHoverDrawable;
            mAnimatingMobileView = animatingMobileView;
        }

        @Override
        public void onAnimationStart(final Animator animation) {
            mIsSettlingHoverDrawable = true;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            mAnimatingHoverDrawable.setTop((Integer) animation.getAnimatedValue());
            postInvalidate();
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mAnimatingMobileView.setVisibility(VISIBLE);

            mHoverDrawable = null;
            mMobileView = null;
            mMobileItemId = INVALID_ID;
            mOriginalMobileItemPosition = INVALID_POSITION;

            mIsSettlingHoverDrawable = false;
        }
    }
}