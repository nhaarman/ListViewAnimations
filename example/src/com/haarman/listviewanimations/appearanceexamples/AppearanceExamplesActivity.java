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
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.BaseActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.swinginadapters.AnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.ScaleInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingLeftInAnimationAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

public class AppearanceExamplesActivity extends BaseActivity implements OnNavigationListener {

	private BaseAdapter mAdapter;

	private ListView mListView;
	private ListView mStickyListView;

	private ListView mCurrentListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appearanceexample);

		mListView = (ListView) findViewById(R.id.activity_appearanceexample_listview);
		mStickyListView = (ListView) findViewById(R.id.activity_appearanceexample_stickylistview);

		mCurrentListView = mListView;
		mStickyListView.setVisibility(View.GONE);

		mAdapter = new MyStickyListAdapter(this, getItems());

		setAlphaAdapter();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(new AnimSelectionAdapter(), this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);

		Toast.makeText(this, "You can turn StickyListHeaders on or off in the menu", Toast.LENGTH_LONG).show();
	}

	private void setAlphaAdapter() {
		AnimationAdapter animAdapter = new AlphaInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private void setLeftAdapter() {
		AnimationAdapter animAdapter = new SwingLeftInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private void setRightAdapter() {
		AnimationAdapter animAdapter = new SwingRightInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private void setBottomAdapter() {
		AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private void setBottomRightAdapter() {
		AnimationAdapter animAdapter = new SwingBottomInAnimationAdapter(new SwingRightInAnimationAdapter(mAdapter));
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private void setScaleAdapter() {
		AnimationAdapter animAdapter = new ScaleInAnimationAdapter(mAdapter);
		animAdapter.setAbsListView(mCurrentListView);
		mCurrentListView.setAdapter(animAdapter);
	}

	private static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}
		return items;
	}

	private static class MyStickyListAdapter extends ArrayAdapter<Integer> implements StickyListHeadersAdapter {

		private Context mContext;

		public MyStickyListAdapter(Context context, ArrayList<Integer> items) {
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

		@Override
		public long getHeaderId(int position) {
			return getItem(position) / 10;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
				tv.setBackgroundColor(Color.CYAN);
			}
			tv.setText(String.valueOf(getItem(position) / 10));
			return tv;
		}
	}

	/* Non-ListViewAnimations related stuff below */

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_appearance, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_appearance_sticky:
			switchListViews();
			item.setChecked(mCurrentListView == mStickyListView);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void switchListViews() {
		mListView.setVisibility(mCurrentListView == mListView ? View.GONE : View.VISIBLE);
		mStickyListView.setVisibility(mCurrentListView == mListView ? View.VISIBLE : View.GONE);
		mCurrentListView = mCurrentListView == mListView ? mStickyListView : mListView;

		onNavigationItemSelected(getSupportActionBar().getSelectedNavigationIndex(), -1);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		switch (itemPosition) {
		case 0:
			setAlphaAdapter();
			return true;
		case 1:
			setLeftAdapter();
			if (mCurrentListView == mStickyListView) {
				Toast.makeText(this, "Due to StickyListHeaders limitations the SwingLeftInAdapter can't work properly", Toast.LENGTH_LONG).show();
			}
			return true;
		case 2:
			setRightAdapter();
			if (mCurrentListView == mStickyListView) {
				Toast.makeText(this, "Due to StickyListHeaders limitations the SwingRightInAdapter can't work properly", Toast.LENGTH_LONG).show();
			}
			return true;
		case 3:
			setBottomAdapter();
			return true;
		case 4:
			setBottomRightAdapter();
			if (mCurrentListView == mStickyListView) {
				Toast.makeText(this, "Due to StickyListHeaders limitations the SwingRightInAdapter can't work properly", Toast.LENGTH_LONG).show();
			}
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
				tv.setTextColor(Color.WHITE);
			}

			tv.setText(getItem(position));

			return tv;
		}
	}
}
