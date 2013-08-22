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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.haarman.listviewanimations.ExpandableListItemActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulationexamples.contextualundo.ContextualUndoActivity;
import com.haarman.listviewanimations.itemmanipulationexamples.contextualundo.ContextualUndoWithTimedDeleteActivity;
import com.haarman.listviewanimations.itemmanipulationexamples.contextualundo.ContextualUndoWithTimedDeleteAndCountDownActivity;

public class ItemManipulationsExamplesActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_examples_itemmanipulations);
	}

	public void onSwipeDismissClicked(View view) {
		Intent intent = new Intent(this, SwipeDismissActivity.class);
		startActivity(intent);
	}

	public void onAnimateRemovalClicked(View view) {
		Intent intent = new Intent(this, AnimateDismissActivity.class);
		startActivity(intent);
	}

	public void onContextualUndoClicked(View view) {
		Intent intent = new Intent(this, ContextualUndoActivity.class);
		startActivity(intent);
	}
	
	public void onContextualUndoTimedCountDownClicked(View view) {
		Intent intent = new Intent(this, ContextualUndoWithTimedDeleteAndCountDownActivity.class);
		startActivity(intent);
	}
	
	public void onContextualUndoTimedClicked(View view) {
		Intent intent = new Intent(this, ContextualUndoWithTimedDeleteActivity.class);
		startActivity(intent);
	}

    public void onExpandListItemAdapterClicked(View view){
        Intent intent = new Intent(this, ExpandableListItemActivity.class);
        startActivity(intent);
    }
}
