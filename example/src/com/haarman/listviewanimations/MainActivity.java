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

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.haarman.listviewanimations.appearanceexamples.AppearanceExamplesActivity;
import com.haarman.listviewanimations.itemmanipulationexamples.ItemManipulationsExamplesActivity;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_main_github:
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("http://nhaarman.github.io/ListViewAnimations?ref=app"));
			startActivity(intent);
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	public void onGoogleCardsExampleClicked(View view) {
		Intent intent = new Intent(this, GoogleCardsActivity.class);
		startActivity(intent);
	}

	public void onGridViewExampleClicked(View view) {
		Intent intent = new Intent(this, GridViewActivity.class);
		startActivity(intent);
	}

	public void onAppearanceClicked(View view) {
		Intent intent = new Intent(this, AppearanceExamplesActivity.class);
		startActivity(intent);
	}

	public void onItemManipulationClicked(View view) {
		Intent intent = new Intent(this, ItemManipulationsExamplesActivity.class);
		startActivity(intent);
	}

}
