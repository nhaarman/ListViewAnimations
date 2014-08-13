/*
 * Copyright 2014 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
