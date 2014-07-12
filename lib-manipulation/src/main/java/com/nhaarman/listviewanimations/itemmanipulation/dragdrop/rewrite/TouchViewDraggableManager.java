package com.nhaarman.listviewanimations.itemmanipulation.dragdrop.rewrite;

import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;

public class TouchViewDraggableManager implements DraggableManager {

    @IdRes
    private final int mTouchViewResId;

    public TouchViewDraggableManager(@IdRes final int touchViewResId) {
        mTouchViewResId = touchViewResId;
    }

    @Override
    public boolean isDraggable(@NonNull final View view, final int position, final float x, final float y) {
        View touchView = view.findViewById(mTouchViewResId);
        boolean xHit = touchView.getLeft() <= x && touchView.getRight() >= x;
        boolean yHit = touchView.getTop() <= y && touchView.getBottom() >= y;
        return xHit && yHit;
    }
}
