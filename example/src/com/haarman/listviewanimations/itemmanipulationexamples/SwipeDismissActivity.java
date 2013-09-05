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

import java.util.ArrayList;
import java.util.Arrays;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.CountDownFormatter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;

public class SwipeDismissActivity extends ActionBarActivity implements OnNavigationListener, OnDismissCallback, DeleteItemCallback {

	private MyListAdapter mAdapter;

	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mListView = new ListView(this);
		setContentView(mListView);

		mAdapter = new MyListAdapter(this, getItems());

		setSwipeDismissAdapter();

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setListNavigationCallbacks(new AnimSelectionAdapter(), this);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private void setSwipeDismissAdapter() {
		SwipeDismissAdapter adapter = new SwipeDismissAdapter(mAdapter, this);
		adapter.setAbsListView(mListView);
		mListView.setAdapter(adapter);
	}

	@Override
	public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
		for (int position : reverseSortedPositions) {
			mAdapter.remove(position);
		}
		Toast.makeText(this, "Removed positions: " + Arrays.toString(reverseSortedPositions), Toast.LENGTH_SHORT).show();
	}

	private void setContextualUndoAdapter() {
		ContextualUndoAdapter adapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row, R.id.undo_row_undobutton);
		adapter.setAbsListView(mListView);
		mListView.setAdapter(adapter);
		adapter.setDeleteItemCallback(this);
	}

	@Override
	public void deleteItem(int position) {
		mAdapter.remove(position);
		mAdapter.notifyDataSetChanged();
	}

	private void setContextualUndoWithTimedDeleteAdapter() {
		ContextualUndoAdapter adapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row, R.id.undo_row_undobutton, 3000);
		adapter.setAbsListView(mListView);
		mListView.setAdapter(adapter);
		adapter.setDeleteItemCallback(this);
	}

	private void setContextualUndoWithTimedDeleteAndCountDownAdapter() {
		ContextualUndoAdapter adapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row, R.id.undo_row_undobutton, 3000, R.id.undo_row_texttv, new MyFormatCountDownCallback());
		adapter.setAbsListView(mListView);
		mListView.setAdapter(adapter);
		adapter.setDeleteItemCallback(this);
	}

	private class MyFormatCountDownCallback implements CountDownFormatter {

		@Override
		public String getCountDownString(long millisUntilFinished) {
			int seconds = (int) Math.ceil((millisUntilFinished / 1000.0));

			if (seconds > 0) {
				return getResources().getQuantityString(R.plurals.countdown_seconds, seconds, seconds);
			}
			return getString(R.string.countdown_dismissing);
		}
	}

	private static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}
		return items;
	}

	private static class MyListAdapter extends ArrayAdapter<Integer> {

		private Context mContext;

		public MyListAdapter(Context context, ArrayList<Integer> items) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
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
		case 3:
			setContextualUndoWithTimedDeleteAndCountDownAdapter();
			return true;
		default:
			return false;
		}
	}

	private class AnimSelectionAdapter extends ArrayAdapter<String> {

		public AnimSelectionAdapter() {
			addAll("Swipe-To-Dismiss", "Contextual Undo", "CU - Timed Delete", "CU - Count Down");
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(SwipeDismissActivity.this).inflate(android.R.layout.simple_list_item_1, parent, false);
				tv.setTextColor(Color.WHITE);
			}

			tv.setText(getItem(position));

			return tv;
		}
	}
}
