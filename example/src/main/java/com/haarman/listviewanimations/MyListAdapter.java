package com.haarman.listviewanimations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import java.util.List;

public class MyListAdapter extends ArrayAdapter<Integer> implements UndoAdapter {

    private final Context mContext;

    public MyListAdapter(final Context context, final List<Integer> items) {
        super(items);
        mContext = context;
    }

    @Override
    public long getItemId(final int location) {
        return getItem(location).hashCode();
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

        view.setText("This is row number " + getItem(position));
        return view;
    }

    @Override
    public View getUndoView(final int position, final View convertView, final ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.undo_row, parent, false);
        }
        return view;
    }

    @Override
    public View getUndoClickView(final View view) {
        return view.findViewById(R.id.undo_row_undobutton);
    }
}