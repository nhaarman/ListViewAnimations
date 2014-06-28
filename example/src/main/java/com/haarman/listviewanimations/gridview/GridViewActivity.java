package com.haarman.listviewanimations.gridview;

import android.os.Bundle;
import android.widget.GridView;

import com.haarman.listviewanimations.BaseActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

public class GridViewActivity extends BaseActivity {

    private static final int INITIAL_DELAY_MILLIS = 300;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gridview);

        GridView gridView = (GridView) findViewById(R.id.activity_gridview_gv);
        SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new GridViewAdapter(this));
        swingBottomInAnimationAdapter.setAbsListView(gridView);

        assert swingBottomInAnimationAdapter.getViewAnimator() != null;
        swingBottomInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        gridView.setAdapter(swingBottomInAnimationAdapter);

        assert getActionBar() != null;
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
