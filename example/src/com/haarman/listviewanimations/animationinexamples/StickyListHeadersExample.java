package com.haarman.listviewanimations.animationinexamples;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class StickyListHeadersExample extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stickylistheaders);
		getListView().setDivider(null);

		MyStickyListAdapter myStickyListAdapter = new MyStickyListAdapter(this, getItems());
		AlphaInAnimationAdapter animAdapter = new AlphaInAnimationAdapter(myStickyListAdapter);
		animAdapter.setAbsListView(getListView());
		getListView().setAdapter(animAdapter);
	}

	public static ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 1000; i++) {
			items.add(i);
		}
		return items;
	}

	private static class MyStickyListAdapter extends ArrayAdapter<Integer> implements StickyListHeadersAdapter {

		private Context mContext;

		public MyStickyListAdapter(Context context, ArrayList<Integer> items) {
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
			tv.setText("This is row number " + getItem(position));
			return tv;
		}

		@Override
		public long getHeaderId(int position) {
			return getItem(position) / 10;
		}

		@Override
		public View getHeaderView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = (TextView) LayoutInflater.from(mContext).inflate(R.layout.list_row, parent, false);
				tv.setBackgroundColor(Color.CYAN);
			}
			tv.setText(String.valueOf(getItem(position) / 10));
			return tv;
		}
	}

}
