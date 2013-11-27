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

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

@SuppressLint("ViewConstructor")
public class ContextualUndoView extends FrameLayout {

	private View mUndoView;
	private View mContentView;
	private TextView mCountDownTV;

	private long mItemId;
	private boolean mKeepLayoutHeight = false;

	public ContextualUndoView(Context context, ViewGroup parent, int undoLayoutResId, int countDownTextViewResId) {
		super(context);
		initUndo(context, parent, undoLayoutResId, countDownTextViewResId);
	}

	private void initUndo(Context context, ViewGroup parent, int undoLayoutResId, final int countDownTextViewResId) {
		mUndoView = LayoutInflater.from(context).inflate(undoLayoutResId, parent, false);
		//mUndoView = View.inflate(getContext(), undoLayoutResId, parent, false);
		addView(mUndoView);

		if (countDownTextViewResId != -1) {
			mCountDownTV = (TextView) mUndoView.findViewById(countDownTextViewResId);
		}
	}

	public void updateCountDownTimer(String timerText) {
		if (mCountDownTV != null) {
			mCountDownTV.setText(timerText);
		}
	}

	public void updateContentView(View contentView) {
		if (mContentView == null) {
			addView(contentView);
		}
		mContentView = contentView;
	}

	public View getContentView() {
		return mContentView;
	}

	public void setItemId(long itemId) {
		this.mItemId = itemId;
	}

	public long getItemId() {
		return mItemId;
	}

	public boolean isContentDisplayed() {
		return mContentView.getVisibility() == View.VISIBLE;
	}
	
	public void setKeepLayoutHeight(boolean copy) {
	    this.mKeepLayoutHeight   = copy;
	}

	public void displayUndo() {
		updateCountDownTimer("");
		if (mKeepLayoutHeight) {
		    ViewGroup.LayoutParams mLayoutParams = mUndoView.getLayoutParams();
		    mLayoutParams.height = mContentView.getHeight();
		    mUndoView.setLayoutParams(mLayoutParams);
		}
		mContentView.setVisibility(View.GONE);
		mUndoView.setVisibility(View.VISIBLE);
	}

	public void displayContentView() {
		mContentView.setVisibility(View.VISIBLE);
		mUndoView.setVisibility(View.GONE);
	}
}