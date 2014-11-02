package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;


import se.emilsjolander.stickylistheaders.StickyListHeadersListViewAbstract;
import se.emilsjolander.stickylistheaders.WrapperView;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListItemView;
import com.nhaarman.listviewanimations.itemmanipulation.TouchEventHandler;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nhaarman.listviewanimations.util.OnNotifyDataSetChanged;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;

public class SwipeMenuTouchListener implements View.OnTouchListener, TouchEventHandler, OnNotifyDataSetChanged {

    private static final int MIN_FLING_VELOCITY_FACTOR = 16;

    /**
     * The minimum distance in pixels that should be moved before starting horizontal item movement.
     */
    private final int mSlop;

    /**
     * The minimum velocity to initiate a fling, as measured in pixels per second.
     */
    private final int mMinFlingVelocity;

    /**
     * The maximum velocity to initiate a fling, as measured in pixels per second.
     */
    private final int mMaxFlingVelocity;

    /**
     * The duration of the fling animation.
     */
    private final long mAnimationTime;

    @NonNull
    private final ListViewWrapper mListViewWrapper;

    /**
     * The width of the {@link android.widget.AbsListView} in pixels.
     */
    private int mViewWidth = 1;

    /**
     * The raw X coordinate of the down event.
     */
    private float mDownX;

    /**
     * The raw Y coordinate of the down event.
     */
    private float mDownY;

    /**
     * Indicates whether the user is swiping an item.
     */
    private boolean mSwiping;

    /**
     * Indicates whether the user can show menu for the current item.
     */
    private boolean mCanShowMenuCurrent;

    /**
     * The {@code VelocityTracker} used in the swipe movement.
     */
    @Nullable
    private VelocityTracker mVelocityTracker;

    /**
     * The parent {@link android.view.View} being swiped.
     */
    @Nullable
    private DynamicListItemView mCurrentView;
    private int mCurrentDirection = DynamicListItemView.DIRECTION_NONE;
    /**
     * The current position being swiped.
     */
    private int mCurrentPosition = AdapterView.INVALID_POSITION;

    /**
     * The parent {@link android.view.View} being swiped.
     */
    @Nullable
    private DynamicListItemView mOpenedView = null;
    /**
     * The parent {@link android.view.View} being swiped.
     */
    @Nullable
    private int mOpenedPosition = AdapterView.INVALID_POSITION;

    @Nullable
    private DynamicListItemView mPreviousView = null;

    private int mPreviousPosition = AdapterView.INVALID_POSITION;
//
//    /**
//     * The number of items in the {@code AbsListView}, minus the pending dismissed items.
//     */
//    private int mVirtualListCount = -1;

    /**
     * Indicates whether the {@link android.widget.AbsListView} is in a horizontal scroll container.
     * If so, this class will prevent the horizontal scroller from receiving any touch events.
     */
    private boolean mParentIsHorizontalScrollContainer;

    /**
     * The resource id of the {@link android.view.View} that may steal touch events from their parents. Useful for example
     * when the {@link android.widget.AbsListView} is in a horizontal scroll container, but not the whole {@code AbsListView} should
     * steal the touch events.
     */
    private int mTouchChildResId;

    /**
     * Indicates whether swipe is enabled.
     */
    private boolean mMenuEnabled = true;

    /**
     * The callback which gets notified of events.
     */
    @NonNull
    private final SwipeMenuCallback mCallback;

    /**
     * The swipe menu adapter to get the menu buttons
     */
    @NonNull
    private MenuAdapter mSwipeMenuAdapter;

    /**
     * Constructs a new {@code SwipeTouchListener} for the given {@link android.widget.AbsListView}.
     */
    @SuppressWarnings("UnnecessaryFullyQualifiedName")
    protected SwipeMenuTouchListener(@NonNull final ListViewWrapper listViewWrapper, BaseAdapter adapter, @NonNull final SwipeMenuCallback callback) {
        ViewConfiguration vc = ViewConfiguration.get(listViewWrapper.getListView().getContext());

        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator) adapter).getDecoratedBaseAdapter();
        }
        if (adapter instanceof MenuAdapter) {
            mSwipeMenuAdapter = (MenuAdapter) adapter;
        }

        mCallback = callback;
        mSlop = vc.getScaledTouchSlop();
        mMinFlingVelocity = vc.getScaledMinimumFlingVelocity();
        mMaxFlingVelocity = vc.getScaledMaximumFlingVelocity();
        mAnimationTime = listViewWrapper.getListView().getContext().getResources().getInteger(android.R.integer.config_shortAnimTime);
        mListViewWrapper = listViewWrapper;
    }

//    /**
//     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} to specify which views can or cannot be swiped.
//     *
//     * @param dismissableManager {@code null} for no restrictions.
//     */
//    public void setDismissableManager(@Nullable final DismissableManager dismissableManager) {
//        mDismissableManager = dismissableManager;
//    }

    /**
     * If the {@link android.widget.AbsListView} is hosted inside a parent(/grand-parent/etc) that can scroll horizontally, horizontal swipes won't
     * work, because the parent will prevent touch-events from reaching the {@code AbsListView}.
     * <p/>
     * Call this method to fix this behavior.
     * Note that this will prevent the parent from scrolling horizontally when the user touches anywhere in a list item.
     */
    public void setParentIsHorizontalScrollContainer() {
        mParentIsHorizontalScrollContainer = true;
        mTouchChildResId = 0;
    }

    /**
     * Sets the resource id of a child view that should be touched to engage swipe.
     * When the user touches a region outside of that view, no swiping will occur.
     *
     * @param childResId The resource id of the list items' child that the user should touch to be able to swipe the list items.
     */
    public void setTouchChild(final int childResId) {
        mTouchChildResId = childResId;
        mParentIsHorizontalScrollContainer = false;
    }

    /**
     * Returns whether a menu is showing.
     *
     * @return {@code true} if a menu is showing.
     */
    public boolean isMenuShown() {
        return mOpenedView != null;
    }

    @NonNull
    public ListViewWrapper getListViewWrapper() {
        return mListViewWrapper;
    }

    /**
     * Enables the menu behavior.
     */
    public void enableMenus() {
        mMenuEnabled = true;
    }

    /**
     * Disables the menus behavior.
     */
    public void disableMenus() {
        mMenuEnabled = false;
    }

    @Override
    public boolean isInteracting() {
        return mSwiping;
    }


    public int getCurrentMenuPosition() {
        return mOpenedPosition;
    }

    public int getCurrentMenuDirection() {
        if (mCurrentView != null) {
            return mCurrentView.getCurrentDirection();
        }
        return DynamicListItemView.DIRECTION_NONE;
    }



    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent event) {
        return onTouch(null, event);
    }

    @Override
    public boolean onTouch(@Nullable final View view, @NonNull final MotionEvent event) {
        if (mListViewWrapper.getAdapter() == null) {
            return false;
        }

//        if (mVirtualListCount == -1 || mActiveSwipeCount == 0) {
//            mVirtualListCount = mListViewWrapper.getCount() - mListViewWrapper.getHeaderViewsCount();
//        }

        if (mViewWidth < 2) {
            mViewWidth = mListViewWrapper.getListView().getWidth();
        }

        boolean result;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                result = handleDownEvent(view, event);
                break;
            case MotionEvent.ACTION_MOVE:
                result = handleMoveEvent(view, event);
                break;
            case MotionEvent.ACTION_CANCEL:
                result = handleCancelEvent();
                break;
            case MotionEvent.ACTION_UP:
                result = handleUpEvent(event);
                break;
            default:
                result = false;
        }

        return result;
    }

    private boolean handleDownEvent(@Nullable final View view, @NonNull final MotionEvent motionEvent) {
        if (!mMenuEnabled) {
            return false;
        }

        DynamicListItemView downView = findDownView(motionEvent);
        if (downView == null) {
            return false;
        }

        int downPosition = AdapterViewUtil.getPositionForView(mListViewWrapper, downView);

        if (mOpenedPosition != AdapterView.INVALID_POSITION) {
            boolean shouldStop = mOpenedPosition != downPosition;
            if (!shouldStop) {
                Rect rect = new Rect();
                int deltaY = 0;
                View parentView = downView;
                if (parentView.getParent() instanceof WrapperView) {
                    parentView = (View) parentView.getParent();
                    deltaY = ((WrapperView)parentView).getItemTop();
                }
                final float screenX = motionEvent.getX();
                final float screenY = motionEvent.getY();
                final int viewX = (int) (screenX - parentView.getLeft());
                final int viewY = (int) (screenY - parentView.getTop() - deltaY);
                downView.getContentView().getHitRect(rect);
                shouldStop = rect.contains(viewX, viewY);
            }
            if (shouldStop) {
                closeMenusAnimated();
                //put swiping to true so that interacting get
                mSwiping = true;
                return true;
            }
            
        }


        mCanShowMenuCurrent = canShowMenu(downPosition, downView);

        if (!mCanShowMenuCurrent) {
            return false;
        }
        /* Check if we are processing the item at this position */
        if (mCurrentPosition == downPosition) {
            return false;
        }

        if (view != null) {
            view.onTouchEvent(motionEvent);
        }

        disableHorizontalScrollContainerIfNecessary(motionEvent, downView);

        mDownX = motionEvent.getX();
        mDownY = motionEvent.getY();

        mCurrentView = downView;
        mCurrentPosition = downPosition;

        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(motionEvent);
        return true;
    }

    /**
     * Returns the child {@link android.view.View} that was touched, by performing a hit test.
     *
     * @param motionEvent the {@link android.view.MotionEvent} to find the {@code View} for.
     *
     * @return the touched {@code View}, or {@code null} if none found.
     */
    @Nullable
    private DynamicListItemView findDownView(@NonNull final MotionEvent motionEvent) {
        Rect rect = new Rect();
        int childCount = mListViewWrapper.getChildCount();
        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();
        DynamicListItemView downView = null;
        for (int i = 0; i < childCount && downView == null; i++) {
            View child = mListViewWrapper.getChildAt(i);
            if (child != null) {
                child.getHitRect(rect);
                if (rect.contains(x, y)) {
                    if (child instanceof DynamicListItemView) {
                        downView = (DynamicListItemView) child;
                    }
                    else if (child instanceof WrapperView) {
                        View item = ((WrapperView) child).getItem();
                        if (item instanceof DynamicListItemView) {
                            downView = (DynamicListItemView) item;
                        }
                    }
                }
            }
        }
        return downView;
    }

    /**
     * Finds out whether the item represented by given position can show a menu.
     *
     * @param position the position of the item.
     *
     * @return {@code true} if the item can show a menu, false otherwise.
     */
    private boolean canShowMenu(final int position, @NonNull final DynamicListItemView view) {
        return mSwipeMenuAdapter != null && (mSwipeMenuAdapter.canShowLeftMenu(position, view) ||
                mSwipeMenuAdapter.canShowRightMenu(position, view));
    }
    
    
    private ViewGroup getListView() {
        ViewGroup result = mListViewWrapper.getListView();
        if (result instanceof StickyListHeadersListViewAbstract) {
            result = ((StickyListHeadersListViewAbstract)result).getWrappedList();
        }
        return result;
    }

    private void disableHorizontalScrollContainerIfNecessary(@NonNull final MotionEvent motionEvent, @NonNull final View view) {
        if (mParentIsHorizontalScrollContainer) {
            getListView().requestDisallowInterceptTouchEvent(true);
        } else if (mTouchChildResId != 0) {
            mParentIsHorizontalScrollContainer = false;

            final View childView = view.findViewById(mTouchChildResId);
            if (childView != null) {
                final Rect childRect = getChildViewRect(mListViewWrapper.getListView(), childView);
                if (childRect.contains((int) motionEvent.getX(), (int) motionEvent.getY())) {
                    getListView().requestDisallowInterceptTouchEvent(true);
                }
            }
        }
    }

    private boolean fetchMenuButtonsIfNecessary(final int direction) {
        switch (direction) {
            case DynamicListItemView.DIRECTION_LEFT:
                if (mSwipeMenuAdapter.canShowLeftMenu(mCurrentPosition, mCurrentView)) {
                    if (mCurrentView.getLeftMenu() == null) {
                        mCurrentView.setLeftButtons(mSwipeMenuAdapter.getLeftButtons(mCurrentPosition, mCurrentView));
                    }
                    return mCurrentView.getLeftMenu() != null;
                }
                break;
            case DynamicListItemView.DIRECTION_RIGHT:
                if (mSwipeMenuAdapter.canShowRightMenu(mCurrentPosition, mCurrentView)) {
                    if (mCurrentView.getRightMenu() == null) {
                        mCurrentView.setRightButtons(mSwipeMenuAdapter.getRightButtons(mCurrentPosition, mCurrentView));
                    }
                    return mCurrentView.getRightMenu() != null;
                }
                break;
        }
        return false;
    }

    private boolean handleMoveEvent(@Nullable final View view, @NonNull final MotionEvent motionEvent) {
        if (!mCanShowMenuCurrent || mVelocityTracker == null || mCurrentView == null) {
            return false;
        }

        mVelocityTracker.addMovement(motionEvent);

        float deltaX = motionEvent.getX() - mDownX;
        float deltaY = motionEvent.getY() - mDownY;
        int direction = (deltaX > 0) ? DynamicListItemView.DIRECTION_LEFT : DynamicListItemView.DIRECTION_RIGHT;

        //not in swipe yet
        if (!mSwiping) {
            if (Math.abs(deltaX) > mSlop && Math.abs(deltaX) > Math.abs(deltaY)) {

                //if the menu is already opened, fake a deltaX so that we translate the views
                //with the correct values. Needs to be done only here so that we don't mess up
                // the test to go into swiping mode
                if (mOpenedView != null) {
                    mDownX -= mOpenedView.getCurrentDeltaX();
                    deltaX = motionEvent.getX() - mDownX;
                }

                //prepare for reuse in case the buttons are used in multiple items
                if (mPreviousView != null && mPreviousView != mCurrentView) {
                    mPreviousView.prepareForReuse();
                    mPreviousView = null;
                }

                mSwiping = true;
                
                getListView().requestDisallowInterceptTouchEvent(true);
                onStartSwipe(mCurrentView, mCurrentPosition, direction);

                /* Cancel ListView's touch (un-highlighting the item) */
                MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL | motionEvent.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT);
                //dispatch the cancel touch event in case the highlighting is done
                //in a deeper view
                mCurrentView.dispatchTouchEvent(cancelEvent);
                if (view != null) {
                    view.onTouchEvent(cancelEvent);
                }
                cancelEvent.recycle();
            } else if (Math.abs(deltaY) > mSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                //the user is scrolling the listview, prevent menu until up
                reset();
            }
        }

        if (mSwiping) {
            if (mCurrentDirection != direction) {
                mCurrentDirection = direction;
                fetchMenuButtonsIfNecessary(mCurrentDirection);
            }
            if (!mCurrentView.setSwipeOffset(deltaX)) {
                reset();
                handleMenuClosed();
            }
            return true;
        }
        return false;
    }

    private boolean handleCancelEvent() {
        if (mVelocityTracker == null || mCurrentView == null) {
            return false;
        }

        if (mCurrentPosition != AdapterView.INVALID_POSITION && mSwiping) {
            closeMenusAnimated();
        }

        reset();
        return false;
    }

    private long getAnimationTime() {
        long animationTime = mAnimationTime;
        if (mVelocityTracker != null) {
            float velocityX = Math.abs(mVelocityTracker.getXVelocity());
            animationTime /= Math.ceil(velocityX / 2000.0f);
        }
        return animationTime;
    }

    private boolean handleUpEvent(@NonNull final MotionEvent motionEvent) {
        if (mVelocityTracker == null || mCurrentView == null) {
            if (mSwiping) {
                //it could be true in the case where we trapped the down event to close the menu
                reset();
            }
            return false;
        }

        if (mSwiping) {
            float deltaX = motionEvent.getX() - mDownX;

            mVelocityTracker.addMovement(motionEvent);
            mVelocityTracker.computeCurrentVelocity(1000);

            int shouldOpenMenu = -1;

            if (mCanShowMenuCurrent) {
                float velocityX = Math.abs(mVelocityTracker.getXVelocity());
                float velocityY = Math.abs(mVelocityTracker.getYVelocity());
                shouldOpenMenu = mCurrentView.shouldOpenMenuOnUp(deltaX, velocityX, velocityY,
                        mMinFlingVelocity, mMaxFlingVelocity);
            }

            if (shouldOpenMenu >= 0) {
                beforeOpenMenu(mCurrentView, mCurrentPosition, shouldOpenMenu);
                mCurrentView.openMenu(shouldOpenMenu, getAnimationTime(), new FlingAnimatorListener(mCurrentView, mCurrentPosition));
            } else {
                closeMenusAnimated();
            }
            getListView().requestDisallowInterceptTouchEvent(false);
            mCurrentDirection = DynamicListItemView.DIRECTION_NONE;
        }

        reset();
        return false;
    }

    /**
     * Close the current menu with animation.
     * @param animationTime     the animation duration
     */
    public void closeMenus(long animationTime) {
        if (mOpenedView != null) {
            beforeCloseMenu(mOpenedView, mOpenedPosition);
            mOpenedView.closeMenu(animationTime, mCloseAnimationListener);
        }
        else if (mCurrentView != null) {
            beforeCloseMenu(mCurrentView, mCurrentPosition);
            mCurrentView.closeMenu(animationTime, mCloseAnimationListener);
        }
    }

    /**
     * Close the current menu with animation.
     */
    public void closeMenusAnimated() {
        closeMenus(getAnimationTime());
    }

    /**
     * Close the current menu without animation
     */
    public void closeMenus() {
        closeMenus(0);
    }

    /**
     * Resets the fields to the initial values, ready to start over.
     */
    private void reset() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
        }

        mVelocityTracker = null;
        mDownX = 0;
        mDownY = 0;
        mCurrentView = null;
        mCurrentPosition = AdapterView.INVALID_POSITION;
        mSwiping = false;
        mCanShowMenuCurrent = false;
    }



    /**
     * Called when the user starts swiping a {@link android.view.View}.
     *
     * @param view     the {@code View} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void onStartSwipe(@NonNull final DynamicListItemView view, final int position, final int direction) {
        if (mCallback != null) {
            mCallback.onStartSwipe(view, position, direction);
        }
    }

    /**
     * Called when the menu is about to be closed
     *
     * @param view     the {@code View} that was swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void beforeCloseMenu(@NonNull final DynamicListItemView view, final int position) {
        if (mCallback != null) {
            mCallback.beforeMenuClose(view, position, view.getCurrentDirection());
        }
    }

    /**
     * Called after the restore animation of a canceled swipe movement ends.
     *
     * @param view     the {@code View} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void afterCloseMenu(@NonNull final DynamicListItemView view, final int position, final int direction) {
        if (mCallback != null) {
            mCallback.onMenuClosed(view, position, direction);
        }
    }

    /**
     * Called before the menu opens
     *
     * @param view the {@code DynamicListItemView} that would be flinged.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     * @param position the direction in which the menu will open.
     *
     */
    protected void beforeOpenMenu(@NonNull final DynamicListItemView view, final int position, final int direction) {
        if (mCallback != null) {
            mCallback.beforeMenuShow(view, position, direction);
        }
    }

    /**
     * Called after the menu successfully opened.
     * Users of this class should implement any finalizing behavior at this point, such as notifying the adapter.
     *
     * @param view     the {@code DynamicListItemView} that is being swiped.
     * @param position the position of the item in the {@link android.widget.ListAdapter} corresponding to the {@code View}.
     */
    protected void afterOpenMenu(@NonNull final DynamicListItemView view, final int position) {
    }

    private static Rect getChildViewRect(final View parentView, final View childView) {
        Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (!parentView.equals(childView)) {
            View workingChildView = childView;
            ViewGroup parent;
            while (!(parent = (ViewGroup) workingChildView.getParent()).equals(parentView)) {
                childRect.offset(parent.getLeft(), parent.getTop());
                workingChildView = parent;
            }
        }
        return childRect;
    }


    private void handleMenuOpened(final DynamicListItemView view, final int position) {
        mOpenedView = view;
        mOpenedPosition = position;
        afterOpenMenu(view, position);
    }

    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} that notifies when the fling animation has ended.
     */
    private class FlingAnimatorListener extends AnimatorListenerAdapter {

        @NonNull
        private final DynamicListItemView mView;

        private final int mPosition;

        private FlingAnimatorListener(@NonNull final DynamicListItemView view, final int position) {
            mView = view;
            mPosition = position;
        }

        @Override
        public void onAnimationEnd(@NonNull final Animator animation) {
            handleMenuOpened(mView, mPosition);
        }
    }

    private void handleMenuClosed() {
        if (mOpenedView == null) return;
        int direction  = mOpenedView.getCurrentDirection();
        mOpenedView.reset();
        mPreviousView = mOpenedView;
        mPreviousPosition = mOpenedPosition;
        mOpenedPosition = AdapterView.INVALID_POSITION;
        mOpenedView = null;
        afterCloseMenu(mPreviousView, mPreviousPosition, direction);
    }


    /**
     * An {@link com.nineoldandroids.animation.Animator.AnimatorListener} that performs the dismissal animation when the current animation has ended.
     */
    private AnimatorListenerAdapter mCloseAnimationListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(@NonNull final Animator animation) {
            handleMenuClosed();
        }
    };

    @Override
    public void onNotifyDataSetChanged() {
        handleMenuClosed();
    }
}
