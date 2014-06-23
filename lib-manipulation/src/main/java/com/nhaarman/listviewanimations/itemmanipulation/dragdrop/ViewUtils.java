package com.nhaarman.listviewanimations.itemmanipulation.dragdrop;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;

class ViewUtils {

    private ViewUtils() {
    }

    @SuppressWarnings("ObjectEquality")
    static Rect getChildViewRect(final View parentView, final View childView) {
        final Rect childRect = new Rect(childView.getLeft(), childView.getTop(), childView.getRight(), childView.getBottom());
        if (parentView == childView) {
            return childRect;
        }

        View view = childView;
        ViewGroup parent;
        while ((parent = (ViewGroup) view.getParent()) != parentView) {
            childRect.offset(parent.getLeft(), parent.getTop());
            view = parent;
        }

        return childRect;
    }

}
