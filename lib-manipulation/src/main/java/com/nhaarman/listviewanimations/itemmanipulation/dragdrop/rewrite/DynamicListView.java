package com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite;

import android.annotation.TargetApi;
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
     * The id of the item view that is being dragged.
     * This value is {@value #INVALID_ID} if and only if the user is not dragging.
     */
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
        if (mHoverDrawable == null) {
            return;
        }
        mHoverDrawable.handleMoveEvent(ev);

        int position = getPositionForId(mMobileItemId);
        long aboveItemId = position - 1 >= 0 ? getAdapter().getItemId(position - 1) : INVALID_ROW_ID;
        long belowItemId = position + 1 < getAdapter().getCount() ? getAdapter().getItemId(position + 1) : INVALID_ROW_ID;

        final long switchId = mHoverDrawable.isMovingUpwards() ? aboveItemId : belowItemId;
        View switchView = getViewForId(switchId);

        final int deltaY = mHoverDrawable.getDeltaY();
        if (switchView != null && Math.abs(deltaY) > mHoverDrawable.getIntrinsicHeight()) {
            switchViews(switchView, switchId, deltaY);
        }
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

    private void handleUpEvent() {
        if (mMobileView == null) {
            return;
        }
        assert mHoverDrawable != null;

        mMobileView.setVisibility(VISIBLE);
        mMobileView.setTranslationY(mHoverDrawable.getDeltaY());
        mMobileView.animate().translationY(0).start();

        mHoverDrawable = null;
        mMobileView = null;
        mMobileItemId = INVALID_ID;
    }

    private void handleCancelEvent() {
        handleUpEvent();
    }

    @Override
    protected void dispatchDraw(@NonNull final Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mHoverDrawable != null) {
            mHoverDrawable.draw(canvas);
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
}