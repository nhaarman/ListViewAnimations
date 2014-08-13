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
package com.haarman.listviewanimations.appearance;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.MyListAdapter;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.appearance.AnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.ScaleInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingLeftInAnimationAdapter;
import com.nhaarman.listviewanimations.appearance.simple.SwingRightInAnimationAdapter;

import java.util.Arrays;

public class AppearanceExamplesActivity extends MyListActivity implements ActionBar.OnNavigationListener {

    private static final String SAVEDINSTANCESTATE_ANIMATIONADAPTER = "savedinstancestate_animationadapter";

    private BaseAdapter mAdapter;

    private AnimationAdapter mAnimAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new MyListAdapter(this);
        setAlphaAdapter();

        assert getActionBar() != null;
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        getActionBar().setDisplayShowTitleEnabled(false);

        SpinnerAdapter animSelectionAdapter = new AnimSelectionAdapter(this);
        getActionBar().setListNavigationCallbacks(animSelectionAdapter, this);
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
    protected void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putParcelable(SAVEDINSTANCESTATE_ANIMATIONADAPTER, mAnimAdapter.onSaveInstanceState());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mAnimAdapter.onRestoreInstanceState(savedInstanceState.getParcelable(SAVEDINSTANCESTATE_ANIMATIONADAPTER));
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

    private static class AnimSelectionAdapter extends ArrayAdapter<String> {

        private final Context mContext;

        AnimSelectionAdapter(@NonNull final Context context) {
            mContext = context;
            String[] items = context.getResources().getStringArray(R.array.appearance_examples);
            addAll(Arrays.asList(items));
        }

        @Override
        public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
                tv.setTextColor(mContext.getResources().getColor(android.R.color.white));
            }

            tv.setText(getItem(position));

            return tv;
        }
    }
}
