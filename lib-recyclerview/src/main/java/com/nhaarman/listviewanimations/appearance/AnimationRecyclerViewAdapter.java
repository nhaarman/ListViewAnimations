package com.nhaarman.listviewanimations.appearance;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.RecyclerViewAdapterDecorator;
import com.nhaarman.listviewanimations.util.AnimatorUtil;
import com.nhaarman.listviewanimations.util.RecyclerViewWrapper;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;

public abstract class AnimationRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends RecyclerViewAdapterDecorator<T> {

    private static final String ALPHA = "alpha";

    @Nullable
    private ViewAnimator mViewAnimator;

    @Nullable
    private RecyclerView mRecyclerView;

    protected AnimationRecyclerViewAdapter(@NonNull final RecyclerView.Adapter<T> decoratedAdapter) {
        super(decoratedAdapter);
    }

    public void setRecyclerView(@NonNull final RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
        mViewAnimator = new ViewAnimator(new RecyclerViewWrapper(recyclerView));
    }

    @Nullable
    public ViewAnimator getViewAnimator() {
        return mViewAnimator;
    }

    @Override
    public void onBindViewHolder(final T viewHolder, final int position) {
        if (mViewAnimator == null) {
            throw new IllegalStateException("No RecyclerView set. Make sur you call AnimationRecyclerViewAdapter#setRecyclerView(RecyclerView).");
        }

        super.onBindViewHolder(viewHolder, position);
        mViewAnimator.cancelExistingAnimation(viewHolder.itemView);
        animateView(viewHolder.itemView, position);
    }

    private void animateView(final View view, final int position) {
        assert mViewAnimator != null;
        assert mRecyclerView != null;

        Animator[] childAnimators;
        if (getDecoratedAdapter() instanceof AnimationRecyclerViewAdapter) {
            childAnimators = ((AnimationRecyclerViewAdapter<?>) getDecoratedAdapter()).getAnimators(mRecyclerView, view);
        } else {
            childAnimators = new Animator[0];
        }
        Animator[] animators = getAnimators(mRecyclerView, view);
        Animator alphaAnimator = ObjectAnimator.ofFloat(view, ALPHA, 0, 1);

        Animator[] concatAnimators = AnimatorUtil.concatAnimators(childAnimators, animators, alphaAnimator);
        mViewAnimator.animateViewIfNecessary(position, view, concatAnimators);
    }

    /**
     * Returns the Animators to apply to the views. In addition to the returned Animators, an alpha transition will be applied to the view.
     *
     * @param parent The parent of the view
     * @param view   The view that will be animated, as retrieved by getView().
     */
    @NonNull
    public abstract Animator[] getAnimators(@NonNull ViewGroup parent, @NonNull View view);

}
