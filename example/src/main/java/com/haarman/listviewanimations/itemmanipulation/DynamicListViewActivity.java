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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.DynamicListViewWrapper;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.OnItemMovedListener;
import com.nhaarman.listviewanimations.itemmanipulation.dragdrop.TouchViewDraggableManager;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.OnDismissCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipemenu.MenuAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipemenu.SwipeMenuAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipemenu.SwipeMenuCallback;
import com.nhaarman.listviewanimations.itemmanipulation.swipemenu.SwipeMenuTouchListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DynamicListViewActivity extends MyListActivity {

    private static final int INITIAL_DELAY_MILLIS = 300;
    private static final String TRANSLATION_X = "translationX";

    private int mNewItemCount;
    private DynamicListView listView;
    private SimpleSwipeUndoAdapter simpleSwipeUndoAdapter;
    private SwipeMenuAdapter swipeMenuAdapter;
    private Button leftButton;
    private Button rightButton;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dynamiclistview);
        listView = (DynamicListView) findViewById(R.id.activity_dynamiclistview_listview);
        listView.addHeaderView(LayoutInflater.from(this).inflate(R.layout.activity_dynamiclistview_header, listView, false));

        /* Setup the adapter */
        ArrayAdapter<String> adapter = new MyListAdapter(this);
        simpleSwipeUndoAdapter = new SimpleSwipeUndoAdapter(adapter, this, new MyOnDismissCallback(adapter));
        swipeMenuAdapter = new SwipeMenuAdapter(simpleSwipeUndoAdapter, this, new SwipeMenuCallback() {
            @Override
            public void onStartSwipe(@NonNull View view, int position, int direction) {

            }

            @Override
            public void onMenuShown(@NonNull View view, int position, int direction) {

            }

            @Override
            public void onMenuClosed(@NonNull View view, int position, int direction) {

            }

            @Override
            public void beforeMenuShow(@NonNull View view, int position, int direction) {

            }

            @Override
            public void beforeMenuClose(@NonNull View view, int position, int direction) {

            }
        });

        leftButton = new Button(this);
//        leftButton.setBackgroundColor(Color.BLUE);
        leftButton.setTextColor(Color.BLUE);
        leftButton.setText("mark read");

        rightButton = new Button(this);
//        rightButton.setBackgroundColor(Color.RED);
        rightButton.setTextColor(Color.RED);
        rightButton.setText("delete");
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (listView != null) {
                    int position = swipeMenuAdapter.getCurrentMenuPosition();
                    if (position != AdapterView.INVALID_POSITION) {
                        listView.remove(position);
                    }
//                mListView.startDragging(position - mListView.getHeaderViewsCount());
                }
            }
        });




//        AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(simpleSwipeUndoAdapter);
//        animAdapter.setAbsListView(listView);
//        assert animAdapter.getViewAnimator() != null;
//        animAdapter.getViewAnimator().setInitialDelayMillis(INITIAL_DELAY_MILLIS);
        listView.setAdapter(swipeMenuAdapter);

        /* Enable drag and drop functionality */
//        listView.enableDragAndDrop();
        //has to to done first :s
        listView.setDraggableManager(new TouchViewDraggableManager(R.id.list_row_draganddrop_touchview));
        listView.setOnItemMovedListener(new MyOnItemMovedListener(adapter));
        listView.setOnItemLongClickListener(new MyOnItemLongClickListener(listView));

        /* Enable swipe to dismiss */
//        listView.enableSimpleSwipeUndo();

        /* Add new items on item click */
        listView.setOnItemClickListener(new MyOnItemClickListener(listView));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Start Edit");
        menu.add("Enable swipe to remove");

        return super.onCreateOptionsMenu(menu);
    }

    private void setDragAndDropEnabled(final boolean enabled, final boolean animated) {
        if (enabled) {
            listView.enableDragAndDrop();
        }
        else {
            listView.disableDragAndDrop();
        }
        int headerViewsCount = listView.getHeaderViewsCount();
        int firstVisiblePosition = listView.getFirstVisiblePosition();
        int startPosition = firstVisiblePosition;
        if (startPosition < headerViewsCount) {
            startPosition += headerViewsCount;
        }
        int lastVisiblePosition = listView.getLastVisiblePosition();
        List<Animator> allAnimators = new ArrayList<Animator>();
        for (int i = startPosition; i <= lastVisiblePosition; i++) {
            View view = listView.getChildAt(i - firstVisiblePosition);
            if (view == null) {
                continue;
            }
            final View touchView = view.findViewById(R.id.list_row_draganddrop_touchview);
            final int touchViewWidth = touchView.getMeasuredWidth();
            final View targetView = (View) touchView.getParent();
            final float currentTx = ViewHelper.getTranslationX(targetView);
            if (animated) {
                allAnimators.add(ObjectAnimator.ofFloat(targetView, TRANSLATION_X, enabled ? (-touchViewWidth): 0, enabled ? 0: (- touchViewWidth) ));
            } else {
                ViewHelper.setTranslationX(targetView, enabled?0:(-touchViewWidth));
            }

        }
        if (allAnimators.size() > 0) {
            AnimatorSet allAnimatorSet = new AnimatorSet();
            allAnimatorSet.playTogether(allAnimators);
            allAnimatorSet.setDuration(300);
            allAnimatorSet.start();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle() == "Start Edit" || item.getTitle() == "End Edit") {
            setDragAndDropEnabled(!listView.isDragAndDropEnabled(), true);
            item.setTitle(listView.isDragAndDropEnabled()?"End Edit":"Start Edit");
            return true;
        } else if (item.getTitle() == "Enable swipe to remove" || item.getTitle() == "Disable swipe to remove") {
            if (listView.isSwipeToDismissEnabled()) {
                item.setTitle("Enable swipe to remove");
                listView.disableSwipeToDismiss();
            }
            else {
                item.setTitle("Disable swipe to remove");
                listView.enableSimpleSwipeUndo();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class MyListAdapter extends ArrayAdapter<String> implements UndoAdapter, MenuAdapter {

        private final Context mContext;

        MyListAdapter(final Context context) {
            mContext = context;
            for (int i = 0; i < 20; i++) {
                add(mContext.getString(R.string.row_number, i));
            }
        }

        @Override
        public long getItemId(final int position) {
            return getItem(position).hashCode();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.list_row_dynamiclistview, parent, false);
                int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.MATCH_PARENT, View.MeasureSpec.AT_MOST);
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(ViewGroup.LayoutParams.WRAP_CONTENT, View.MeasureSpec.UNSPECIFIED);
                view.measure(widthMeasureSpec, heightMeasureSpec);
            }

            View touchView = view.findViewById(R.id.list_row_draganddrop_touchview);
            float translationX = listView.isDragAndDropEnabled()?0:(0-touchView.getMeasuredWidth());
            ViewHelper.setTranslationX((View) touchView.getParent(), translationX);
            ((TextView) view.findViewById(R.id.list_row_draganddrop_textview)).setText(getItem(position));

            return view;
        }

        @NonNull
        @Override
        public View getUndoView(final int position, final View convertView, @NonNull final ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.undo_row, parent, false);
            }
            return view;
        }

        @NonNull
        @Override
        public View getUndoClickView(@NonNull final View view) {
            return view.findViewById(R.id.undo_row_undobutton);
        }

        @NonNull
        @Override
        public View[] getLeftButtons(int position) {
            return  new View[]{leftButton};
        }

        @NonNull
        @Override
        public View[] getRightButtons(int position) {
            return new View[]{rightButton};
        }

        @Override
        public boolean canShowLeftMenu(int position) {
            return (position % 3) == 0;
        }

        @Override
        public boolean canShowRightMenu(int position) {
            return (position % 2) == 1;
        }
    }

    private static class MyOnItemLongClickListener implements AdapterView.OnItemLongClickListener {

        private final DynamicListView mListView;

        MyOnItemLongClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public boolean onItemLongClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            if (mListView != null) {
                mListView.remove(position - mListView.getHeaderViewsCount(), 3);
//                mListView.startDragging(position - mListView.getHeaderViewsCount());
            }
            return true;
        }
    }

    private class MyOnDismissCallback implements OnDismissCallback {

        private final ArrayAdapter<String> mAdapter;

        @Nullable
        private Toast mToast;

        MyOnDismissCallback(final ArrayAdapter<String> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onDismiss(@NonNull final ViewGroup listView, @NonNull final int[] reverseSortedPositions) {
            for (int position : reverseSortedPositions) {
                mAdapter.remove(position);
            }

            if (mToast != null) {
                mToast.cancel();
            }
            mToast = Toast.makeText(
                    DynamicListViewActivity.this,
                    getString(R.string.removed_positions, Arrays.toString(reverseSortedPositions)),
                    Toast.LENGTH_LONG
            );
            mToast.show();
        }
    }

    private class MyOnItemMovedListener implements OnItemMovedListener {

        private final ArrayAdapter<String> mAdapter;

        private Toast mToast;

        MyOnItemMovedListener(final ArrayAdapter<String> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onItemMoved(final int originalPosition, final int newPosition) {
            if (mToast != null) {
                mToast.cancel();
            }

            mToast = Toast.makeText(getApplicationContext(), getString(R.string.moved, mAdapter.getItem(newPosition), newPosition), Toast.LENGTH_SHORT);
            mToast.show();
        }
    }

    private class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        private final DynamicListView mListView;

        MyOnItemClickListener(final DynamicListView listView) {
            mListView = listView;
        }

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            mListView.insert(position, getString(R.string.newly_added_item, mNewItemCount));
            mNewItemCount++;
        }
    }
}
