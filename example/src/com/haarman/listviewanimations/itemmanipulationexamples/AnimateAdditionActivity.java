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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter;

import java.util.ArrayList;

public class AnimateAdditionActivity extends MyListActivity implements AdapterView.OnItemClickListener {

    private int mAddedItemNumber;
    private AnimateAdditionAdapter<String> mAnimateAdditionAdapter;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyAdapter myAdapter = new MyAdapter(this, getStringItems());

        mAnimateAdditionAdapter = new AnimateAdditionAdapter<String>(myAdapter);
        mAnimateAdditionAdapter.setListView(getListView());

        getListView().setAdapter(mAnimateAdditionAdapter);
        getListView().setOnItemClickListener(this);

        Toast.makeText(this, "Tap on an item to insert a new item", Toast.LENGTH_LONG).show();
    }

    private static ArrayList<String> getStringItems() {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            items.add("This is row number " + i);
        }
        return items;
    }

    @Override
    public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
        mAnimateAdditionAdapter.insert(position, "This is newly added item " + mAddedItemNumber);
        mAddedItemNumber++;
    }

    private static class MyAdapter extends ArrayAdapter<String> {

        private final Context mContext;

        public MyAdapter(final Context context, final ArrayList<String> items) {
            super(items);
            mContext = context;
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
            }
            tv.setText(getItem(position));
            return tv;
        }
    }
}
