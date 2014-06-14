package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class StickyListHeadersAdapterDecorator extends BaseAdapterDecorator<StickyListHeadersListView> implements StickyListHeadersAdapter {

    @NonNull
    private final StickyListHeadersAdapter mStickyListHeadersAdapter;

    /**
     * Create a new {@code BaseAdapterDecorator}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter the {@code} BaseAdapter to decorate.
     */
    public StickyListHeadersAdapterDecorator(@NonNull final BaseAdapter baseAdapter) {
        super(baseAdapter);

        BaseAdapter adapter = baseAdapter;
        while (adapter instanceof BaseAdapterDecorator) {
            adapter = ((BaseAdapterDecorator<StickyListHeadersListView>) adapter).getDecoratedBaseAdapter();
        }

        if (!(adapter instanceof StickyListHeadersAdapter)) {
            throw new IllegalArgumentException(adapter.getClass().getCanonicalName() + " does not implement StickyListHeadersAdapter");
        }

        mStickyListHeadersAdapter = (StickyListHeadersAdapter) adapter;
    }

    @Override
    public View getHeaderView(final int i, final View view, final ViewGroup viewGroup) {
        return mStickyListHeadersAdapter.getHeaderView(i, view, viewGroup);
    }

    @Override
    public long getHeaderId(final int i) {
        return mStickyListHeadersAdapter.getHeaderId(i);
    }
}
