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

import java.util.ArrayList;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

public class AppearanceExamplesActivity extends MyListActivity implements OnNavigationListener {

	private BaseAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mAdapter = new MyAdapter(this, getItems());
		setAlphaAdapter();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(new AnimSelectionAdapter(), this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
	}

	private void setAlphaAdapter() {
		AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private void setLeftAdapter() {
		AnimationAdapter animAdapter = new SwingLeftInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private void setRightAdapter() {
		AnimationAdapter animAdapter = new SwingRightInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private void setBottomAdapter() {
		AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private void setBottomRightAdapter() {
		AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(mAdapter));
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private void setScaleAdapter() {
		AnimationAdapter animAdapter = new ScaleInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	private static class MyAdapter extends ArrayAdapter<Integer> {

		private Context mContext;

		public MyAdapter(Context context, ArrayList<Integer> items) {
			super(items);
			mContext = context;
		}

		@Override
		public long getItemId(int position) {
			return getItem(position).hashCode();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
			}
			tv.setText("This is row number " + getItem(position));
			return tv;
		}
	}

	/* Non-ListViewAnimations related stuff below */

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
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

	private class AnimSelectionAdapter extends ArrayAdapter<String> {

		public AnimSelectionAdapter() {
			addAll("Alpha", "Left", "Right", "Bottom", "Bottom right", "Scale");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(AppearanceExamplesActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
			}

			tv.setText(getItem(position));

			return tv;
		}
	}
}
