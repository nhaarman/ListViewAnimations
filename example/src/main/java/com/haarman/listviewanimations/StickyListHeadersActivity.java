package com.haarman.listviewanimations;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.util.StickyListHeadersAdapterDecorator;
import com.nhaarman.listviewanimations.util.StickyListHeadersListViewWrapper;

import se.emilsjolander.stickylistheaders.StickyListHeadersAdapter;
import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class StickyListHeadersActivity extends Activity {

    private StickyListHeadersListView mListView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = new StickyListHeadersListView(this);
        setContentView(mListView);
        MyAdapter integers = new MyAdapter();
        for (int i = 0; i < 100; i++) {
            integers.add(i);
        }

        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(integers);
        StickyListHeadersAdapterDecorator stickyListHeadersAdapterDecorator = new StickyListHeadersAdapterDecorator(animationAdapter);
        stickyListHeadersAdapterDecorator.setListViewWrapper(new StickyListHeadersListViewWrapper(mListView));
        mListView.setAdapter(stickyListHeadersAdapterDecorator);
    }


    private class MyAdapter extends ArrayAdapter<Integer> implements StickyListHeadersAdapter {

        @Override
        public View getHeaderView(final int i, final View view, final ViewGroup viewGroup) {
            TextView tv = new TextView(StickyListHeadersActivity.this);
            tv.setText("Header");
            return tv;
        }

        @Override
        public long getHeaderId(final int i) {
            return i;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView tv = new TextView(StickyListHeadersActivity.this);
            tv.setText("Position: " + position);
            return tv;
        }
    }
}
