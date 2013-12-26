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
import android.widget.Toast;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;
import com.nhaarman.listviewanimations.widget.DynamicListView;

public class DragAndDropActivity extends MyListActivity {

	private DynamicListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_draganddrop);

		mListView = (DynamicListView) findViewById(R.id.activity_draganddrop_listview);
		mListView.setDivider(null);

		final ArrayAdapter<Integer> adapter = createListAdapter();
		AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(adapter);
		animAdapter.setInitialDelayMillis(300);
		animAdapter.setAbsListView(mListView);
		mListView.setAdapter(animAdapter);

		Toast.makeText(this, "Long press an item to start dragging", Toast.LENGTH_LONG).show();
        mListView.setOnItemMovedListener(new DynamicListView.OnItemMovedListener() {
            @Override
            public void onItemMoved(int newPosition) {
                Toast.makeText(getApplicationContext(), adapter.getItem(newPosition) + " moved to position " + newPosition, Toast.LENGTH_SHORT).show();
            }
        });
	}
}
