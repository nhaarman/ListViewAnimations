package com.haarman.listviewanimations.itemmanipulation.contextualundo;

import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.haarman.listviewanimations.BaseAdapterDecorator;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.nineoldandroids.view.ViewHelper.setAlpha;
import static com.nineoldandroids.view.ViewHelper.setTranslationX;
import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

/**
 * Warning: a stable id for each item in the adapter is required.
 * The decorated adapter should not try to cast convertView to a particular view.
 * The undoLayout should have the same height as the content row.
 */
public class ContextualUndoAdapter extends BaseAdapterDecorator implements ContextualUndoListViewTouchListener.Callback {

    private final int undoLayoutId;
    private final int undoActionId;
    private final int animationTime = 350;
    private ContextualUndoView currentRemovedView;
    private long currentRemovedId;
    private Map<View, Animator> activeAnimators = new ConcurrentHashMap<View, Animator>();
    private DeleteItemCallback deleteItemCallback;

    public ContextualUndoAdapter(BaseAdapter baseAdapter, int undoLayoutId, int undoActionId) {
        super(baseAdapter);

        this.undoLayoutId = undoLayoutId;
        this.undoActionId = undoActionId;
        currentRemovedId = -1;
    }

    @Override
    public final View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            ContextualUndoView contextualUndoView = new ContextualUndoView(parent.getContext(), undoLayoutId);
            contextualUndoView.findViewById(undoActionId).setOnClickListener(makeUndoListener(contextualUndoView));
            convertView = contextualUndoView;
        }

        ContextualUndoView contextualUndoView = ContextualUndoView.from(convertView);
        View contentView = super.getView(position, contextualUndoView.getContentView(), parent);
        contextualUndoView.updateContentView(contentView);

        long itemId = getItemId(position);

        if (areEquals(itemId, currentRemovedId)) {
            contextualUndoView.displayUndo();
            currentRemovedView = contextualUndoView;
        } else {
            contextualUndoView.displayContentView();
        }

        contextualUndoView.setItemId(itemId);
        return contextualUndoView;
    }

    protected boolean areEquals(long firstId, long secondId) {
        return firstId == secondId;
    }

    @Override
    public void setListView(ListView listView) {
        super.setListView(listView);
        ContextualUndoListViewTouchListener contextualUndoListViewTouchListener = new ContextualUndoListViewTouchListener(listView, this);
        listView.setOnTouchListener(contextualUndoListViewTouchListener);
        listView.setOnScrollListener(contextualUndoListViewTouchListener.makeScrollListener());
        listView.setRecyclerListener(makeRecyclerListener());
    }

    @Override
    public void onViewSwiped(View dismissView, int dismissPosition) {
        ContextualUndoView contextualUndoView = ContextualUndoView.from(dismissView);
        //swipe the content view to show the contextual undo
        if (contextualUndoView.isContentDisplayed()) {
            restoreViewPosition(contextualUndoView);
            contextualUndoView.displayUndo();
            removePreviousContextualUndoIfPresent();
            setCurrentRemovedView(contextualUndoView);
        } else {
            //swipe again the undo view to confirm delete
            onListScrolled();
        }
    }

    private void restoreViewPosition(View view) {
        setAlpha(view, 1f);
        setTranslationX(view, 0);
    }

    private void removePreviousContextualUndoIfPresent() {
        //only one undo is allowed at a time
        if (currentRemovedView != null) {
            onListScrolled();
        }
    }

    private void setCurrentRemovedView(ContextualUndoView currentRemovedView) {
        this.currentRemovedView = currentRemovedView;
        currentRemovedId = currentRemovedView.getItemId();
    }

    private void clearCurrentRemovedView() {
        currentRemovedView = null;
        currentRemovedId = -1;
    }

    @Override
    public void onListScrolled() {
        if (currentRemovedView == null) {
            return;
        }

        final View dismissView = currentRemovedView;

        clearCurrentRemovedView();

        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();

        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(animationTime);

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                setAnimationInactive(animation);
                restoreViewPosition(dismissView);
                restoreViewDimension(dismissView);
                deleteCurrentItem();
            }

            private void setAnimationInactive(Animator animation) {
                activeAnimators.remove(animation);
            }

            private void restoreViewDimension(View view) {
                ViewGroup.LayoutParams lp;
                lp = view.getLayoutParams();
                lp.height = originalHeight;
                view.setLayoutParams(lp);
            }

            private void deleteCurrentItem() {
                ContextualUndoView contextualUndoView = ContextualUndoView.from(dismissView);
                long deleteItemId = contextualUndoView.getItemId();
                for (int i = 0; i < getCount(); i++) {
                    if (areEquals(getItemId(i), deleteItemId)) {
                        deleteItemCallback.deleteItem(getItem(i));
                    }
                }
            }
        });

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });

        animator.start();
        activeAnimators.put(dismissView, animator);
    }

    public AbsListView.RecyclerListener makeRecyclerListener() {
        return new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                Animator animator = activeAnimators.get(view);
                if (animator != null) {
                    animator.cancel();
                }
            }
        };
    }

    public long getCurrentRemovedId() {
        return currentRemovedId;
    }

    public void setDeleteItemCallback(DeleteItemCallback deleteItemCallback) {
        this.deleteItemCallback = deleteItemCallback;
    }

    private View.OnClickListener makeUndoListener(ContextualUndoView contextualUndoView) {
        return new UndoListener(contextualUndoView);
    }

    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putLong("currentRemovedId", currentRemovedId);
        return bundle;
    }

    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        currentRemovedId = bundle.getLong("currentRemovedId", -1);
    }

    public interface DeleteItemCallback {
        void deleteItem(Object itemId);
    }

    private class UndoListener implements View.OnClickListener {

        private final ContextualUndoView contextualUndoView;

        public UndoListener(ContextualUndoView contextualUndoView) {
            this.contextualUndoView = contextualUndoView;
        }

        @Override
        public void onClick(View v) {
            clearCurrentRemovedView();
            contextualUndoView.displayContentView();
            moveViewOffScreen();
            animateViewComingBack();

        }

        private void moveViewOffScreen() {
            setTranslationX(contextualUndoView, contextualUndoView.getWidth());
        }

        private void animateViewComingBack() {
            animate(contextualUndoView)
                    .translationX(0)
                    .setDuration(animationTime)
                    .setListener(null);
        }
    }
}