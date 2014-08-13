/*
 * Copyright 2014 Niek Haarman
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
package com.haarman.listviewanimations.itemmanipulation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.haarman.listviewanimations.BaseActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.expandablelistitems.ExpandableListItemActivity;

public class ItemManipulationsExamplesActivity extends BaseActivity {

    @SuppressLint("InlinedApi")
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_examples_itemmanipulations);
    }

    public void onDynamicListViewClicked(final View view) {
        Intent intent = new Intent(this, DynamicListViewActivity.class);
        startActivity(intent);
    }

    public void onExpandListItemAdapterClicked(final View view) {
        Intent intent = new Intent(this, ExpandableListItemActivity.class);
        startActivity(intent);
    }
}
