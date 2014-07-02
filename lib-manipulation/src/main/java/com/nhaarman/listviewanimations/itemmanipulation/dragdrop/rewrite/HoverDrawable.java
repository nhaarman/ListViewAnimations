package com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite;

import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;

class HoverDrawable extends BitmapDrawable {

    /**
     * The original y coordinate of the top of given {@code View}.
     */
    private float mOriginalY;

    /**
     * The original y coordinate of the position that was touched.
     */
    private float mDownY;

    HoverDrawable(@NonNull final View view, @NonNull final MotionEvent ev) {
        super(view.getResources(), BitmapUtils.getBitmapFromView(view));
        mOriginalY = view.getTop();
        mDownY = ev.getY();

        setBounds(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    void handleMoveEvent(@NonNull final MotionEvent ev) {
        int top = (int) (mOriginalY - mDownY + ev.getY());
        setBounds(getBounds().left, top, getBounds().left + getIntrinsicWidth(), top + getIntrinsicHeight());
    }

    boolean isMovingUpward() {
        return mOriginalY > getBounds().top;
    }

    int getDeltaY() {
        return (int) (mOriginalY - getBounds().top);
    }

    void reset() {
        int deltaY = getDeltaY();
        mOriginalY -= deltaY;
        mDownY -= deltaY;
    }

}
