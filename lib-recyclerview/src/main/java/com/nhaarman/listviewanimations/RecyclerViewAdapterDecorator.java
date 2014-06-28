package com.nhaarman.listviewanimations;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

public class RecyclerViewAdapterDecorator<T extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<T> {

    @NonNull
    private final RecyclerView.Adapter<T> mDecoratedAdapter;

    public RecyclerViewAdapterDecorator(@NonNull final RecyclerView.Adapter<T> decoratedAdapter) {
        mDecoratedAdapter = decoratedAdapter;
    }

    @NonNull
    public RecyclerView.Adapter<T> getDecoratedAdapter() {
        return mDecoratedAdapter;
    }

    @Override
    public T onCreateViewHolder(final ViewGroup viewGroup, final int i) {
        return mDecoratedAdapter.onCreateViewHolder(viewGroup, i);
    }

    @Override
    public void onBindViewHolder(final T viewHolder, final int i) {
        mDecoratedAdapter.onBindViewHolder(viewHolder, i);
    }

    @Override
    public int getItemCount() {
        return mDecoratedAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(final int position) {
        return mDecoratedAdapter.getItemViewType(position);
    }

    @Override
    public void setHasStableIds(final boolean hasStableIds) {
        mDecoratedAdapter.setHasStableIds(hasStableIds);
    }

    @Override
    public long getItemId(final int position) {
        return mDecoratedAdapter.getItemId(position);
    }

    @Override
    public void onViewRecycled(final T holder) {
        mDecoratedAdapter.onViewRecycled(holder);
    }

    @Override
    public void onViewAttachedToWindow(final T holder) {
        mDecoratedAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(final T holder) {
        mDecoratedAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void registerAdapterDataObserver(final RecyclerView.AdapterDataObserver observer) {
        mDecoratedAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void unregisterAdapterDataObserver(final RecyclerView.AdapterDataObserver observer) {
        mDecoratedAdapter.unregisterAdapterDataObserver(observer);
    }
}
