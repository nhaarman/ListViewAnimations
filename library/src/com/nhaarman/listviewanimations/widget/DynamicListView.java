/*
 * Copyright (C) 2013 The Android Open Source Project
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhaarman.listviewanimations.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeOnTouchListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.animation.TypeEvaluator;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

/**
 * The DynamicListView is an extension of {@link ListView} that supports cell dragging
 * and swapping.
 * </p>
 * Make sure your adapter has stable ids, and override {@link ListAdapter#hasStableIds()} to return true.</br>
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

    private int mOriginalTranscriptMode;

    public interface OnHoverCellListener {
        public Drawable onHoverCellCreated(Drawable hoverCellDrawable);
    }

    /**
     * Implement this interface to be notified of ordering changes. Call {@link #setOnItemMovedListener(com.nhaarman.listviewanimations.widget.DynamicListView.OnItemMovedListener)}.
     */
    public interface OnItemMovedListener {
        /**
         * Called after an item is dropped and moved.
         *
         * @param newPosition the new position of the item.
         */
        public void onItemMoved(int newPosition);
    }

    private final int SMOOTH_SCROLL_AMOUNT_AT_EDGE = 15;
    private final int MOVE_DURATION = 150;

    private int mLastEventY = -1, mLastEventX = -1;

    private int mDownY = -1;
    private int mDownX = -1;

    private int mTotalOffset = 0;

    private boolean mCellIsMobile = false;
    private boolean mIsMobileScrolling = false;
    private int mSmoothScrollAmountAtEdge = 0;

    private final int INVALID_ID = -1;
    private long mAboveItemId = INVALID_ID;
    private long mMobileItemId = INVALID_ID;
    private long mBelowItemId = INVALID_ID;

    private Drawable mHoverCell;
    private Rect mHoverCellCurrentBounds;
    private Rect mHoverCellOriginalBounds;

    private final int INVALID_POINTER_ID = -1;
    private int mActivePointerId = INVALID_POINTER_ID;

    private boolean mIsWaitingForScrollFinish = false;
    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    private OnTouchListener mOnTouchListener;
    private boolean mIsParentHorizontalScrollContainer;
    private int mResIdOfDynamicTouchChild;
    private boolean mDynamicTouchChildTouched;
    private int mSlop;

    private boolean mSkipCallingOnTouchListener;

    private OnHoverCellListener mOnHoverCellListener;

    private OnItemMovedListener mOnItemMovedListener;
    private int mLastMovedToIndex;

    public DynamicListView(Context context) {
        super(context);
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public DynamicListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        setOnItemLongClickListener(mOnItemLongClickListener);
        setOnScrollListener(mScrollListener);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        mSmoothScrollAmountAtEdge = (int) (SMOOTH_SCROLL_AMOUNT_AT_EDGE / metrics.density);
        ViewConfiguration vc = ViewConfiguration.get(getContext());
        mSlop = vc.getScaledTouchSlop();
    }

    public void setAdapter(BaseAdapter adapter) {
        super.setAdapter(adapter);
    }

    @Override
    @Deprecated
    /**
     * @deprecated use #setAdapter(BaseAdapter) instead.
     */
    public void setAdapter(ListAdapter adapter) {
        if (!(adapter instanceof BaseAdapter)) {
            throw new IllegalArgumentException("DynamicListView needs a BaseAdapter!");
        }
        super.setAdapter(adapter);
    }

    /**
     * Listens for long clicks on any items in the listview. When a cell has
     * been selected, the hover cell is created and set up.
     */
    private OnItemLongClickListener mOnItemLongClickListener = new OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos, long id) {
            if (mResIdOfDynamicTouchChild == 0) {
                mDynamicTouchChildTouched = true;
                makeCellMobile();
                return true;
            }
            return false;
        }
    };

    private void makeCellMobile() {
        int position = pointToPosition(mDownX, mDownY);
        int itemNum = position - getFirstVisiblePosition();
        View selectedView = getChildAt(itemNum);
        if (selectedView == null || position < getHeaderViewsCount() || position >= getAdapter().getCount() - getHeaderViewsCount()) {
            return;
        }

        mOriginalTranscriptMode = getTranscriptMode();
        setTranscriptMode(TRANSCRIPT_MODE_NORMAL);


        mTotalOffset = 0;

        mMobileItemId = getAdapter().getItemId(position);
        mHoverCell = getAndAddHoverView(selectedView);
        if (mOnHoverCellListener != null) {
            mHoverCell = mOnHoverCellListener.onHoverCellCreated(mHoverCell);
        }
        selectedView.setVisibility(INVISIBLE);

        mCellIsMobile = true;
        getParent().requestDisallowInterceptTouchEvent(true);

        updateNeighborViewsForId(mMobileItemId);
    }

    /**
     * Creates the hover cell with the appropriate bitmap and of appropriate
     * size. The hover cell's BitmapDrawable is drawn on top of the bitmap every
     * single time an invalidate call is made.
     */
    private BitmapDrawable getAndAddHoverView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        int top = v.getTop();
        int left = v.getLeft();

        Bitmap b = getBitmapFromView(v);

        BitmapDrawable drawable = new BitmapDrawable(getResources(), b);

        mHoverCellOriginalBounds = new Rect(left, top, left + w, top + h);
        mHoverCellCurrentBounds = new Rect(mHoverCellOriginalBounds);

        drawable.setBounds(mHoverCellCurrentBounds);

        return drawable;
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    private Bitmap getBitmapFromView(View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

    /**
     * Stores a reference to the views above and below the item currently
     * corresponding to the hover cell. It is important to note that if this
     * item is either at the top or bottom of the list, mAboveItemId or mBelowItemId
     * may be invalid.
     */
    private void updateNeighborViewsForId(long itemId) {
        int position = getPositionForId(itemId);
        ListAdapter adapter = getAdapter();
        if (!adapter.hasStableIds()) {
            throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        }

        mAboveItemId = position - 1 >= 0 ? adapter.getItemId(position - 1) : INVALID_ROW_ID;
        mBelowItemId = position + 1 < adapter.getCount() ? adapter.getItemId(position + 1) : INVALID_ROW_ID;
    }

    /**
     * Retrieves the view in the list corresponding to itemId
     */
    private View getViewForId(long itemId) {
        int firstVisiblePosition = getFirstVisiblePosition();
        ListAdapter adapter = getAdapter();
        if (!adapter.hasStableIds()) {
            throw new IllegalStateException("Adapter doesn't have stable ids! Make sure your adapter has stable ids, and override hasStableIds() to return true.");
        }

        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            int position = firstVisiblePosition + i;
            long id = adapter.getItemId(position);
            if (id == itemId) {
                return v;
            }
        }
        return null;
    }

    /**
     * Retrieves the position in the list corresponding to itemId
     */
    private int getPositionForId(long itemId) {
        View v = getViewForId(itemId);
        if (v == null) {
            return -1;
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
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverCell != null) {
            mHoverCell.draw(canvas);
        }
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        mOnTouchListener = l;
    }

    public void setOnHoverCellListener(OnHoverCellListener onHoverCellListener) {
        mOnHoverCellListener = onHoverCellListener;
    }

    private Rect getChildViewRect(View parentView, View childView) {
        final Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (parentView == childView) {
            return childRect;
        }

        ViewGroup parent;
        while ((parent = (ViewGroup) childView.getParent()) != parentView) {
            childRect.offset(parent.getLeft(), parent.getTop());
            childView = parent;
        }

        return childRect;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSkipCallingOnTouchListener) {
            return super.onTouchEvent(event);
        }

        if (mOnTouchListener instanceof SwipeOnTouchListener) {
            if (((SwipeOnTouchListener) mOnTouchListener).isSwiping()) {
                mSkipCallingOnTouchListener = true;
                boolean retVal = mOnTouchListener.onTouch(this, event);
                mSkipCallingOnTouchListener = false;
                return retVal || super.onTouchEvent(event);
            }
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mDownX = (int) event.getX();
                mDownY = (int) event.getY();
                mActivePointerId = event.getPointerId(0);

                mDynamicTouchChildTouched = false;
                if (mResIdOfDynamicTouchChild != 0) {
                    mIsParentHorizontalScrollContainer = false;

                    int position = pointToPosition(mDownX, mDownY);
                    int childNum = (position != INVALID_POSITION) ? position - getFirstVisiblePosition() : -1;
                    View itemView = (childNum >= 0) ? getChildAt(childNum) : null;
                    View childView = (itemView != null) ? itemView.findViewById(mResIdOfDynamicTouchChild) : null;
                    if (childView != null) {
                        final Rect childRect = getChildViewRect(this, childView);
                        if (childRect.contains(mDownX, mDownY)) {
                            mDynamicTouchChildTouched = true;
                            getParent().requestDisallowInterceptTouchEvent(true);
                        }
                    }
                }

                if (mIsParentHorizontalScrollContainer) {
                    // Do it now and don't wait until the user moves more than the
                    // slop factor.
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER_ID) {
                    break;
                }

                int pointerIndex = event.findPointerIndex(mActivePointerId);

                mLastEventY = (int) event.getY(pointerIndex);
                mLastEventX = (int) event.getX(pointerIndex);
                int deltaY = mLastEventY - mDownY;
                int deltaX = mLastEventX - mDownX;

                if (!mCellIsMobile && mDynamicTouchChildTouched) {
                    if (Math.abs(deltaY) > mSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                        makeCellMobile();

                        // Cancel ListView's touch (un-highlighting the item)
                        MotionEvent cancelEvent = MotionEvent.obtain(event);
                        cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (event.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
                        super.onTouchEvent(cancelEvent);
                        cancelEvent.recycle();
                    }
                }

                if (mCellIsMobile) {
                    mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mHoverCellOriginalBounds.top + deltaY + mTotalOffset);
                    mHoverCell.setBounds(mHoverCellCurrentBounds);
                    invalidate();

                    handleCellSwitch();

                    mIsMobileScrolling = false;
                    handleMobileCellScroll();
                }
                break;
            case MotionEvent.ACTION_UP:
                mDynamicTouchChildTouched = false;
                touchEventsEnded();
                break;
            case MotionEvent.ACTION_CANCEL:
                mDynamicTouchChildTouched = false;
                touchEventsCancelled();
                break;
            case MotionEvent.ACTION_POINTER_UP:
            /*
             * If a multitouch event took place and the original touch dictating
			 * the movement of the hover cell has ended, then the dragging event
			 * ends and the hover cell is animated to its corresponding position
			 * in the listview.
			 */
                pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    mDynamicTouchChildTouched = false;
                    touchEventsEnded();
                }
                break;
            default:
                break;
        }

        if (mCellIsMobile) {
            return false;
        } else if (mOnTouchListener != null) {
            mSkipCallingOnTouchListener = true;
            boolean retVal = mOnTouchListener.onTouch(this, event);
            mSkipCallingOnTouchListener = false;
            if (retVal) {
                return true;
            }
        }
        return super.onTouchEvent(event);
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
        final int deltaY = mLastEventY - mDownY;
        int deltaYTotal = mHoverCellOriginalBounds.top + mTotalOffset + deltaY;

        View belowView = getViewForId(mBelowItemId);
        View mobileView = getViewForId(mMobileItemId);
        View aboveView = getViewForId(mAboveItemId);

        boolean isBelow = (belowView != null) && (deltaYTotal > belowView.getTop());
        boolean isAbove = (aboveView != null) && (deltaYTotal < aboveView.getTop());

        if (isBelow || isAbove) {

            final long switchItemId = isBelow ? mBelowItemId : mAboveItemId;
            View switchView = isBelow ? belowView : aboveView;
            final int originalItem = getPositionForView(mobileView);

            if (switchView == null) {
                updateNeighborViewsForId(mMobileItemId);
                return;
            }

            if (getPositionForView(switchView) < getHeaderViewsCount()) {
                return;
            }
            swapElements(originalItem, getPositionForView(switchView));

            BaseAdapter adapter;
            if (getAdapter() instanceof HeaderViewListAdapter) {
                adapter = (BaseAdapter) ((HeaderViewListAdapter) getAdapter()).getWrappedAdapter();
            } else {
                adapter = (BaseAdapter) getAdapter();
            }
            adapter.notifyDataSetChanged();

            mDownY = mLastEventY;
            mDownX = mLastEventX;

            final int switchViewStartTop = switchView.getTop();

            mobileView.setVisibility(View.VISIBLE);
            switchView.setVisibility(View.INVISIBLE);

            updateNeighborViewsForId(mMobileItemId);

            final ViewTreeObserver observer = getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                public boolean onPreDraw() {
                    observer.removeOnPreDrawListener(this);

                    View switchView = getViewForId(switchItemId);

                    mTotalOffset += deltaY;

                    int switchViewNewTop = switchView.getTop();
                    int delta = switchViewStartTop - switchViewNewTop;

                    ViewHelper.setTranslationY(switchView, delta);

                    ObjectAnimator animator = ObjectAnimator.ofFloat(switchView, "translationY", 0);
                    animator.setDuration(MOVE_DURATION);
                    animator.start();

                    return true;
                }
            });
        }
    }

    private void swapElements(int indexOne, int indexTwo) {
        mLastMovedToIndex = indexTwo;
        ListAdapter adapter = getAdapter();

        if (adapter instanceof HeaderViewListAdapter) {
            adapter = ((HeaderViewListAdapter) adapter).getWrappedAdapter();
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
        if (mCellIsMobile || mIsWaitingForScrollFinish) {
            mCellIsMobile = false;
            mIsWaitingForScrollFinish = false;
            mIsMobileScrolling = false;
            mActivePointerId = INVALID_POINTER_ID;

            /* Restore the transcript mode */
            setTranscriptMode(mOriginalTranscriptMode);

            // If the autoscroller has not completed scrolling, we need to wait
            // for it to
            // finish in order to determine the final location of where the
            // hover cell
            // should be animated to.
            if (mScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
                mIsWaitingForScrollFinish = true;
                return;
            }

            mHoverCellCurrentBounds.offsetTo(mHoverCellOriginalBounds.left, mobileView.getTop());

            ObjectAnimator hoverViewAnimator = ObjectAnimator.ofObject(mHoverCell, "bounds", sBoundEvaluator, mHoverCellCurrentBounds);
            hoverViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    invalidate();
                }
            });
            hoverViewAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    setEnabled(false);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    mAboveItemId = INVALID_ID;
                    mMobileItemId = INVALID_ID;
                    mBelowItemId = INVALID_ID;
                    mobileView.setVisibility(VISIBLE);
                    mHoverCell = null;
                    setEnabled(true);
                    invalidate();
                    if (mOnItemMovedListener != null) {
                        mOnItemMovedListener.onItemMoved(mLastMovedToIndex - getHeaderViewsCount());
                    }
                }
            });
            hoverViewAnimator.start();
        } else {
            touchEventsCancelled();
        }
    }

    /**
     * Resets all the appropriate fields to a default state.
     */
    private void touchEventsCancelled() {
        View mobileView = getViewForId(mMobileItemId);
        if (mCellIsMobile) {
            mAboveItemId = INVALID_ID;
            mMobileItemId = INVALID_ID;
            mBelowItemId = INVALID_ID;
            mobileView.setVisibility(VISIBLE);
            mHoverCell = null;
            invalidate();
        }
        mCellIsMobile = false;
        mIsMobileScrolling = false;
        mActivePointerId = INVALID_POINTER_ID;
    }

    /**
     * This TypeEvaluator is used to animate the BitmapDrawable back to its
     * final location when the user lifts his finger by modifying the
     * BitmapDrawable's bounds.
     */
    private final static TypeEvaluator<Rect> sBoundEvaluator = new TypeEvaluator<Rect>() {
        public Rect evaluate(float fraction, Rect startValue, Rect endValue) {
            return new Rect(interpolate(startValue.left, endValue.left, fraction), interpolate(startValue.top, endValue.top, fraction), interpolate(startValue.right, endValue.right, fraction),
                    interpolate(startValue.bottom, endValue.bottom, fraction));
        }

        public int interpolate(int start, int end, float fraction) {
            return (int) (start + fraction * (end - start));
        }
    };

    /**
     * Determines whether this listview is in a scrolling state invoked
     * by the fact that the hover cell is out of the bounds of the listview;
     */
    private void handleMobileCellScroll() {
        mIsMobileScrolling = handleMobileCellScroll(mHoverCellCurrentBounds);
    }

    /**
     * This method is in charge of determining if the hover cell is above
     * or below the bounds of the listview. If so, the listview does an appropriate
     * upward or downward smooth scroll so as to reveal new items.
     */
    private boolean handleMobileCellScroll(Rect r) {
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        if (hoverViewTop + hoverHeight >= height && (offset + extent) < range) {
            smoothScrollBy(mSmoothScrollAmountAtEdge, 0);
            return true;
        }

        return false;
    }

    public void setIsParentHorizontalScrollContainer(boolean isParentHorizontalScrollContainer) {
        mIsParentHorizontalScrollContainer = (mResIdOfDynamicTouchChild == 0) && isParentHorizontalScrollContainer;
    }

    public boolean isParentHorizontalScrollContainer() {
        return mIsParentHorizontalScrollContainer;
    }

    public void setDynamicTouchChild(int childResId) {
        mResIdOfDynamicTouchChild = childResId;
        if (childResId != 0) {
            setIsParentHorizontalScrollContainer(false);
        }
    }

    /**
     * This scroll listener is added to the listview in order to handle cell swapping
     * when the cell is either at the top or bottom edge of the listview. If the hover
     * cell is at either edge of the listview, the listview will begin scrolling. As
     * scrolling takes place, the listview continuously checks if new cells became visible
     * and determines whether they are potential candidates for a cell swap.
     */
    private OnScrollListener mScrollListener = new OnScrollListener() {

        private int mPreviousFirstVisibleItem = -1;
        private int mPreviousVisibleItemCount = -1;
        private int mCurrentFirstVisibleItem;
        private int mCurrentVisibleItemCount;
        private int mCurrentScrollState;

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mCurrentFirstVisibleItem = firstVisibleItem;
            mCurrentVisibleItemCount = visibleItemCount;

            mPreviousFirstVisibleItem = (mPreviousFirstVisibleItem == -1) ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
            mPreviousVisibleItemCount = (mPreviousVisibleItemCount == -1) ? mCurrentVisibleItemCount : mPreviousVisibleItemCount;

            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();

            mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
            mPreviousVisibleItemCount = mCurrentVisibleItemCount;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
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
                if (mCellIsMobile && mIsMobileScrolling) {
                    handleMobileCellScroll();
                } else if (mIsWaitingForScrollFinish) {
                    touchEventsEnded();
                }
            }
        }

        /**
         * Determines if the listview scrolled up enough to reveal a new cell at the
         * top of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleFirstVisibleCellChange() {
            if (mCurrentFirstVisibleItem != mPreviousFirstVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForId(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }

        /**
         * Determines if the listview scrolled down enough to reveal a new cell at the
         * bottom of the list. If so, then the appropriate parameters are updated.
         */
        public void checkAndHandleLastVisibleCellChange() {
            int currentLastVisibleItem = mCurrentFirstVisibleItem + mCurrentVisibleItemCount;
            int previousLastVisibleItem = mPreviousFirstVisibleItem + mPreviousVisibleItemCount;
            if (currentLastVisibleItem != previousLastVisibleItem) {
                if (mCellIsMobile && mMobileItemId != INVALID_ID) {
                    updateNeighborViewsForId(mMobileItemId);
                    handleCellSwitch();
                }
            }
        }
    };

    /**
     * Set the {@link com.nhaarman.listviewanimations.widget.DynamicListView.OnItemMovedListener} to be notified when an item is dropped.
     */
    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        this.mOnItemMovedListener = onItemMovedListener;
    }

    /**
     * Interface, usually implemented by a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator},
     * that indicates that it can swap the visual position of two list items.
     *
     * @author Anton Spaans on 9/11/13.
     */
    public interface Swappable {

        /**
         * Swaps the item on the first adapter position with the item on the second adapter position.
         * Be sure to call {@link android.widget.BaseAdapter#notifyDataSetChanged()} if appropriate when implementing this method.
         *
         * @param positionOne First adapter position.
         * @param positionTwo Second adapter position.
         */
        public void swapItems(int positionOne, int positionTwo);
    }
}