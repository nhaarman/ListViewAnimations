/*
 * Copyright 2013 Niek Haarman
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
package com.haarman.listviewanimations.itemmanipulationexamples;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.MyListAdapter;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.SwipeDismissAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.TimedUndoAdapter;

import android.support.annotation.NonNull;

import java.util.Arrays;

public class SwipeDismissActivity extends MyListActivity implements OnNavigationListener, OnDismissCallback {

    private MyListAdapter mAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = createListAdapter();

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(new AnimSelectionAdapter(), this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private void setSwipeDismissAdapter() {
        SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
        adapter.setAbsListView(getListView());
        getListView().setAdapter(adapter);
    }

    private void setContextualUndoAdapter() {
        SimpleSwipeUndoAdapter adapter = new SimpleSwipeUndoAdapter(mAdapter, this, this);
        adapter.setAbsListView(getListView());
        getListView().setAdapter(adapter);
    }

    private void setContextualUndoWithTimedDeleteAdapter() {
        TimedUndoAdapter adapter = new TimedUndoAdapter(mAdapter, this, this);
        adapter.setAbsListView(getListView());
        getListView().setAdapter(adapter);
    }

    @Override
    public void onDismiss(@NonNull final AbsListView absListView, @NonNull final int[] reverseSortedPositions) {
        for (int position : reverseSortedPositions) {
            mAdapter.remove(position);
        }
        Toast.makeText(this, "Removed positions: " + Arrays.toString(reverseSortedPositions), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
        switch (itemPosition) {
            case 0:
                setSwipeDismissAdapter();
                return true;
            case 1:
                setContextualUndoAdapter();
                return true;
            case 2:
                setContextualUndoWithTimedDeleteAdapter();
                return true;
            default:
                return false;
        }
    }

    private class AnimSelectionAdapter extends ArrayAdapter<String> {

        AnimSelectionAdapter() {
            addAll("Swipe-To-Dismiss", "Contextual Undo", "CU - Timed Delete");
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(SwipeDismissActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            tv.setText(getItem(position));

            return tv;
        }
    }
}
