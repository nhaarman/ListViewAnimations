package com.haarman.listviewanimations.itemmanipulationexamples;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.haarman.listviewanimations.MyListActivity;
import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.itemmanipulation.ExpandableListItemAdapter;
import com.haarman.listviewanimations.swinginadapters.prepared.AlphaInAnimationAdapter;

public class ExpandableListItemActivity extends MyListActivity {

	private MyExpandableListItemAdapter mExpandableListItemAdapter;
	private boolean mLimited;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mExpandableListItemAdapter = new MyExpandableListItemAdapter(this, getItems());
		AlphaInAnimationAdapter alphaInAnimationAdapter = new AlphaInAnimationAdapter(mExpandableListItemAdapter);
		alphaInAnimationAdapter.setAbsListView(getListView());
		alphaInAnimationAdapter.setInitialDelayMillis(500);
		getListView().setAdapter(alphaInAnimationAdapter);

		Toast.makeText(this, R.string.explainexpand, Toast.LENGTH_LONG).show();
	}

	private static class MyExpandableListItemAdapter extends ExpandableListItemAdapter<Integer> {

		private Context mContext;
		private LruCache<Integer, Bitmap> mMemoryCache;

		/**
		 * Creates a new ExpandableListItemAdapter with the specified list, or an empty list if
		 * items == null.
		 */
		private MyExpandableListItemAdapter(Context context, List<Integer> items) {
			super(context, R.layout.activity_expandablelistitem_card, R.id.activity_expandablelistitem_card_title, R.id.activity_expandablelistitem_card_content, items);
			mContext = context;

			final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

			// Use 1/8th of the available memory for this memory cache.
			final int cacheSize = maxMemory;
			mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(Integer key, Bitmap bitmap) {
					// The cache size will be measured in kilobytes rather than
					// number of items.
					return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
				}
			};
		}

		@Override
		public View getTitleView(int position, View convertView, ViewGroup parent) {
			TextView tv = (TextView) convertView;
			if (tv == null) {
				tv = new TextView(mContext);
			}
			tv.setText(mContext.getString(R.string.expandorcollapsecard, getItem(position)));
			return tv;
		}

		@Override
		public View getContentView(int position, View convertView, ViewGroup parent) {
			ImageView imageView = (ImageView) convertView;
			if (imageView == null) {
				imageView = new ImageView(mContext);
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			}

			int imageResId;
			switch (getItem(position) % 5) {
			case 0:
				imageResId = R.drawable.img_nature1;
				break;
			case 1:
				imageResId = R.drawable.img_nature2;
				break;
			case 2:
				imageResId = R.drawable.img_nature3;
				break;
			case 3:
				imageResId = R.drawable.img_nature4;
				break;
			default:
				imageResId = R.drawable.img_nature5;
			}

			Bitmap bitmap = getBitmapFromMemCache(imageResId);
			if (bitmap == null) {
				bitmap = BitmapFactory.decodeResource(mContext.getResources(), imageResId);
				addBitmapToMemoryCache(imageResId, bitmap);
			}
			imageView.setImageBitmap(bitmap);

			return imageView;
		}

		private void addBitmapToMemoryCache(int key, Bitmap bitmap) {
			if (getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}

		private Bitmap getBitmapFromMemCache(int key) {
			return mMemoryCache.get(key);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_expandablelistitem, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_expandable_limit:
			mLimited = !mLimited;
			item.setChecked(mLimited);
			mExpandableListItemAdapter.setLimit(mLimited ? 2 : 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
