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

package com.nhaarman.listviewanimations.itemmanipulation.swipedismiss;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Debug;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class SwipeTouchListenerTestActivity extends Activity {

    private ListView mListView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        mListView = new ListView(this);
        List<Integer> integers = new ArrayList<Integer>();
        for (int i = 0; i < 20; i++) {
            integers.add(i);
        }

        ListAdapter myListAdapter = new MyListAdapter(this, integers);
        mListView.setAdapter(myListAdapter);

        setContentView(mListView);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int[] location = new int[2];
        mListView.getLocationOnScreen(location);

        ev = MotionEvent.obtain(ev.getDownTime(), ev.getEventTime(), ev.getAction(), ev.getX() - location[0], ev.getY() - location[1], ev.getMetaState());
        boolean handled = mListView.onInterceptTouchEvent(ev);
        if (!handled) {
            handled = mListView.dispatchTouchEvent(ev);
        }
        if (!handled) {
            handled = onTouchEvent(ev);
        }
        return handled;
    }

    public AbsListView getAbsListView() {
        return mListView;
    }

    private static class MyListAdapter extends ArrayAdapter<Integer> {

        private final Context mContext;

        MyListAdapter(final Context context, final List<Integer> items) {
            super(items);
            mContext = context;
        }

        @Override
        public long getItemId(final int location) {
            return getItem(location).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            TextView view = (TextView) convertView;
            if (view == null) {
                view = new TextView(mContext);
                view.setTextSize(30);
            }

            view.setText("This is row number " + getItem(position));
            return view;
        }
    }
}
