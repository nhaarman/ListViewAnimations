package com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite;

import android.content.Context;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.util.Swappable;

public class DynamicListView extends ListView {

    private static final int INVALID_ID = -1;

    @Nullable
    private HoverDrawable mHoverDrawable;

    @Nullable
    private View mMobileView;
    private long mMobileItemId;

    public DynamicListView(@NonNull final Context context) {
        super(context);
    }

    public DynamicListView(@NonNull final Context context, @NonNull final AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicListView(@NonNull final Context context, @NonNull final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(@NonNull final MotionEvent ev) {
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
            default:
                break;
        }

        return true;
//        return super.onTouchEvent(ev);
    }

    private void handleDownEvent(@NonNull final MotionEvent ev) {
        int position = pointToPosition((int) ev.getX(), (int) ev.getY());
        if (position != INVALID_POSITION) {
            mMobileItemId = getAdapter().getItemId(position);
            mMobileView = getChildAt(position - getFirstVisiblePosition());
            mHoverDrawable = new HoverDrawable(mMobileView, ev);
            mMobileView.setVisibility(INVISIBLE);
        }
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

    private void handleMoveEvent(@NonNull final MotionEvent ev) {
        if (mHoverDrawable != null) {
            mHoverDrawable.handleMoveEvent(ev);

            int position = getPositionForId(mMobileItemId);

            long aboveItemId = position - 1 >= 0 ? getAdapter().getItemId(position - 1) : INVALID_ROW_ID;
            long belowItemId = position + 1 < getAdapter().getCount() ? getAdapter().getItemId(position + 1) : INVALID_ROW_ID;

            final long switchId = mHoverDrawable.isMovingUpward() ? aboveItemId : belowItemId;
            View switchView = getViewForId(switchId);

            final int deltaY = mHoverDrawable.getDeltaY();
            if (switchView != null && Math.abs(deltaY) > mHoverDrawable.getIntrinsicHeight()) {

                final int switchViewPosition = getPositionForView(switchView);
                int mobileViewPosition = getPositionForView(mMobileView);

                ((Swappable) getAdapter()).swapItems(switchViewPosition, mobileViewPosition);
                ((BaseAdapter) getAdapter()).notifyDataSetChanged();

                mHoverDrawable.reset();
                mMobileView.setVisibility(VISIBLE);
                switchView.setVisibility(INVISIBLE);

                getViewTreeObserver().addOnPreDrawListener(
                        new ViewTreeObserver.OnPreDrawListener() {
                            @Override
                            public boolean onPreDraw() {
                                getViewTreeObserver().removeOnPreDrawListener(this);

                                View switchView = getViewForId(switchId);
                                switchView.setTranslationY(-deltaY);
                                switchView.animate().translationY(0).start();


                                mMobileView = getViewForId(mMobileItemId);
                                return true;
                            }
                        }
                );
            }


            invalidate();
        }
    }

    private void handleUpEvent() {
    }

    private void handleCancelEvent() {
    }

    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mHoverDrawable != null) {
            mHoverDrawable.draw(canvas);
        }
    }
}