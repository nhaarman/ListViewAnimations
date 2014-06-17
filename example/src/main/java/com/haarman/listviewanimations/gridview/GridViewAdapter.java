package com.haarman.listviewanimations.gridview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.haarman.listviewanimations.R;
import com.haarman.listviewanimations.util.BitmapCache;
import com.nhaarman.listviewanimations.ArrayAdapter;

public class GridViewAdapter extends ArrayAdapter<Integer> {

    private final Context mContext;
    private final BitmapCache mMemoryCache;

    public GridViewAdapter(final Context context) {
        mContext = context;
        mMemoryCache = new BitmapCache();

        for (int i = 0; i < 1000; i++) {
            add(i);
        }
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {
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

    private void addBitmapToMemoryCache(final int key, final Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemCache(final int key) {
        return mMemoryCache.get(key);
    }
}
