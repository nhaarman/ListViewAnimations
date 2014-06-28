package com.haarman.listviewanimations;

import android.os.Bundle;

import com.nhaarman.listviewanimations.appearance.StickyListHeadersAdapterDecorator;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.util.StickyListHeadersListViewWrapper;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;

public class StickyListHeadersActivity extends BaseActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stickylistheaders);

        StickyListHeadersListView listView = (StickyListHeadersListView) findViewById(R.id.activity_stickylistheaders_listview);
        listView.setFitsSystemWindows(true);

        MyListAdapter adapter = new MyListAdapter(this);
        AlphaInAnimationAdapter animationAdapter = new AlphaInAnimationAdapter(adapter);
        StickyListHeadersAdapterDecorator stickyListHeadersAdapterDecorator = new StickyListHeadersAdapterDecorator(animationAdapter);
        stickyListHeadersAdapterDecorator.setListViewWrapper(new StickyListHeadersListViewWrapper(listView));

        assert animationAdapter.getViewAnimator() != null;
        animationAdapter.getViewAnimator().setInitialDelayMillis(500);

        assert stickyListHeadersAdapterDecorator.getViewAnimator() != null;
        stickyListHeadersAdapterDecorator.getViewAnimator().setInitialDelayMillis(500);

        listView.setAdapter(stickyListHeadersAdapterDecorator);
    }
}
