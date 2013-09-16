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
 * 
 * External default OnScrollListener allowing to custom onScrollListening events like loading more items.
 * Author: David Berlioz
 */

package com.haarman.listviewanimations.itemmanipulation;

import android.widget.AbsListView;

public class SwipeOnScrollListener implements AbsListView.OnScrollListener {
	
	SwipeDismissListViewTouchListener mTouchListener;
	
	public void setTouchListener(SwipeDismissListViewTouchListener touchListener) {
		mTouchListener = touchListener;
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			mTouchListener.disallowSwipe();
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}
}
