package com.haarman.listviewanimations;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;

import android.support.annotation.NonNull;

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

        int backgroundColorResId;
        switch (getItem(position) % 4) {
            case 0:
                backgroundColorResId = R.color.holo_red_light_transparent;
                break;
            case 1:
                backgroundColorResId = R.color.holo_blue_light_transparent;
                break;
            case 2:
                backgroundColorResId = R.color.holo_green_light_transparent;
                break;
            default:
                backgroundColorResId = R.color.holo_purple_light_transparent;
        }
        view.setBackgroundColor(mContext.getResources().getColor(backgroundColorResId));

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
}