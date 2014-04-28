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
package com.haarman.listviewanimations.appearanceexamples;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;
import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

import java.util.ArrayList;

public class AppearanceExamplesActivity extends MyListActivity implements OnNavigationListener {

    private static final String SAVEDINSTANCESTATE_ANIMATIONADAPTER = "savedinstancestate_animationadapter";

    private BaseAdapter mAdapter;
    private AnimationAdapter mAnimAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MyAdapter(this, getItems());
        setAlphaAdapter();

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getSupportActionBar().setListNavigationCallbacks(new AnimSelectionAdapter(), this);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        outState.putParcelable(SAVEDINSTANCESTATE_ANIMATIONADAPTER, mAnimAdapter.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAnimAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(SAVEDINSTANCESTATE_ANIMATIONADAPTER));
    }

    private void setAlphaAdapter() {
        if (!(mAnimAdapter instanceof AlphaInAnimationAdapter)) {
            mAnimAdapter = new AlphaInAnimationAdapter(mAdapter);
            mAnimAdapter.setAbsListView(getListView());
            getListView().setAdapter(mAnimAdapter);
        }
    }

    private void setLeftAdapter() {
        if (!(mAnimAdapter instanceof SwingLeftInAnimationAdapter)) {
            mAnimAdapter = new SwingLeftInAnimationAdapter(mAdapter);
            mAnimAdapter.setAbsListView(getListView());
            getListView().setAdapter(mAnimAdapter);
        }
    }

    private void setRightAdapter() {
        if (!(mAnimAdapter instanceof SwingRightInAnimationAdapter)) {
            mAnimAdapter = new SwingRightInAnimationAdapter(mAdapter);
            mAnimAdapter.setAbsListView(getListView());
            getListView().setAdapter(mAnimAdapter);
        }
    }

    private void setBottomAdapter() {
        if (!(mAnimAdapter instanceof SwingBottomInAnimationAdapter)) {
            mAnimAdapter = new SwingBottomInAnimationAdapter(mAdapter);
            mAnimAdapter.setAbsListView(getListView());
            getListView().setAdapter(mAnimAdapter);
        }
    }

    private void setBottomRightAdapter() {
        mAnimAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(mAdapter));
        mAnimAdapter.setAbsListView(getListView());
        getListView().setAdapter(mAnimAdapter);
    }

    private void setScaleAdapter() {
        if (!(mAnimAdapter instanceof ScaleInAnimationAdapter)) {
            mAnimAdapter = new ScaleInAnimationAdapter(mAdapter);
            mAnimAdapter.setAbsListView(getListView());
            getListView().setAdapter(mAnimAdapter);
        }
    }

    @Override
    public boolean onNavigationItemSelected(final int itemPosition, final long itemId) {
        switch (itemPosition) {
            case 0:
                setAlphaAdapter();
                return true;
            case 1:
                setLeftAdapter();
                return true;
            case 2:
                setRightAdapter();
                return true;
            case 3:
                setBottomAdapter();
                return true;
            case 4:
                setBottomRightAdapter();
                return true;
            case 5:
                setScaleAdapter();
                return true;
            default:
                return false;
        }
    }

    /* Non-ListViewAnimations related stuff below */

    private static class MyAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        public MyAdapter(final Context context, final ArrayList<Integer> items) {
            super(items);
            mContext = context;
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
            }
            tv.setText("This is row number " + getItem(position));
            return tv;
        }
    }

    private class AnimSelectionAdapter extends ArrayAdapter<String> {

        public AnimSelectionAdapter() {
            addAll("Alpha", "Left", "Right", "Bottom", "Bottom right", "Scale");
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(AppearanceExamplesActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
            }

            tv.setText(getItem(position));

            return tv;
        }
    }
}
