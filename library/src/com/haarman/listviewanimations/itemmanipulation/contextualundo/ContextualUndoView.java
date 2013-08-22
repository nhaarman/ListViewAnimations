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
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextSwitcher;
import android.widget.ViewSwitcher.ViewFactory;

@SuppressLint("ViewConstructor")
public class ContextualUndoView extends FrameLayout {

	private View mUndoView;
	private View mContentView;
	private TextSwitcher mCountDownSwitcher = null;
	private long mItemId;

	public ContextualUndoView(Context context, int undoLayoutResourceId, final int countDownTextLayoutId, final int countDownSwitcherId) {
		super(context);
		
		initUndo(undoLayoutResourceId, countDownTextLayoutId, countDownSwitcherId);
	}

	private void initUndo(int undoLayoutResourceId, final int countDownTextLayoutId, final int countDownSwitcherId) {
		mUndoView = View.inflate(getContext(), undoLayoutResourceId, null);
		addView(mUndoView);
		if(countDownSwitcherId != -1){
			mCountDownSwitcher = (TextSwitcher)mUndoView.findViewById(countDownSwitcherId);
			mCountDownSwitcher.setFactory(new ViewFactory() {
                
                public View makeView() {
                    return View.inflate(getContext(), countDownTextLayoutId, null);
                }
            });
		}
			
	}
	
	public void updateCountDownTimer(final String timerText){
		if(mCountDownSwitcher != null)
			mCountDownSwitcher.setText(timerText);
	}

	public void updateContentView(View contentView) {
		if (this.mContentView == null) {
			addView(contentView);
		}
		this.mContentView = contentView;
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

	public void displayUndo() {
		updateCountDownTimer("");
		mContentView.setVisibility(View.GONE);
		mUndoView.setVisibility(View.VISIBLE);
	}

	public void displayContentView() {
		mContentView.setVisibility(View.VISIBLE);
		mUndoView.setVisibility(View.GONE);
	}
}