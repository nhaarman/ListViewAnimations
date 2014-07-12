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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.WrapperListAdapter;

import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.Swappable;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * The DynamicListView is an extension of {@link android.widget.ListView} that supports cell dragging
 * and swapping.
 * </p>
 * Make sure your adapter has stable ids, and override {@link android.widget.ListAdapter#hasStableIds()} to return true.</br>
 * </p>
 * This layout is in charge of positioning the hover cell in the correct location
 * on the screen in response to user touch events. It uses the position of the
 * hover cell to determine when two cells should be swapped. If two cells should
 * be swapped, all the corresponding data set and layout changes are handled here.
 * </p>
 * If no cell is selected, all the touch events are passed down to the ListView
 * and behave normally. If one of the items in the ListView experiences a
 * long press event, the contents of its current visible state are captured as
 * a bitmap and its visibility is set to INVISIBLE. A hover cell is then created and
 * added to this layout as an overlaying BitmapDrawable above the ListView. Once the
 * hover cell is translated some distance to signify an item swap, a data set change
 * accompanied by animation takes place. When the user releases the hover cell,
 * it animates into its corresponding position in the ListView.
 * </p>
 * When the hover cell is either above or below the bounds of the ListView, this
 * ListView also scrolls on its own so as to reveal additional content.
 * </p>
 * See http://youtu.be/_BZIvjMgH-Q
 */
public class DynamicListView extends ListView {

    private static final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private static final int INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;
    private static final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;
    @NonNull
    private final ScrollProperties mScrollProperties = new ScrollProperties();
    @NonNull
    private final TouchProperties mTouchProperties = new TouchProperties();
    /**
     * This scroll listener is added to the listview in order to handle cell swapping
     * when the cell is either at the top or bottom edge of the listview. If the hover
     * cell is at either edge of the listview, the listview will begin scrolling. As
     * scrolling takes place, the listview continuously checks if new cells became visible
     * and determines whether they are potential candidates for a cell swap.
     */
    @Nullable
    private final OnScrollListener mScrollListener = new MyOnScrollListener();
    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    @Nullable
    private final OnItemLongClickListener mOnItemLongClickListener = new MyOnItemLongClickListener();
    @Nullable
    private HoverCellHandler mHoverCellHandler;
    private int mOriginalTranscriptMode;
    private int mTotalOffset;
    private int mScrollState;
    @Nullable
    private OnTouchListener mOnTouchListener;
    private int mResIdOfDynamicTouchChild;
    private boolean mDynamicTouchChildTouched;
    private int mSlop;
    private boolean mSkipCallingOnTouchListener;
    @Nullable
    private OnItemMovedListener mOnItemMovedListener;
    private int mLastMovedToIndex;
    @Nullable
    private OnHoverCellListener mOnHoverCellListener;

    public DynamicListView(@NonNull final Context context) {
        super(context);
        init(context);
    }

    public DynamicListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DynamicListView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(@NonNull final Context context) {
        setOnItemLongClickListener(mOnItemLongClickListener);
        setOnScrollListener(mScrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mScrollProperties.mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mSlop = vc.getScaledTouchSlop();
    }

    @SuppressWarnings("TypeMayBeWeakened")
    public void setAdapter(final BaseAdapter adapter) {
        super.setAdapter(adapter);
    }

    /**
     * @deprecated use #setAdapter(BaseAdapter) instead.
     */
    @Override
    @Deprecated
    public void setAdapter(final ListAdapter adapter) {
        if (!(adapter instanceof BaseAdapter)) {
            throw new IllegalArgumentException("DynamicListView needs a BaseAdapter!");
        }
        super.setAdapter(adapter);
    }

    /**
     * Enables the drag and drop functionality.
     */
    public void enableDrag() {
        setOnItemLongClickListener(mOnItemLongClickListener);
    }

    /**
     * Disables the drag and drop functionality.
     */
    public void disableDrag() {
        setOnItemLongClickListener(null);
    }

    private void makeCellMobile(final int x, final int y) {
        int position = pointToPosition(x, y);
        int itemNumber = position - getFirstVisiblePosition();

        View selectedView = getChildAt(itemNumber);
        if (selectedView == null || position < getHeaderViewsCount() || position >= getAdapter().getCount() - getFooterViewsCount()) {
            return;
        }

        mOriginalTranscriptMode = getTranscriptMode();
        setTranscriptMode(TRANSCRIPT_MODE_NORMAL);

        mTotalOffset = 0;
        mLastMovedToIndex = AdapterViewUtil.getPositionForView(this, selectedView) + getHeaderViewsCount();
        mMobileItemId = getAdapter().getItemId(position);

        mHoverCellHandler = new HoverCellHandler(selectedView, mOnHoverCellListener);

        selectedView.setVisibility(INVISIBLE);
        getParent().requestDisallowInterceptTouchEvent(true);
        updateNeighborViewsForId(mMobileItemId);
    }

    /**
     * Stores a reference to the views above and below the item currently
     * corresponding to the hover cell. It is important to note that if this
     * item is either at the top or bottom of the list, mAboveItemId or mBelowItemId
     * may be invalid.
     */
    private void updateNeighborViewsForId(final long itemId) {
        ListAdapter adapter = getAdapter();
        if (!adapter.hasStableIds()) {
            throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        }

        int position = getPositionForId(itemId);
        mAboveItemId = position - 1 >= 0 ? adapter.getItemId(position - 1) : INVALID_ROW_ID;
        mBelowItemId = position + 1 < adapter.getCount() ? adapter.getItemId(position + 1) : INVALID_ROW_ID;
    }

    /**
     * Retrieves the view in the list corresponding to itemId
     */
    @Nullable
    private View getViewForId(final long itemId) {
        if (itemId == INVALID_ID) {
            return null;
        }

        int firstVisiblePosition = getFirstVisiblePosition();
        ListAdapter adapter = getAdapter();
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
     * dispatchDraw gets invoked when all the child views are about to be drawn.
     * By overriding this method, the hover cell (BitmapDrawable) can be drawn
     * over the listview's items whenever the listview is redrawn.
     */
    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCellHandler != null) {
            mHoverCellHandler.draw(canvas);
        }
    }

    @Override
    public void setOnTouchListener(@Nullable final OnTouchListener onTouchListener) {
        mOnTouchListener = onTouchListener;
    }

    public void setOnHoverCellListener(@Nullable final OnHoverCellListener onHoverCellListener) {
        mOnHoverCellListener = onHoverCellListener;
    }

    private void handleDownEvent(@NonNull final MotionEvent ev) {
        mTouchProperties.mDownX = (int) ev.getX();
        mTouchProperties.mDownY = (int) ev.getY();
        mActivePointerId = ev.getPointerId(0);

        mDynamicTouchChildTouched = false;
        if (mResIdOfDynamicTouchChild != 0) {

            int position = pointToPosition(mTouchProperties.mDownX, mTouchProperties.mDownY);
            int childNum = position == INVALID_POSITION ? -1 : position - getFirstVisiblePosition();
            View itemView = childNum >= 0 ? getChildAt(childNum) : null;
            View childView = itemView == null ? null : itemView.findViewById(mResIdOfDynamicTouchChild);
            if (childView != null) {
                final Rect childRect = ViewUtils.getChildViewRect(this, childView);
                if (childRect.contains(mTouchProperties.mDownX, mTouchProperties.mDownY)) {
                    mDynamicTouchChildTouched = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
            }
        }
    }

    private void handleMoveEvent(@NonNull final MotionEvent ev) {
        if (mActivePointerId == INVALID_POINTER_ID) {
            return;
        }

        int pointerIndex = ev.findPointerIndex(mActivePointerId);

        mTouchProperties.mLastEventY = (int) ev.getY(pointerIndex);
        mTouchProperties.mLastEventX = (int) ev.getX(pointerIndex);
        int deltaY = mTouchProperties.mLastEventY - mTouchProperties.mDownY;
        int deltaX = mTouchProperties.mLastEventX - mTouchProperties.mDownX;

        if (mHoverCellHandler == null && mDynamicTouchChildTouched) {
            if (Math.abs(deltaY) > mSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                makeCellMobile(mTouchProperties.mDownX, mTouchProperties.mDownY);

                // Cancel ListView's touch (un-highlighting the item)
                MotionEvent cancelEvent = MotionEvent.obtain(ev);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL | ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
                super.onTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }
        }

        if (mHoverCellHandler != null) {
            assert mHoverCellHandler != null;
            mHoverCellHandler.offset(deltaY + mTotalOffset);

            invalidate();

            handleCellSwitch();

            mScrollProperties.mIsMobileScrolling = false;
            handleMobileCellScroll();
        }
    }

    private void handleUpEvent() {
        mDynamicTouchChildTouched = false;
        touchEventsEnded();
    }

    private void handleCancelEvent() {
        mDynamicTouchChildTouched = false;
        touchEventsCancelled();
    }

    private void handlePointerUpEvent(@NonNull final MotionEvent ev) {
        /*
         * If a multitouch event took place and the original touch dictating
         * the movement of the hover cell has ended, then the dragging event
         * ends and the hover cell is animated to its corresponding position
         * in the listview.
         */
        int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            handleUpEvent();
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent ev) {
        if (mSkipCallingOnTouchListener) {
            return super.onTouchEvent(ev);
        }

        if (mOnTouchListener instanceof SwipeOnTouchListener) {
            if (((SwipeOnTouchListener) mOnTouchListener).isSwiping()) {
                mSkipCallingOnTouchListener = true;
                boolean result = mOnTouchListener.onTouch(this, ev);
                mSkipCallingOnTouchListener = false;
                return result || super.onTouchEvent(ev);
            }
        }

        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                handleDownEvent(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                handleMoveEvent(ev);
                break;
            case MotionEvent.ACTION_UP:
                handleUpEvent();
                break;
            case MotionEvent.ACTION_CANCEL:
                handleCancelEvent();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handlePointerUpEvent(ev);
                break;
            default:
                break;
        }

        boolean result;
        if (mHoverCellHandler != null) {
            result = false;
        } else if (mOnTouchListener != null) {
            mSkipCallingOnTouchListener = true;
            result = mOnTouchListener.onTouch(this, ev) || super.onTouchEvent(ev);
            mSkipCallingOnTouchListener = false;
        } else {
            result = super.onTouchEvent(ev);
        }
        return result;
    }

    /**
     * This method determines whether the hover cell has been shifted far enough
     * to invoke a cell swap. If so, then the respective cell swap candidate is
     * determined and the data set is changed. Upon posting a notification of the
     * data set change, a layout is invoked to place the cells in the right place.
     * Using a ViewTreeObserver and a corresponding OnPreDrawListener, we can
     * offset the cell being swapped to where it previously was and then animate it to
     * its new position.
     */
    private void handleCellSwitch() {
        assert mHoverCellHandler != null;

        final int deltaY = mTouchProperties.mLastEventY - mTouchProperties.mDownY;
        int deltaYTotal = mHoverCellHandler.getTop() + mTotalOffset + deltaY;

        View mobileView = getViewForId(mMobileItemId);
        assert mobileView != null;

        View belowView = getViewForId(mBelowItemId);
        View aboveView = getViewForId(mAboveItemId);

        boolean hasViewBelow = belowView != null && deltaYTotal > belowView.getTop();
        boolean hasViewAbove = aboveView != null && deltaYTotal < aboveView.getTop();

        if (hasViewBelow || hasViewAbove) {
            final long switchItemId = hasViewBelow ? mBelowItemId : mAboveItemId;
            View switchView = hasViewBelow ? belowView : aboveView;

            if (getPositionForView(switchView) < getHeaderViewsCount() || getPositionForView(switchView) >= getAdapter().getCount() - getFooterViewsCount()) {
                return;
            }
            final int originalItem = getPositionForView(mobileView);
            swapElements(originalItem, getPositionForView(switchView));

            BaseAdapter adapter;
            if (getAdapter() instanceof WrapperListAdapter) {
                adapter = (BaseAdapter) ((WrapperListAdapter) getAdapter()).getWrappedAdapter();
            } else {
                adapter = (BaseAdapter) getAdapter();
            }
            adapter.notifyDataSetChanged();

            mTouchProperties.mDownY = mTouchProperties.mLastEventY;
            mTouchProperties.mDownX = mTouchProperties.mLastEventX;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViewsForId(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new MyOnPreDrawListener(switchItemId, deltaY, switchViewStartTop));
        }
    }

    private void swapElements(final int indexOne, final int indexTwo) {
        mLastMovedToIndex = indexTwo;
        ListAdapter adapter = getAdapter();

        if (adapter instanceof WrapperListAdapter) {
            adapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
        }

        if (adapter instanceof Swappable) {
            ((Swappable) adapter).swapItems(indexOne - getHeaderViewsCount(), indexTwo - getHeaderViewsCount());
        }
    }

    /**
     * Resets all the appropriate fields to a default state while also animating
     * the hover cell back to its correct location.
     */
    private void touchEventsEnded() {
        final View mobileView = getViewForId(mMobileItemId);
        assert mobileView != null;

        if (mHoverCellHandler != null || mScrollProperties.mIsWaitingForScrollFinish) {
            mScrollProperties.mIsWaitingForScrollFinish = false;
            mScrollProperties.mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;
            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;

            /* Restore the transcript mode */
            setTranscriptMode(mOriginalTranscriptMode);

            // If the autoscroller has not completed scrolling, we need to wait
            // for it to finish in order to determine the final location of where the
            // hover cell should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mScrollProperties.mIsWaitingForScrollFinish = true;
                return;
            }

            if (mHoverCellHandler != null) {
                mHoverCellHandler.offsetTo(mobileView.getTop());
                ObjectAnimator hoverViewAnimator = mHoverCellHandler.createAnimateToEndPositionAnimator();
                hoverViewAnimator.addUpdateListener(
                        new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                                invalidate();
                            }
                        }
                );
                hoverViewAnimator.addListener(
                        new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(final Animator animation) {
                                setEnabled(false);
                            }

                            @Override
                            public void onAnimationEnd(final Animator animation) {
                                mobileView.setVisibility(VISIBLE);
                                setEnabled(true);
                                invalidate();
                                if (mOnItemMovedListener != null) {
                                    mOnItemMovedListener.onItemMoved(mLastMovedToIndex - getHeaderViewsCount());
                                }
                            }
                        }
                );
                hoverViewAnimator.start();
                mHoverCellHandler = null;
            }
        } else {
            touchEventsCancelled();
        }
    }

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled() {
        if (mHoverCellHandler != null) {
            View mobileView = getViewForId(mMobileItemId);
            assert mobileView != null;
            mobileView.setVisibility(VISIBLE);

            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;
            mHoverCellHandler = null;
            invalidate();
        }
        mScrollProperties.mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }

    /**
     * This method is in charge of determining if the hover cell is above
     * or below the bounds of the listview. If so, the listview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    private void handleMobileCellScroll() {
        assert mHoverCellHandler != null;
        Rect r = mHoverCellHandler.getHoverCellCurrentBounds();

        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        boolean result = false;
        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mScrollProperties.mSmoothScrollAmountAtEdge, 0);
            result = true;
        } else if (hoverViewTop + hoverHeight >= height && offset + extent < range) {
            smoothScrollBy(mScrollProperties.mSmoothScrollAmountAtEdge, 0);
            result = true;
        }
        mScrollProperties.mIsMobileScrolling = result;
    }

    /**
     * If this {@code DynamicListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch events from reaching the {@code DynamicListView}.
     * <p/>
     * If a {@code DynamicListView} view has a child with the given resource id, the user can still swipe the list item by touching that child.
     * If the user touches an area outside that child (but inside the list item view), then the swipe will not happen and the parent
     * will do its job instead (scrolling horizontally).
     *
     * @param childResId The resource id of the list items' child that the user should touch to be able to swipe the list items.
     */
    public void setDynamicTouchChild(@IdRes final int childResId) {
        mResIdOfDynamicTouchChild = childResId;
    }

    /**
     * Set the {@link OnItemMovedListener} to be notified when an item is dropped.
     */
    public void setOnItemMovedListener(@Nullable final OnItemMovedListener onItemMovedListener) {
        mOnItemMovedListener = onItemMovedListener;
    }

    private static class TouchProperties {
        int mLastEventY = -1;
        int mLastEventX = -1;
        int mDownY = -1;
        int mDownX = -1;
    }

    private static class ScrollProperties {
        boolean mIsMobileScrolling;
        int mSmoothScrollAmountAtEdge;
        boolean mIsWaitingForScrollFinish;
    }

    private class MyOnScrollListener implements OnScrollListener {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        @Override
        public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = mPreviousFirstVisibleItem == -1 ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = mPreviousVisibleItemCount == -1 ? mCurrentVisibleItemCount : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            mCurrentScrollState = scrollState;
            mScrollState = scrollState;
            isScrollCompleted();
        }

        /**
         * This method is in charge of invoking 1 of 2 actions. Firstly, if the listview
         * is in a state of scrolling invoked by the hover cell being outside the bounds
         * of the listview, then this scrolling event is continued. Secondly, if the hover
         * cell has already been released, this invokes the animation for the hover cell
         * to return to its correct position after the listview has entered an idle scroll
         * state.
         */
        private void isScrollCompleted() {
            if (mCurrentVisibleItemCount > 0 && mCurrentScrollState == SCROLL_STATE_IDLE) {
                if (mHoverCellHandler != null && mScrollProperties.mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mScrollProperties.mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, then the appropriate parameters are updated.
         */
        private void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mHoverCellHandler != null && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForId(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, then the appropriate parameters are updated.
         */
        private void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mHoverCellHandler != null && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForId(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }
    }

    private class MyOnItemLongClickListener implements OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (mResIdOfDynamicTouchChild == 0) {
                mDynamicTouchChildTouched = true;
                makeCellMobile(mTouchProperties.mDownX, mTouchProperties.mDownY);
                return true;
            }
            return false;
        }
    }

    private class MyOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

        private static final int MOVE_DURATION = 150;

        private final long mSwitchItemId;
        private final int mDeltaY;
        private final int mSwitchViewStartTop;

        MyOnPreDrawListener(final long switchItemId, final int deltaY, final int switchViewStartTop) {
            mSwitchItemId = switchItemId;
            mDeltaY = deltaY;
            mSwitchViewStartTop = switchViewStartTop;
        }

        @Override
        public boolean onPreDraw() {
            getViewTreeObserver().removeOnPreDrawListener(this);

            mTotalOffset += mDeltaY;

            View switchView = getViewForId(mSwitchItemId);
            if (switchView != null) {
                int switchViewNewTop = switchView.getTop();
                int delta = mSwitchViewStartTop - switchViewNewTop;

                ViewHelper.setTranslationY(switchView, delta);

                ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, "translationY", 0);
                animator.setDuration(MOVE_DURATION);
                animator.start();
            }

            return true;
        }
    }
}