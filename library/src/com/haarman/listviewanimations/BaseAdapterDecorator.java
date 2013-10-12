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

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.SectionIndexer;

import com.emilsjolander.components.stickylistheaders.StickyListHeadersAdapter;
import com.haarman.listviewanimations.view.DynamicListView;
import com.haarman.listviewanimations.view.DynamicListView.Swappable;

/**
 * A decorator class that enables decoration of an instance of the BaseAdapter
 * class.
 * 
 * Classes extending this class can override methods and provide extra
 * functionality before or after calling the super method.
 */
public abstract class BaseAdapterDecorator extends BaseAdapter implements SectionIndexer, StickyListHeadersAdapter, DynamicListView.Swappable {

	protected final BaseAdapter mDecoratedBaseAdapter;

	private AbsListView mListView;

	private boolean mIsParentHorizontalScrollContainer;

	public BaseAdapterDecorator(BaseAdapter baseAdapter) {
		mDecoratedBaseAdapter = baseAdapter;
	}

	public void setAbsListView(AbsListView listView) {
		mListView = listView;

		if (mDecoratedBaseAdapter instanceof BaseAdapterDecorator) {
			((BaseAdapterDecorator) mDecoratedBaseAdapter).setAbsListView(listView);
		}
	}

	public AbsListView getAbsListView() {
		return mListView;
	}

	@Override
	public int getCount() {
		return mDecoratedBaseAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return mDecoratedBaseAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mDecoratedBaseAdapter.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mDecoratedBaseAdapter.getView(position, convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mDecoratedBaseAdapter.areAllItemsEnabled();
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return mDecoratedBaseAdapter.getDropDownView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position) {
		return mDecoratedBaseAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mDecoratedBaseAdapter.getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return mDecoratedBaseAdapter.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return mDecoratedBaseAdapter.isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		return mDecoratedBaseAdapter.isEnabled(position);
	}

	@Override
	public void notifyDataSetChanged() {
		if (!(mDecoratedBaseAdapter instanceof ArrayAdapter<?>)) {
			// fix #35 dirty trick !
			// leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
			mDecoratedBaseAdapter.notifyDataSetChanged();
		}
	}
	
	/**
	 * Helper function if you want to force notifyDataSetChanged()
	 * @param force
	 */
	public void notifyDataSetChanged(Boolean force) {
		if ((force) || (!(mDecoratedBaseAdapter instanceof ArrayAdapter<?>))) {
			// leads to an infinite loop when trying because ArrayAdapter triggers notifyDataSetChanged itself
			mDecoratedBaseAdapter.notifyDataSetChanged();
		}
	}

	@Override
	public void notifyDataSetInvalidated() {
		mDecoratedBaseAdapter.notifyDataSetInvalidated();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mDecoratedBaseAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDecoratedBaseAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public int getPositionForSection(int section) {
		if (mDecoratedBaseAdapter instanceof SectionIndexer) {
			return ((SectionIndexer) mDecoratedBaseAdapter).getPositionForSection(section);
		}
		return 0;
	}

	@Override
	public int getSectionForPosition(int position) {
		if (mDecoratedBaseAdapter instanceof SectionIndexer) {
			return ((SectionIndexer) mDecoratedBaseAdapter).getSectionForPosition(position);
		}
		return 0;
	}

	@Override
	public Object[] getSections() {
		if (mDecoratedBaseAdapter instanceof SectionIndexer) {
			return ((SectionIndexer) mDecoratedBaseAdapter).getSections();
		}
		return null;
	}

	@Override
	public long getHeaderId(int position) {
		if (mDecoratedBaseAdapter instanceof StickyListHeadersAdapter) {
			return ((StickyListHeadersAdapter) mDecoratedBaseAdapter).getHeaderId(position);
		}
		return 0;
	}

	@Override
	public View getHeaderView(int position, View convertView, ViewGroup parent) {
		if (mDecoratedBaseAdapter instanceof StickyListHeadersAdapter) {
			return ((StickyListHeadersAdapter) mDecoratedBaseAdapter).getHeaderView(position, convertView, parent);
		}
		return null;
	}

	public BaseAdapter getDecoratedBaseAdapter() {
		return mDecoratedBaseAdapter;
	}

	@Override
	public void swapItems(int positionOne, int positionTwo) {
		if (mDecoratedBaseAdapter instanceof Swappable) {
			((Swappable) mDecoratedBaseAdapter).swapItems(positionOne, positionTwo);
		}
	}

	public void setIsParentHorizontalScrollContainer(boolean isParentHorizontalScrollContainer) {
		mIsParentHorizontalScrollContainer = isParentHorizontalScrollContainer;
	}

	public boolean isParentHorizontalScrollContainer() {
		return mIsParentHorizontalScrollContainer;
	}
}