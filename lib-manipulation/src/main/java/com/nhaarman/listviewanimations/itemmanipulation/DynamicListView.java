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

package com.nhaarman.listviewanimations.itemmanipulation;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.animateaddition.AnimateAdditionAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DragAndDropHandler;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DynamicListViewWrapper;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissTouchListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SwipeUndoTouchListener;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoCallback;
import com.nhaarman.listviewanimations.util.Insertable;

import java.util.Collection;
import java.util.HashSet;

/**
 * A {@link android.widget.ListView} implementation which provides the following functionality:
 * <ul>
 * <li>Drag and drop</li>
 * <li>Swipe to dismiss</li>
 * <li>Swipe to dismiss with contextual undo</li>
 * <li>Animate addition</li>
 * </ul>
 */
public class DynamicListView extends ListView {

    @NonNull
    private final MyOnScrollListener mMyOnScrollListener;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DragAndDropHandler}
     * that will handle drag and drop functionality, if set.
     */
    @Nullable
    private DragAndDropHandler mDragAndDropHandler;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListener}
     * that will handle swipe movement functionality, if set.
     */
    @Nullable
    private SwipeTouchListener mSwipeTouchListener;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.TouchEventHandler}
     * that is currently actively consuming {@code MotionEvent}s.
     */
    @Nullable
    private TouchEventHandler mCurrentHandlingTouchEventHandler;

    /**
     * The {@link com.nhaarman.listviewanimations.itemmanipulation.animateaddition.AnimateAdditionAdapter}
     * that is possibly set to animate insertions.
     */
    @Nullable
    private AnimateAdditionAdapter<Object> mAnimateAdditionAdapter;

    @Nullable
    private SwipeUndoAdapter mSwipeUndoAdapter;

    public DynamicListView(@NonNull final Context context) {
        this(context, null);
    }

    public DynamicListView(@NonNull final Context context, @Nullable final AttributeSet attrs) {
        //noinspection HardCodedStringLiteral
        this(context, attrs, Resources.getSystem().getIdentifier("listViewStyle", "attr", "android"));
    }

    public DynamicListView(@NonNull final Context context, @Nullable final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);

        mMyOnScrollListener = new MyOnScrollListener();
        super.setOnScrollListener(mMyOnScrollListener);
    }

    @Override
    public void setOnTouchListener(final OnTouchListener onTouchListener) {
        if (onTouchListener instanceof SwipeTouchListener) {
            return;
        }

        super.setOnTouchListener(onTouchListener);
    }

    @Override
    public void setOnScrollListener(final OnScrollListener onScrollListener) {
        mMyOnScrollListener.addOnScrollListener(onScrollListener);
    }

    /**
     * Enables the drag and drop functionality for this {@code DynamicListView}.
     * <p/>
     * <b>NOTE: This method can only be called on devices running ICS (14) and above, otherwise an exception will be thrown.</b>
     *
     * @throws java.lang.UnsupportedOperationException if the device uses an older API than 14.
     */
    public void enableDragAndDrop() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            throw new UnsupportedOperationException("Drag and drop is only supported API levels 14 and up!");
        }

        mDragAndDropHandler = new DragAndDropHandler(this);
    }

    /**
     * Disables the drag and drop functionality.
     */
    public void disableDragAndDrop() {
        mDragAndDropHandler = null;
    }

    /**
     * Enables swipe to dismiss functionality for this {@code DynamicListView}.
     *
     * @param onDismissCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback}
     *                          that is notified of dismissals.
     */
    public void enableSwipeToDismiss(@NonNull final OnDismissCallback onDismissCallback) {
        mSwipeTouchListener = new SwipeDismissTouchListener(new DynamicListViewWrapper(this), onDismissCallback);
    }

    /**
     * Enables swipe to dismiss with contextual undo for this {@code DynamicListView}.
     *
     * @param undoCallback the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoCallback}
     *                     that is used.
     */
    public void enableSwipeUndo(@NonNull final UndoCallback undoCallback) {
        mSwipeTouchListener = new SwipeUndoTouchListener(new DynamicListViewWrapper(this), undoCallback);
    }

    /**
     * Enables swipe to dismiss with contextual undo for this {@code DynamicListView}.
     * This method requires that a {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SwipeUndoAdapter} has been set.
     * It is allowed to have the {@code SwipeUndoAdapter} wrapped in a {@link com.nhaarman.listviewanimations.BaseAdapterDecorator}.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not extend {@code SwipeUndoAdapter}.
     */
    public void enableSimpleSwipeUndo() {
        if (mSwipeUndoAdapter == null) {
            throw new IllegalStateException("enableSimpleSwipeUndo requires a SwipeUndoAdapter to be set as an adapter");
        }

        mSwipeTouchListener = new SwipeUndoTouchListener(new DynamicListViewWrapper(this), mSwipeUndoAdapter.getUndoCallback());
        mSwipeUndoAdapter.setSwipeUndoTouchListener((SwipeUndoTouchListener) mSwipeTouchListener);
    }

    /**
     * Disables any swipe to dismiss functionality set by either {@link #enableSwipeToDismiss(com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback)},
     * {@link #enableSwipeUndo(com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoCallback)} or {@link #enableSimpleSwipeUndo()}.
     */
    public void disableSwipeToDismiss() {
        mSwipeTouchListener = null;
    }

    /**
     * Sets the {@link ListAdapter} for this {@code DynamicListView}.
     * If the drag and drop functionality is or will be enabled, the adapter should have stable ids,
     * and should implement {@link com.nhaarman.listviewanimations.util.Swappable}.
     *
     * @param adapter the adapter.
     *
     * @throws java.lang.IllegalStateException    if the drag and drop functionality is enabled
     *                                            and the adapter does not have stable ids.
     * @throws java.lang.IllegalArgumentException if the drag and drop functionality is enabled
     *                                            and the adapter does not implement {@link com.nhaarman.listviewanimations.util.Swappable}.
     */
    @Override
    public void setAdapter(final ListAdapter adapter) {
        ListAdapter wrappedAdapter = adapter;
        mSwipeUndoAdapter = null;

        if (adapter instanceof BaseAdapter) {
            BaseAdapter rootAdapter = (BaseAdapter) wrappedAdapter;
            while (rootAdapter instanceof BaseAdapterDecorator) {
                if (rootAdapter instanceof SwipeUndoAdapter) {
                    mSwipeUndoAdapter = (SwipeUndoAdapter) rootAdapter;
                }
                rootAdapter = ((BaseAdapterDecorator) rootAdapter).getDecoratedBaseAdapter();
            }

            if (rootAdapter instanceof Insertable) {
                mAnimateAdditionAdapter = new AnimateAdditionAdapter((BaseAdapter) wrappedAdapter);
                mAnimateAdditionAdapter.setListView(this);
                wrappedAdapter = mAnimateAdditionAdapter;
            }
        }

        super.setAdapter(wrappedAdapter);

        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.setAdapter(adapter);
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull final MotionEvent ev) {
        if (mCurrentHandlingTouchEventHandler == null) {
            /* None of the TouchEventHandlers are actively consuming events yet. */
            boolean firstTimeInteracting = false;

            /* We don't support dragging items when there are items in the undo state. */
            if (!(mSwipeTouchListener instanceof SwipeUndoTouchListener) || !((SwipeUndoTouchListener) mSwipeTouchListener).hasPendingItems()) {
                /* Offer the event to the DragAndDropHandler */
                if (mDragAndDropHandler != null) {
                    mDragAndDropHandler.onTouchEvent(ev);
                    firstTimeInteracting = mDragAndDropHandler.isInteracting();
                    if (firstTimeInteracting) {
                        mCurrentHandlingTouchEventHandler = mDragAndDropHandler;
                        sendCancelEvent(mSwipeTouchListener, ev);
                    }
                }
            }

            /* If not handled, offer the event to the SwipeDismissTouchListener */
            if (mCurrentHandlingTouchEventHandler == null && mSwipeTouchListener != null) {
                mSwipeTouchListener.onTouchEvent(ev);
                firstTimeInteracting = mSwipeTouchListener.isInteracting();
                if (firstTimeInteracting) {
                    mCurrentHandlingTouchEventHandler = mSwipeTouchListener;
                    sendCancelEvent(mDragAndDropHandler, ev);
                }
            }

            if (firstTimeInteracting) {
                /* One of the TouchEventHandlers is now taking over control.
                   Cancel touch event handling on this DynamicListView */
                MotionEvent cancelEvent = MotionEvent.obtain(ev);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                super.onTouchEvent(cancelEvent);
            }

            return firstTimeInteracting || super.dispatchTouchEvent(ev);
        } else {
            return onTouchEvent(ev);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent ev) {
        if (mCurrentHandlingTouchEventHandler != null) {
            mCurrentHandlingTouchEventHandler.onTouchEvent(ev);
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_UP || ev.getActionMasked() == MotionEvent.ACTION_CANCEL) {
            /* Gesture is finished, reset the active TouchEventHandler */
            mCurrentHandlingTouchEventHandler = null;
        }

        return mCurrentHandlingTouchEventHandler != null || super.onTouchEvent(ev);
    }

    /**
     * Sends a cancel event to given {@link com.nhaarman.listviewanimations.itemmanipulation.TouchEventHandler}.
     *
     * @param touchEventHandler the {@code TouchEventHandler} to send the event to.
     * @param motionEvent       the {@link MotionEvent} to base the cancel event on.
     */
    private void sendCancelEvent(@Nullable final TouchEventHandler touchEventHandler, @NonNull final MotionEvent motionEvent) {
        if (touchEventHandler != null) {
            MotionEvent cancelEvent = MotionEvent.obtain(motionEvent);
            cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
            touchEventHandler.onTouchEvent(cancelEvent);
        }
    }

    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.dispatchDraw(canvas);
        }
    }

    @Override
    public int computeVerticalScrollOffset() {
        return super.computeVerticalScrollOffset();
    }

    @Override
    public int computeVerticalScrollExtent() {
        return super.computeVerticalScrollExtent();
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    /* Proxy methods below */

    /**
     * Inserts an item at given index. Will show an entrance animation for the new item if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param index the index the new item should be inserted at.
     * @param item  the item to insert.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public void insert(final int index, final Object item) {
        if (mAnimateAdditionAdapter == null) {
            throw new IllegalStateException("Adapter should implement Insertable!");
        }
        mAnimateAdditionAdapter.insert(index, item);
    }

    /**
     * Inserts items, starting at given index. Will show an entrance animation for the new items if the newly added items are visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param index the starting index the new items should be inserted at.
     * @param items the items to insert.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public void insert(final int index, final Object... items) {
        if (mAnimateAdditionAdapter == null) {
            throw new IllegalStateException("Adapter should implement Insertable!");
        }
        mAnimateAdditionAdapter.insert(index, items);
    }

    /**
     * Inserts items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void insert(@NonNull final Pair<Integer, T>... indexItemPairs) {
        if (mAnimateAdditionAdapter == null) {
            throw new IllegalStateException("Adapter should implement Insertable!");
        }
        ((AnimateAdditionAdapter<T>) mAnimateAdditionAdapter).insert(indexItemPairs);
    }

    /**
     * Insert items at given indexes. Will show an entrance animation for the new items if the newly added item is visible.
     * Will also call {@link Insertable#add(int, Object)} of the root {@link android.widget.BaseAdapter}.
     *
     * @param indexItemPairs the index-item pairs to insert. The first argument of the {@code Pair} is the index, the second argument is the item.
     *
     * @throws java.lang.IllegalStateException if the adapter that was set does not implement {@link com.nhaarman.listviewanimations.util.Insertable}.
     */
    public <T> void insert(@NonNull final Iterable<Pair<Integer, T>> indexItemPairs) {
        if (mAnimateAdditionAdapter == null) {
            throw new IllegalStateException("Adapter should implement Insertable!");
        }
        ((AnimateAdditionAdapter<T>) mAnimateAdditionAdapter).insert(indexItemPairs);
    }

    /**
     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DraggableManager} to be used
     * for determining whether an item should be dragged when the user issues a down {@code MotionEvent}.
     * <p/>
     * This method does nothing if the drag and drop functionality is not enabled.
     */
    public void setDraggableManager(@NonNull final DraggableManager draggableManager) {
        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.setDraggableManager(draggableManager);
        }
    }

    /**
     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener}
     * that is notified when user has dropped a dragging item.
     * <p/>
     * This method does nothing if the drag and drop functionality is not enabled.
     */
    public void setOnItemMovedListener(@Nullable final OnItemMovedListener onItemMovedListener) {
        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.setOnItemMovedListener(onItemMovedListener);
        }
    }

    /**
     * Starts dragging the item at given position. User must be touching this {@code DynamicListView}.
     * <p/>
     * This method does nothing if the drag and drop functionality is not enabled.
     *
     * @param position the position of the item in the adapter to start dragging. Be sure to subtract any header views.
     *
     * @throws java.lang.IllegalStateException if the user is not touching this {@code DynamicListView},
     *                                         or if there is no adapter set.
     */
    public void startDragging(final int position) {
        /* We don't support dragging items when items are in the undo state. */
        if (mSwipeTouchListener instanceof SwipeUndoTouchListener && ((SwipeUndoTouchListener) mSwipeTouchListener).hasPendingItems()) {
            return;
        }

        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.startDragging(position);
        }
    }

    /**
     * Sets the scroll speed when dragging an item. Defaults to {@code 1.0f}.
     * <p/>
     * This method does nothing if the drag and drop functionality is not enabled.
     *
     * @param speed {@code <1.0f} to slow down scrolling, {@code >1.0f} to speed up scrolling.
     */
    public void setScrollSpeed(final float speed) {
        if (mDragAndDropHandler != null) {
            mDragAndDropHandler.setScrollSpeed(speed);
        }
    }

    /**
     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} to specify which views can or cannot be swiped.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     *
     * @param dismissableManager {@code null} for no restrictions.
     */
    public void setDismissableManager(@Nullable final DismissableManager dismissableManager) {
        if (mSwipeTouchListener != null) {
            mSwipeTouchListener.setDismissableManager(dismissableManager);
        }
    }

    /**
     * Flings the {@link android.view.View} corresponding to given position out of sight.
     * Calling this method has the same effect as manually swiping an item off the screen.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     *
     * @param position the position of the item in the {@link android.widget.ListAdapter}. Must be visible.
     */
    public void fling(final int position) {
        if (mSwipeTouchListener != null) {
            mSwipeTouchListener.fling(position);
        }
    }

    /**
     * Sets the resource id of a child view that should be touched to engage swipe.
     * When the user touches a region outside of that view, no swiping will occur.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     *
     * @param childResId The resource id of the list items' child that the user should touch to be able to swipe the list items.
     */
    public void setSwipeTouchChild(final int childResId) {
        if (mSwipeTouchListener != null) {
            mSwipeTouchListener.setTouchChild(childResId);
        }
    }

    /**
     * Sets the minimum value of the alpha property swiping Views should have.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     *
     * @param minimumAlpha the alpha value between 0.0f and 1.0f.
     */
    public void setMinimumAlpha(final float minimumAlpha) {
        if (mSwipeTouchListener != null) {
            mSwipeTouchListener.setMinimumAlpha(minimumAlpha);
        }
    }

    /**
     * Dismisses the {@link android.view.View} corresponding to given position.
     * Calling this method has the same effect as manually swiping an item off the screen.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     * It will however throw an exception if an incompatible swipe functionality is enabled.
     *
     * @param position the position of the item in the {@link android.widget.ListAdapter}. Must be visible.
     *
     * @throws java.lang.IllegalStateException if the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListener}
     *                                         that is enabled does not extend {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissTouchListener}.
     */
    public void dismiss(final int position) {
        if (mSwipeTouchListener != null) {
            if (mSwipeTouchListener instanceof SwipeDismissTouchListener) {
                ((SwipeDismissTouchListener) mSwipeTouchListener).dismiss(position);
            } else {
                throw new IllegalStateException("Enabled swipe functionality does not support dismiss");
            }
        }
    }

    /**
     * Performs the undo animation and restores the original state for given {@link android.view.View}.
     * <p/>
     * This method does nothing if no swipe functionality is enabled.
     * It will however throw an exception if an incompatible swipe functionality is enabled.
     *
     * @param view the parent {@code View} which contains both primary and undo {@code View}s.
     *
     * @throws java.lang.IllegalStateException if the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeTouchListener}
     *                                         that is enabled doe snot extend {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SwipeUndoTouchListener}.
     */
    public void undo(@NonNull final View view) {
        if (mSwipeTouchListener != null) {
            if (mSwipeTouchListener instanceof SwipeUndoTouchListener) {
                ((SwipeUndoTouchListener) mSwipeTouchListener).undo(view);
            } else {
                throw new IllegalStateException("Enabled swipe functionality does not support undo");
            }
        }
    }

    private class MyOnScrollListener implements OnScrollListener {

        private final Collection<OnScrollListener> mOnScrollListeners = new HashSet<>();

        @Override
        public void onScrollStateChanged(final AbsListView view, final int scrollState) {
            for (OnScrollListener onScrollListener : mOnScrollListeners) {
                onScrollListener.onScrollStateChanged(view, scrollState);
            }

            if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                if (mSwipeTouchListener instanceof SwipeUndoTouchListener) {
                    ((SwipeUndoTouchListener) mSwipeTouchListener).dimissPending();
                }
            }
        }

        @Override
        public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
            for (OnScrollListener onScrollListener : mOnScrollListeners) {
                onScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }

        public void addOnScrollListener(final OnScrollListener onScrollListener) {
            mOnScrollListeners.add(onScrollListener);
        }
    }
}