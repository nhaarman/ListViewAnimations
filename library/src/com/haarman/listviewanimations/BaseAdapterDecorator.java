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
import android.widget.BaseAdapter;

public abstract class BaseAdapterDecorator extends BaseAdapter {

	protected final BaseAdapter decoratedBaseAdapter;

	public BaseAdapterDecorator(BaseAdapter baseAdapter) {
		decoratedBaseAdapter = baseAdapter;
	}

	@Override
	public int getCount() {
		return decoratedBaseAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		return decoratedBaseAdapter.getItem(position);
	}

	@Override
	public long getItemId(int position) {
		return decoratedBaseAdapter.getItemId(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return decoratedBaseAdapter.getView(position, convertView, parent);
	}

	@Override
	public boolean areAllItemsEnabled() {
		return decoratedBaseAdapter.areAllItemsEnabled();
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return decoratedBaseAdapter.getDropDownView(position, convertView, parent);
	}

	@Override
	public int getItemViewType(int position) {
		return decoratedBaseAdapter.getItemViewType(position);
	}

	@Override
	public int getViewTypeCount() {
		return decoratedBaseAdapter.getViewTypeCount();
	}

	@Override
	public boolean hasStableIds() {
		return decoratedBaseAdapter.hasStableIds();
	}

	@Override
	public boolean isEmpty() {
		return decoratedBaseAdapter.isEmpty();
	}

	@Override
	public boolean isEnabled(int position) {
		return decoratedBaseAdapter.isEnabled(position);
	}

	@Override
	public void notifyDataSetChanged() {
		decoratedBaseAdapter.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		decoratedBaseAdapter.notifyDataSetInvalidated();
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		decoratedBaseAdapter.registerDataSetObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		decoratedBaseAdapter.unregisterDataSetObserver(observer);
	}

}