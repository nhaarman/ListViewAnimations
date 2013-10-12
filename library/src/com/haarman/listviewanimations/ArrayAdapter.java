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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import android.widget.BaseAdapter;

import com.haarman.listviewanimations.view.DynamicListView;
import com.haarman.listviewanimations.view.DynamicListView.Swappable;

/**
 * A true {@link ArrayList} adapter providing access to all ArrayList methods.
 * Also implements {@link Swappable} for easy item swapping.
 */
public abstract class ArrayAdapter<T> extends BaseAdapter implements DynamicListView.Swappable {

	protected List<T> mItems;

	/**
	 * Creates a new ArrayAdapter with an empty list.
	 */
	public ArrayAdapter() {
		this(null);
	}

	/**
	 * Creates a new {@link ArrayAdapter} with a <b>copy</b> of the specified
	 * list, or an empty list if items == null.
	 */
	
	public ArrayAdapter(List<T> items) {
		mItems = new ArrayList<T>();
		if (items != null) {
			mItems.addAll(items);
		}
	}
	

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public T getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * Appends the specified element to the end of the list.
	 */
	// @ requires item != null;
	public void add(T item) {
		mItems.add(item);
		notifyDataSetChanged();
	}

	/**
	 * Inserts the specified element at the specified position in the list.
	 */
	public void add(int position, T item) {
		mItems.add(position, item);
		notifyDataSetChanged();
	}

	/**
	 * Appends all of the elements in the specified collection to the end of the
	 * list, in the order that they are returned by the specified collection's
	 * Iterator.
	 */
	public void addAll(Collection<? extends T> items) {
		mItems.addAll(items);
		notifyDataSetChanged();
	}

	/**
	 * Appends all of the elements to the end of the list, in the order that
	 * they are specified.
	 */
	public void addAll(T... items) {
		Collections.addAll(mItems, items);
		notifyDataSetChanged();
	}

	/**
	 * Inserts all of the elements in the specified collection into the list,
	 * starting at the specified position.
	 */
	public void addAll(int position, Collection<? extends T> items) {
		mItems.addAll(position, items);
		notifyDataSetChanged();
	}

	/**
	 * Inserts all of the elements into the list, starting at the specified
	 * position.
	 */
	public void addAll(int position, T... items) {
		for (int i = position; i < (items.length + position); i++) {
			mItems.add(i, items[i]);
		}
		notifyDataSetChanged();
	}

	/**
	 * Removes all of the elements from the list.
	 */
	public void clear() {
		mItems.clear();
		notifyDataSetChanged();
	}

	/**
	 * Replaces the element at the specified position in this list with the
	 * specified element.
	 */
	public void set(int position, T item) {
		mItems.set(position, item);
		notifyDataSetChanged();
	}

	/**
	 * Removes the specified element from the list
	 */
	public void remove(T item) {
		mItems.remove(item);
		notifyDataSetChanged();
	}

	/**
	 * Removes the element at the specified position in the list
	 */
	public void remove(int position) {
		mItems.remove(position);
		notifyDataSetChanged();
	}

	/**
	 * Removes all elements at the specified positions in the list
	 */
	public void removePositions(Collection<Integer> positions) {
		ArrayList<Integer> positionsList = new ArrayList<Integer>(positions);
		Collections.sort(positionsList);
		Collections.reverse(positionsList);
		for (int position : positionsList) {
			mItems.remove(position);
		}
		notifyDataSetChanged();
	}

	/**
	 * Removes all of the list's elements that are also contained in the
	 * specified collection
	 */
	public void removeAll(Collection<T> items) {
		mItems.removeAll(items);
		notifyDataSetChanged();
	}

	/**
	 * Retains only the elements in the list that are contained in the specified
	 * collection
	 */
	public void retainAll(Collection<T> items) {
		mItems.retainAll(items);
		notifyDataSetChanged();
	}

	/**
	 * Returns the position of the first occurrence of the specified element in
	 * this list, or -1 if this list does not contain the element. More
	 * formally, returns the lowest position <tt>i</tt> such that
	 * <tt>(o==null&nbsp;?&nbsp;get(i)==null&nbsp;:&nbsp;o.equals(get(i)))</tt>,
	 * or -1 if there is no such position.
	 */
	public int indexOf(T item) {
		return mItems.indexOf(item);
	}

	@Override
	public void swapItems(int positionOne, int positionTwo) {
		T temp = getItem(positionOne);
		set(positionOne, getItem(positionTwo));
		set(positionTwo, temp);
	}
	
	private BaseAdapter mDataSetChangedSlavedAdapter;
	
	public void propagateNotifyDataSetChanged(BaseAdapter slavedAdapter) {
		mDataSetChangedSlavedAdapter = slavedAdapter;
	}

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		if (mDataSetChangedSlavedAdapter != null) {
			mDataSetChangedSlavedAdapter.notifyDataSetChanged();
		}
	}
}
