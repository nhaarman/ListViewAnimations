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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.AnimateAdditionAdapter;

import java.util.ArrayList;

public class AnimateAdditionActivity extends MyListActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MyAdapter myAdapter = new MyAdapter(this, getStringItems());

        final AnimateAdditionAdapter<String> animateAdditionAdapter = new AnimateAdditionAdapter<String>(myAdapter);
        animateAdditionAdapter.setAbsListView(getListView());

        getListView().setAdapter(animateAdditionAdapter);

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                animateAdditionAdapter.insert(2, "This is a new row!");
                handler.postDelayed(this, 500);
            }
        }, 1000);
    }

    public static ArrayList<String> getStringItems() {
        ArrayList<String> items = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            items.add("This is row number " + String.valueOf(i));
        }
        return items;
    }

    private static class MyAdapter extends ArrayAdapter<String> {

        private Context mContext;

        public MyAdapter(Context context, ArrayList<String> items) {
            super(items);
            mContext = context;
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView tv = (TextView) convertView;
            if (tv == null) {
                tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
            }
            tv.setText(getItem(position));
            return tv;
        }
    }
}
