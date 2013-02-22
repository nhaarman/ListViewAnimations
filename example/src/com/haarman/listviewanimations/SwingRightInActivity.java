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
package com.haarman.listviewanimations;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingRightInAnimationAdapter;

public class SwingRightInActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		MySwingBottomInAdapter mAdapter = new MySwingBottomInAdapter(this, getItems());
		mAdapter.setListView(getListView());
		getListView().setAdapter(mAdapter);
	}

	private ArrayList<String> getItems() {
		ArrayList<String> items = new ArrayList<String>();
		for (int i = 0; i < 1000; i++) {
			items.add(String.valueOf(i));
		}
		return items;
	}

	private class MySwingBottomInAdapter extends SwingRightInAnimationAdapter<String> {

		public MySwingBottomInAdapter(Context context, ArrayList<String> items) {
			super(context, items);
		}

		@Override
		protected View getItemView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.list_row, parent, false);
			}
			tv.setText(getItem(position));
			return tv;
		}

	}
}
