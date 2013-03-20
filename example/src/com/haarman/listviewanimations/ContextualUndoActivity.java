package com.haarman.listviewanimations;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;

import java.util.ArrayList;
import java.util.Arrays;

public class ContextualUndoActivity extends MyListActivity {

    private ContextualUndoAdapter contextualUndoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final ArrayAdapter<String> mAdapter = createListAdapter();
        contextualUndoAdapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row, R.id.undo);

        if (savedInstanceState != null) {
            contextualUndoAdapter.onRestoreInstanceState(savedInstanceState.getParcelable("contextualUndoAdapterState"));
        }
        contextualUndoAdapter.setListView(getListView());
        getListView().setAdapter(contextualUndoAdapter);
        contextualUndoAdapter.setDeleteItemCallback(new ContextualUndoAdapter.DeleteItemCallback() {
            @Override
            public void deleteItem(Object item) {
                performActualDelete(item);
                mAdapter.remove((String) item);
                mAdapter.notifyDataSetChanged();
            }
        });

        getListView().setOnItemClickListener(null);
    }

    private void performActualDelete(Object item) {
        ITEMS.remove(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("contextualUndoAdapterState", contextualUndoAdapter.onSaveInstanceState());
    }

    @Override
    protected ArrayAdapter<String> createListAdapter() {
        return new MyListAdapter(this, ITEMS);
    }

    private static final ArrayList<String> ITEMS = new ArrayList<String>();

    static {
        String[] words = "Remember: in order to use the Contextual Undo, your decorated adapter must have sable ids.".split(" ");
        ITEMS.addAll(Arrays.asList(words));
    }

    private static class MyListAdapter extends ArrayAdapter<String> {

        private Context mContext;

        public MyListAdapter(Context context, ArrayList<String> items) {
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
