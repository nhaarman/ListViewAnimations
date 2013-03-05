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

import java.util.Collection;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * A decorator class that enables decoration of an instance of the
 * com.haarman.listviewanimations.ArrayAdapter class.
 * 
 * Classes extending this class can override methods and provide extra
 * functionality before or after calling the super method.
 */
public abstract class ArrayAdapterDecorator<T> extends ArrayAdapter<T> {

	protected final ArrayAdapter<T> mDecoratedArrayAdapter;

	private ListView mListView;

	public ArrayAdapterDecorator(ArrayAdapter<T> arrayAdapter) {
		mDecoratedArrayAdapter = arrayAdapter;
	}

	public void setListView(ListView listView) {
		mListView = listView;

		if (mDecoratedArrayAdapter instanceof ArrayAdapterDecorator) {
			((ArrayAdapterDecorator<T>) mDecoratedArrayAdapter).setListView(listView);
		}
	}

	public ListView getListView() {
		return mListView;
	}

	@Override
	public int getCount() {
		return mDecoratedArrayAdapter.getCount();
	}

	@Override
	public T getItem(int position) {
		return mDecoratedArrayAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return mDecoratedArrayAdapter.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return mDecoratedArrayAdapter.getView(position, convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return mDecoratedArrayAdapter.areAllItemsEnabled();
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return mDecoratedArrayAdapter.getDropDownView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position) {
		return mDecoratedArrayAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return mDecoratedArrayAdapter.getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return mDecoratedArrayAdapter.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return mDecoratedArrayAdapter.isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		return mDecoratedArrayAdapter.isEnabled(position);
	}

	@Override
	public void notifyDataSetChanged() {
		mDecoratedArrayAdapter.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		mDecoratedArrayAdapter.notifyDataSetInvalidated();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		mDecoratedArrayAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		mDecoratedArrayAdapter.unregisterDataSetObserver(observer);
	}

	@Override
	public void add(T item) {
		mDecoratedArrayAdapter.add(item);
	}

	@Override
	public void add(int index, T item) {
		mDecoratedArrayAdapter.add(index, item);
	}

	@Override
	public void addAll(Collection<? extends T> items) {
		mDecoratedArrayAdapter.addAll(items);
	}

	@Override
	public void addAll(T... items) {
		mDecoratedArrayAdapter.addAll(items);
	}

	@Override
	public void addAll(int index, Collection<? extends T> items) {
		mDecoratedArrayAdapter.addAll(index, items);
	}

	@Override
	public void addAll(int index, T... items) {
		mDecoratedArrayAdapter.addAll(index, items);
	}

	@Override
	public void clear() {
		mDecoratedArrayAdapter.clear();
	}

	@Override
	public void set(int index, T item) {
		mDecoratedArrayAdapter.set(index, item);
	}

	@Override
	public void remove(T item) {
		mDecoratedArrayAdapter.remove(item);
	}

	@Override
	public void remove(int index) {
		mDecoratedArrayAdapter.remove(index);
	}

	@Override
	public void removePositions(Collection<Integer> positions) {
		mDecoratedArrayAdapter.removePositions(positions);
	}

	@Override
	public void removeAll(Collection<T> items) {
		mDecoratedArrayAdapter.removeAll(items);
	}

	@Override
	public void retainAll(Collection<T> items) {
		mDecoratedArrayAdapter.retainAll(items);
	}

	@Override
	public int indexOf(T item) {
		return mDecoratedArrayAdapter.indexOf(item);
	}
}