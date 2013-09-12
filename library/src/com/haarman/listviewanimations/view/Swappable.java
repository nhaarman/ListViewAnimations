/*
 * Copyright (c) 2013 K–NFB Reading Technology, Inc.
 */

package com.haarman.listviewanimations.view;

/**
 * Interface, usually implemented by a {@link com.haarman.listviewanimations.BaseAdapterDecorator},
 * that indicates that it can swap the visual position of two list-items.
 *
 * @author Anton Spaans on 9/11/13.
 */
public interface Swappable {
	
	/**
	 * Swaps the item on the first adapter position with the item on the second adapter position.
	 * Be sure to call {@link android.widget.BaseAdapter#notifyDataSetChanged()} if appropriate.
	 *
	 * @param positionOne First adapter position.
	 * @param positionTwo Second adapter position.
	 */
	public void swapItems(int positionOne, int positionTwo);
}