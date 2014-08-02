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

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.MyListAdapter;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.itemmanipulation.animateaddition.AnimateAdditionAdapter;

public class AnimateAdditionActivity extends MyListActivity implements AdapterView.OnItemClickListener {

    private int mAddedItemNumber;
    private AnimateAdditionAdapter<String> mAnimateAdditionAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyListAdapter myAdapter = new MyListAdapter(this);

        mAnimateAdditionAdapter = new AnimateAdditionAdapter<>(myAdapter);
        mAnimateAdditionAdapter.setListView(getListView());

        getListView().setAdapter(mAnimateAdditionAdapter);
        getListView().setOnItemClickListener(this);

        Toast.makeText(this, getString(R.string.tap_to_insert), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        mAnimateAdditionAdapter.insert(position, getString(R.string.newly_added_item, mAddedItemNumber));
        mAddedItemNumber++;
    }

}
