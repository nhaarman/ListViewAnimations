package com.haarman.listviewanimations;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;

public class MyListAdapter extends ArrayAdapter<String> implements UndoAdapter, StickyListHeadersAdapter {

    private final Context mContext;

    public MyListAdapter(final Context context) {
        mContext = context;
        for (int i = 0; i < 1000; i++) {
            add(mContext.getString(R.string.row_number, i));
        }
    }

    @Override
    public long getItemId(final int position) {
        return getItem(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
        }

        view.setText(getItem(position));

        return view;
    }

    @NonNull
    @Override
    public View getUndoView(final int position, final View convertView, @NonNull final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.undo_row, parent, false);
        }
        return view;
    }

    @NonNull
    @Override
    public View getUndoClickView(@NonNull final View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }

    @Override
    public View getHeaderView(final int position, final View convertView, final ViewGroup parent) {
        TextView view = (TextView) convertView;
        if (view == null) {
            view = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_header, parent, false);
        }

        view.setText(mContext.getString(R.string.header, getHeaderId(position)));

        return view;
    }

    @Override
    public long getHeaderId(final int position) {
        return position / 10;
    }
}