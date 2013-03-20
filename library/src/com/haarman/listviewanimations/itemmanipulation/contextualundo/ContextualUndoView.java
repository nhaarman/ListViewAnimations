package com.haarman.listviewanimations.itemmanipulation.contextualundo;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

class ContextualUndoView extends FrameLayout {

    private View undoView;
    private View contentView;
    private long itemId;

    ContextualUndoView(Context context, int undoLayoutResourceId) {
        super(context);
        initUndo(undoLayoutResourceId);
    }

    void initUndo(int undoLayoutResourceId) {
        undoView = View.inflate(getContext(), undoLayoutResourceId, null);
        addView(undoView);
    }

    static ContextualUndoView from(View view) {
        return (ContextualUndoView) view;
    }

    void updateContentView(View contentView) {
        if (this.contentView == null) {
            addView(contentView);
        }
        this.contentView = contentView;
    }

    View getContentView() {
        return contentView;
    }

    void setItemId(long itemId) {
        this.itemId = itemId;
    }

    long getItemId() {
        return itemId;
    }

    boolean isContentDisplayed() {
        return contentView.getVisibility() == View.VISIBLE;
    }

    void displayUndo() {
        contentView.setVisibility(View.GONE);
        undoView.setVisibility(View.VISIBLE);
    }

    void displayContentView() {
        contentView.setVisibility(View.VISIBLE);
        undoView.setVisibility(View.GONE);
    }
}