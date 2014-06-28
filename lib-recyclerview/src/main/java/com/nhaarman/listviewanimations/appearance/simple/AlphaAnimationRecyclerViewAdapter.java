package com.nhaarman.listviewanimations.appearance.simple;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.nhaarman.listviewanimations.appearance.AnimationRecyclerViewAdapter;
import com.nineoldandroids.animation.Animator;

public class AlphaAnimationRecyclerViewAdapter<T extends RecyclerView.ViewHolder> extends AnimationRecyclerViewAdapter<T> {

    public AlphaAnimationRecyclerViewAdapter(@NonNull final RecyclerView.Adapter<T> decoratedAdapter) {
        super(decoratedAdapter);
    }

    @NonNull
    @Override
    public Animator[] getAnimators(@NonNull final ViewGroup parent, @NonNull final View view) {
        return new Animator[0];
    }
}
