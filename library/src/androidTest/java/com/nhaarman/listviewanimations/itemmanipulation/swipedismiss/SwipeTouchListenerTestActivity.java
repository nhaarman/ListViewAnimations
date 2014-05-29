package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class SwipeTouchListenerTestActivity extends Activity {

    private ListView mListView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        mListView = new ListView(this);
        List<Integer> integers = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            integers.add(i);
        }

        ListAdapter myListAdapter = new MyListAdapter(this, integers);
        mListView.setAdapter(myListAdapter);

        setContentView(mListView);
    }

    public AbsListView getAbsListView() {
        return mListView;
    }

    private static class MyListAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        MyListAdapter(final Context context, final List<Integer> items) {
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
                view = new TextView(mContext);
                view.setTextSize(30);
            }

            view.setText("This is row number " + getItem(position));
            return view;
        }
    }
}
