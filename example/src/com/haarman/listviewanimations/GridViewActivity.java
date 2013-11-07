package com.haarman.listviewanimations;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.util.LruCache;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import com.haarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;

public class GridViewActivity extends ActionBarActivity {

	@SuppressLint("InlinedApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT >= 19) {
			getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
		}
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gridview);

		GridView gridView = (GridView) findViewById(R.id.activity_gridview_gv);
		SwingBottomInAnimationAdapter swingBottomInAnimationAdapter = new SwingBottomInAnimationAdapter(new MyAdapter(this, getItems()));
		swingBottomInAnimationAdapter.setAbsListView(gridView);
		swingBottomInAnimationAdapter.setInitialDelayMillis(300);
		gridView.setAdapter(swingBottomInAnimationAdapter);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
	}

	private ArrayList<Integer> getItems() {
		ArrayList<Integer> items = new ArrayList<Integer>();
		for (int i = 0; i < 100; i++) {
			items.add(i);
		}
		return items;
	}

	private static class MyAdapter extends ArrayAdapter<Integer> {

		private Context mContext;
		private LruCache<Integer, Bitmap> mMemoryCache;

		public MyAdapter(Context context, List<Integer> list) {
			super(list);
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
		public View getView(int position, View convertView, ViewGroup viewGroup) {
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
