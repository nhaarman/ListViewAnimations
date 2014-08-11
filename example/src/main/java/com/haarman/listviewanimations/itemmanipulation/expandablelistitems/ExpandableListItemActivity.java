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
package com.haarman.listviewanimations.itemmanipulation.expandablelistitems;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;

public class ExpandableListItemActivity extends MyListActivity {

    private static final int INITIAL_DELAY_MILLIS = 500;
    private MyExpandableListItemAdapter mExpandableListItemAdapter;

    private boolean mLimited;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mExpandableListItemAdapter = new MyExpandableListItemAdapter(this);
        AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mExpandableListItemAdapter);
        alphaInAnimationAdapter.setAbsListView(getListView());

        assert alphaInAnimationAdapter.getViewAnimator() != null;
        alphaInAnimationAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);

        getListView().setAdapter(alphaInAnimationAdapter);

        Toast.makeText(this, R.string.explainexpand, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_expandablelistitem, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_expandable_limit:
                mLimited = !mLimited;
                item.setChecked(mLimited);
                mExpandableListItemAdapter.setLimit(mLimited ? 2 : 0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
