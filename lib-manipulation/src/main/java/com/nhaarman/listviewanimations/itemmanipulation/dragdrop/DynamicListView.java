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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Rect;
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

@TargetApi(14)
public class DynamicListView extends ListView {

    private static final int INVALID_ID = -1;

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
     * The y coordinate of the last non-final {@code MotionEvent}.
     */
    private float mLastMotionEventY = -1;

    /**
     * The id of the item view that is being dragged.
     * This value is {@value #INVALID_ID} if and only if the user is not dragging.
     */
    private long mMobileItemId;

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
     * @param position the position of the item start dragging.
     *
     * @throws java.lang.IllegalStateException if the user is not touching this {@code DynamicListView}.
     */
    public void startDragging(final int position) {
        if (mLastMotionEventY < 0) {
            throw new IllegalStateException("User must be touching the DynamicListView!");
        }
        mMobileItemId = getAdapter().getItemId(position);
        mMobileView = getChildAt(position - getFirstVisiblePosition());
        mHoverDrawable = new HoverDrawable(mMobileView, mLastMotionEventY);
        mMobileView.setVisibility(INVISIBLE);
    }

    /**
     * Sets the {@link DraggableManager} to be used for determining whether an item should be dragged when the user issues a down {@code MotionEvent}.
     */
    public void setDraggableManager(@NonNull final DraggableManager draggableManager) {
        mDraggableManager = draggableManager;
    }

    public void setOnItemMovedListener(@Nullable final OnItemMovedListener onItemMovedListener) {
        mOnItemMovedListener = onItemMovedListener;
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent ev) {
        boolean handled;

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handled = handleDownEvent(ev);
                mLastMotionEventY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                handled = handleMoveEvent(ev);
                mLastMotionEventY = ev.getY();
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

        return handled || super.onTouchEvent(ev);
    }

    private boolean handleDownEvent(@NonNull final MotionEvent ev) {
        boolean handled = false;

        int position = pointToPosition((int) ev.getX(), (int) ev.getY());
        if (position != INVALID_POSITION) {
            View downView = getChildAt(position - getFirstVisiblePosition());
            assert downView != null;
            if (mDraggableManager.isDraggable(downView, position, ev.getX() - downView.getX(), ev.getY() - downView.getY())) {
                mMobileItemId = getAdapter().getItemId(position);
                mMobileView = getChildAt(position - getFirstVisiblePosition());
                mHoverDrawable = new HoverDrawable(mMobileView, ev);
                mMobileView.setVisibility(INVISIBLE);
                handled = true;
            }
        }

        return handled;
    }

    /**
     * Retrieves the position in the list corresponding to itemId
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
     * Retrieves the view in the list corresponding to itemId
     */
    @Nullable
    private View getViewForId(final long itemId) {
        ListAdapter adapter = getAdapter();
        if (itemId == INVALID_ID || adapter == null) {
            return null;
        }

        int firstVisiblePosition = getFirstVisiblePosition();
        if (!adapter.hasStableIds()) {
            throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        }

        View result = null;
        for (int i = 0; i < getChildCount() && result == null; i++) {
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemId) {
                result = getChildAt(i);
            }
        }
        return result;
    }

    private boolean handleMoveEvent(@NonNull final MotionEvent ev) {
        if (mHoverDrawable == null) {
            return false;
        }
        mHoverDrawable.handleMoveEvent(ev);

        switchIfNecessary();
        invalidate();

        return true;
    }

    private void switchIfNecessary() {
        if (mHoverDrawable == null || getAdapter() == null) {
            return;
        }

        int position = getPositionForId(mMobileItemId);
        long aboveItemId = position - 1 >= 0 ? getAdapter().getItemId(position - 1) : INVALID_ROW_ID;
        long belowItemId = position + 1 < getAdapter().getCount() ? getAdapter().getItemId(position + 1) : INVALID_ROW_ID;

        final long switchId = mHoverDrawable.isMovingUpwards() ? aboveItemId : belowItemId;
        View switchView = getViewForId(switchId);

        final int deltaY = mHoverDrawable.getDeltaY();
        if (switchView != null && Math.abs(deltaY) > mHoverDrawable.getIntrinsicHeight()) {
            switchViews(switchView, switchId, deltaY);
        }

        mScrollHandler.handleMobileCellScroll();

        invalidate();
    }

    private void switchViews(final View switchView, final long switchId, final float deltaY) {
        assert mHoverDrawable != null;

        final int switchViewPosition = getPositionForView(switchView);
        int mobileViewPosition = getPositionForView(mMobileView);

        ((Swappable) getAdapter()).swapItems(switchViewPosition, mobileViewPosition);
        ((BaseAdapter) getAdapter()).notifyDataSetChanged();

        mHoverDrawable.shift(switchView.getHeight());
        animateSwitchView(switchId, deltaY);
    }

    private void animateSwitchView(final long switchId, final float translationY) {
        getViewTreeObserver().addOnPreDrawListener(new AnimateSwitchViewOnPreDrawListener(switchId, translationY));
    }

    private boolean handleUpEvent() {
        if (mMobileView == null) {
            return false;
        }
        assert mHoverDrawable != null;

        mMobileView.setVisibility(VISIBLE);
        mMobileView.setTranslationY(mHoverDrawable.getDeltaY());
        mMobileView.animate().translationY(0).start();

        if (mOnItemMovedListener != null) {
            mOnItemMovedListener.onItemMoved(getPositionForId(mMobileItemId));
        }

        mHoverDrawable = null;
        mMobileView = null;
        mMobileItemId = INVALID_ID;

        return true;
    }

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

    private static class DefaultDraggableManager implements DraggableManager {

        @Override
        public boolean isDraggable(@NonNull final View view, final int position, final float x, final float y) {
            return false;
        }
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

            if (mMobileView != null) {
                mMobileView.setVisibility(VISIBLE);
            }
            mMobileView = getViewForId(mMobileItemId);
            if (mMobileView != null) {
                mMobileView.setVisibility(INVISIBLE);
            }
            return true;
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

        private static final int SMOOTH_SCROLL_DP = 20;

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
            if (mHoverDrawable == null) {
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
                mHoverDrawable.onScroll(mMobileView.getY());
            }

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

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
            if (mHoverDrawable == null || mCurrentFirstVisibleItem >= mPreviousFirstVisibleItem) {
                return;
            }

            int position = getPositionForId(mMobileItemId);
            if (position == INVALID_POSITION) {
                return;
            }

            long switchItemId = position - 1 >= 0 ? getAdapter().getItemId(position - 1) : INVALID_ROW_ID;
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
            if (mHoverDrawable == null || mCurrentLastVisibleItem <= mPreviousLastVisibleItem) {
                return;
            }

            int position = getPositionForId(mMobileItemId);
            if (position == INVALID_POSITION) {
                return;
            }

            long switchItemId = position + 1 < getAdapter().getCount() ? getAdapter().getItemId(position + 1) : INVALID_ROW_ID;
            View switchView = getViewForId(switchItemId);
            if (switchView != null) {
                switchViews(switchView, switchItemId, switchView.getHeight());
            }
        }
    }
}