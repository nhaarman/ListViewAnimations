package com.nhaarman.listviewanimations;

import com.nhaarman.listviewanimations.util.OnNotifyDataSetChanged;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class ListViewAnimationsBaseAdapter extends BaseAdapter {
    private OnNotifyDataSetChanged mOnNotifyDataSetChanged;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        return null;
    }
    

    public void setOnNotifyDataSetChanged(final OnNotifyDataSetChanged onNotifyDataSetChanged) {
        mOnNotifyDataSetChanged = onNotifyDataSetChanged;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if (mOnNotifyDataSetChanged != null) {
            mOnNotifyDataSetChanged.onNotifyDataSetChanged();
        }
    }
}
