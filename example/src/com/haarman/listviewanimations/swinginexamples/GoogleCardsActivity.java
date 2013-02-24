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
package com.haarman.listviewanimations.swinginexamples;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class GoogleCardsActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_googlecards);

		ListView listView = (ListView) findViewById(R.id.activity_googlecards_listview);

		GoogleCardsAdapter googleCardsAdapter = new GoogleCardsAdapter(this);
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(googleCardsAdapter);
		swingBottomInAnimationAdapter.setListView(listView);

		listView.setAdapter(swingBottomInAnimationAdapter);

		googleCardsAdapter.addAll(getItems());
	}

	private ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}
		return items;
	}

	private class GoogleCardsAdapter extends ArrayAdapter<Integer> {

		private Context mContext;

		public GoogleCardsAdapter(Context context) {
			mContext = context;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(mContext).inflate(R.layout.activity_googlecards_card, parent, false);
			}

			TextView textView = (TextView) view.findViewById(R.id.activity_googlecards_card_textview);
			textView.setText("This is card " + (getItem(position) + 1));

			ImageView imageView = (ImageView) view.findViewById(R.id.activity_googlecards_card_imageview);
			int imageResId;
			switch (getItem(position) % 5) {
				case 0:
					imageResId = R.drawable.img_nature1;
					break;
				case 1:
					imageResId = R.drawable.img_nature2;
					break;
				case 2:
					imageResId = R.drawable.img_nature3;
					break;
				case 3:
					imageResId = R.drawable.img_nature4;
					break;
				default:
					imageResId = R.drawable.img_nature5;
			}
			imageView.setImageResource(imageResId);

			return view;
		}
	}
}
