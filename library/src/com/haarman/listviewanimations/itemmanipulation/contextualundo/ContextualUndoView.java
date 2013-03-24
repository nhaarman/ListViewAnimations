/*
 * Copyright 2013 Frankie Sardo
 * Copyright 2013 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.haarman.listviewanimations.itemmanipulation.contextualundo;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

class ContextualUndoView extends FrameLayout {

	private View undoView;
	private View contentView;
	private long itemId;

	ContextualUndoView(Context context, int undoLayoutResourceId) {
		super(context);
		initUndo(undoLayoutResourceId);
	}

	void initUndo(int undoLayoutResourceId) {
		undoView = View.inflate(getContext(), undoLayoutResourceId, null);
		addView(undoView);
	}

	void updateContentView(View contentView) {
		if (this.contentView == null) {
			addView(contentView);
		}
		this.contentView = contentView;
	}

	View getContentView() {
		return contentView;
	}

	void setItemId(long itemId) {
		this.itemId = itemId;
	}

	long getItemId() {
		return itemId;
	}

	boolean isContentDisplayed() {
		return contentView.getVisibility() == View.VISIBLE;
	}

	void displayUndo() {
		contentView.setVisibility(View.GONE);
		undoView.setVisibility(View.VISIBLE);
	}

	void displayContentView() {
		contentView.setVisibility(View.VISIBLE);
		undoView.setVisibility(View.GONE);
	}
}