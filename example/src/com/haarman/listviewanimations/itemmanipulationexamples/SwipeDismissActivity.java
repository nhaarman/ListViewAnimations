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

import java.util.Arrays;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.itemmanipulation.SwipeDismissAdapter;
import com.haarman.listviewanimations.itemmanipulation.OnDismissCallback;

public class SwipeDismissActivity extends MyListActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ArrayAdapter<String> mAdapter = createListAdapter();

		SwipeDismissAdapter swipeDismissAdapter = new SwipeDismissAdapter(mAdapter, new MyOnDismissCallback(mAdapter));
		swipeDismissAdapter.setListView(getListView());

		getListView().setAdapter(swipeDismissAdapter);
	}

	private class MyOnDismissCallback implements OnDismissCallback {

		private ArrayAdapter<String> mAdapter;

		public MyOnDismissCallback(ArrayAdapter<String> adapter) {
			mAdapter = adapter;
		}

		@Override
		public void onDismiss(ListView listView, int[] reverseSortedPositions) {
			for (int position : reverseSortedPositions) {
				mAdapter.remove(position);
			}
			Toast.makeText(SwipeDismissActivity.this, "Removed positions: " + Arrays.toString(reverseSortedPositions), Toast.LENGTH_SHORT).show();
		}
	}
}
