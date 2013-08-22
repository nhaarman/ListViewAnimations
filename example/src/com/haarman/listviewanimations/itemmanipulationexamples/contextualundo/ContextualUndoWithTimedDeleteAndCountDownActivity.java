package com.haarman.listviewanimations.itemmanipulationexamples.contextualundo;

import android.os.Bundle;
import com.haarman.listviewanimations.ArrayAdapter;
import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.haarman.listviewanimations.itemmanipulation.contextualundo.ContextualUndoAdapter.FormatCountDownCallback;

public class ContextualUndoWithTimedDeleteAndCountDownActivity extends MyListActivity {

	private final ArrayAdapter<String> mAdapter = createListAdapter();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ContextualUndoAdapter contextualUndoAdapter = new ContextualUndoAdapter(mAdapter, R.layout.undo_row_with_count_down, R.id.undo_row_undobutton, 3000, R.layout.undo_row_timer_text, R.id.undo_row_countdown);
		contextualUndoAdapter.setAbsListView(getListView());
		getListView().setAdapter(contextualUndoAdapter);
		contextualUndoAdapter.setDeleteItemCallback(new MyDeleteItemCallback());
		contextualUndoAdapter.setFormatCountDownCallback(new MyFormatCountDownCallback());
	}

	private class MyDeleteItemCallback implements DeleteItemCallback {

		@Override
		public void deleteItem(int position) {
			mAdapter.remove(position);
			mAdapter.notifyDataSetChanged();
		}
	}
	
	private class MyFormatCountDownCallback implements FormatCountDownCallback {

		@Override
		public String getCountDownString(long millisUntilFinished) {
			int seconds = (int)(millisUntilFinished / 1000);
			if(seconds == -1)
				return "Deleting Item.";
			else if(seconds > 1)
				return String.format("Deleting in %d seconds.", seconds);
			else if(seconds == 1)
				return String.format("Deleting in %d second.", seconds);
			else
				return "Deleting now!";
		}

	}
}