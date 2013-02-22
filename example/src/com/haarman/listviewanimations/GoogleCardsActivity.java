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

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;


public class GoogleCardsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_googlecards);

		ListView listView = (ListView) findViewById(R.id.activity_googlecards_listview);
		GoogleCardsAdapter mAdapter = new GoogleCardsAdapter(this, getItems());
		mAdapter.setListView(listView);
		listView.setAdapter(mAdapter);
	}

	private ArrayList<String> getItems() {
		ArrayList<String> items = new ArrayList<String>();
		for (int i = 0; i < 100; i++) {
			items.add("This is card number " + (i + 1));
		}
		return items;
	}

	private class GoogleCardsAdapter extends SwingBottomInAnimationAdapter<String> {

		public GoogleCardsAdapter(Context context) {
			super(context);
		}

		public GoogleCardsAdapter(Context context, ArrayList<String> items) {
			super(context, items);
		}

		@Override
		protected View getItemView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			if (view == null) {
				view = LayoutInflater.from(getContext()).inflate(R.layout.activity_googlecards_card, parent, false);
			}

			TextView textView = (TextView) view.findViewById(R.id.activity_googlecards_card_textview);
			textView.setText(getItem(position));

			ImageView imageView = (ImageView) view.findViewById(R.id.activity_googlecards_card_imageview);
			int imageResId;
			switch (position % 5) {
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

	public static int dpToPx(Context context, int dp) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
		return (int) px;
	}
}
