package com.haarman.listviewanimations.itemmanipulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.haarman.listviewanimations.ArrayAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

/**
 * An {@link ArrayAdapter} which allows items to be expanded using an animation.
 */
public abstract class ExpandableListItemAdapter<T> extends ArrayAdapter<T> {

	private static final int DEFAULTTITLEPARENTRESID = 10000;
	private static final int DEFAULTCONTENTPARENTRESID = 10001;

	private Context mContext;
	private int mViewLayoutResId;
	private int mTitleParentResId;
	private int mContentParentResId;
	private int mActionViewResId;
	private List<Long> mVisibleIds;

	private int mLimit;
	private Map<Long, View> mExpandedViews;

	/**
	 * Creates a new ExpandableListItemAdapter with an empty list.
	 */
	protected ExpandableListItemAdapter(Context context) {
		this(context, null);
	}

	/**
	 * Creates a new {@link ExpandableListItemAdapter} with the specified list,
	 * or an empty list if items == null.
	 */
	protected ExpandableListItemAdapter(Context context, List<T> items) {
		super(items);
		mContext = context;
		mTitleParentResId = DEFAULTTITLEPARENTRESID;
		mContentParentResId = DEFAULTCONTENTPARENTRESID;

		mVisibleIds = new ArrayList<Long>();
	}

	/**
	 * Creates a new ExpandableListItemAdapter with an empty list. Uses given
	 * layout resource for the view; titleParentResId and contentParentResId
	 * should be identifiers for ViewGroups within that layout.
	 */
	protected ExpandableListItemAdapter(Context context, int layoutResId, int titleParentResId, int contentParentResId) {
		this(context, layoutResId, titleParentResId, contentParentResId, null);
	}

	/**
	 * Creates a new ExpandableListItemAdapter with the specified list, or an
	 * empty list if items == null. Uses given layout resource for the view;
	 * titleParentResId and contentParentResId should be identifiers for
	 * ViewGroups within that layout.
	 */
	protected ExpandableListItemAdapter(Context context, int layoutResId, int titleParentResId, int contentParentResId, List<T> items) {
		super(items);
		mContext = context;
		mViewLayoutResId = layoutResId;
		mTitleParentResId = titleParentResId;
		mContentParentResId = contentParentResId;

		mVisibleIds = new ArrayList<Long>();
		mExpandedViews = new HashMap<Long, View>();
	}

	/**
	 * Set the resource id of the child {@link View} contained in the View returned by
	 * {@link #getTitleView(int, View, ViewGroup)} that will be the actuator of the expand / collapse animations.<br>
	 * If there is no View in the title View with given resId, a {@link NullPointerException} is thrown.</p>
	 * Default behavior: the whole title View acts as the actuator.
	 * @param resId the resource id.
	 */
	public void setActionViewResId(int resId) {
		mActionViewResId = resId;
	}

	/**
	 * Set the maximum number of items allowed to be expanded. When the (limit+1)th item is expanded, the first expanded item will collapse.
	 * @param limit the maximum number of items allowed to be expanded. Use <= 0 for no limit.
	 */
	public void setLimit(int limit) {
		mLimit = limit;
		mVisibleIds.clear();
		mExpandedViews.clear();
		notifyDataSetChanged();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view = (ViewGroup) convertView;
		ViewHolder viewHolder;

		if (view == null) {
			view = createView(parent);

			viewHolder = new ViewHolder();
			viewHolder.titleParent = (ViewGroup) view.findViewById(mTitleParentResId);
			viewHolder.contentParent = (ViewGroup) view.findViewById(mContentParentResId);

			view.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) view.getTag();
		}

		if (mLimit > 0) {
			if (mVisibleIds.contains(getItemId(position))) {
				mExpandedViews.put(getItemId(position), view);
			} else if (mExpandedViews.containsValue(view) && !mVisibleIds.contains(getItemId(position))) {
				mExpandedViews.remove(getItemId(position));
			}
		}

		View titleView = getTitleView(position, viewHolder.titleView, viewHolder.titleParent);
		if (titleView != viewHolder.titleView) {
			viewHolder.titleParent.removeAllViews();
			viewHolder.titleParent.addView(titleView);

			if (mActionViewResId == 0) {
				view.setOnClickListener(new TitleViewOnClickListener(viewHolder.contentParent));
			} else {
				view.findViewById(mActionViewResId).setOnClickListener(new TitleViewOnClickListener(viewHolder.contentParent));
			}
		}
		viewHolder.titleView = titleView;

		View contentView = getContentView(position, viewHolder.contentView, viewHolder.contentParent);
		if (contentView != viewHolder.contentView) {
			viewHolder.contentParent.removeAllViews();
			viewHolder.contentParent.addView(contentView);
		}
		viewHolder.contentView = contentView;

		viewHolder.contentParent.setVisibility(mVisibleIds.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
		viewHolder.contentParent.setTag(getItemId(position));

		return view;
	}

	private ViewGroup createView(ViewGroup parent) {
		ViewGroup view;

		if (mViewLayoutResId == 0) {
			view = new RootView(mContext);
		} else {
			view = (ViewGroup) LayoutInflater.from(mContext).inflate(mViewLayoutResId, parent, false);
		}

		return view;
	}

	/**
	 * Get a View that displays the title of the data at the specified position
	 * in the data set. You can either create a View manually or inflate it from
	 * an XML layout file. When the View is inflated, the parent View (GridView,
	 * ListView...) will apply default layout parameters unless you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the title of the data at the specified
	 *         position.
	 */
	public abstract View getTitleView(int position, View convertView, ViewGroup parent);

	/**
	 * Get a View that displays the content of the data at the specified
	 * position in the data set. You can either create a View manually or
	 * inflate it from an XML layout file. When the View is inflated, the parent
	 * View (GridView, ListView...) will apply default layout parameters unless
	 * you use
	 * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
	 * to specify a root view and to prevent attachment to the root.
	 * 
	 * @param position
	 *            The position of the item within the adapter's data set of the
	 *            item whose view we want.
	 * @param convertView
	 *            The old view to reuse, if possible. Note: You should check
	 *            that this view is non-null and of an appropriate type before
	 *            using. If it is not possible to convert this view to display
	 *            the correct data, this method can create a new view.
	 * @param parent
	 *            The parent that this view will eventually be attached to
	 * @return A View corresponding to the content of the data at the specified
	 *         position.
	 */
	public abstract View getContentView(int position, View convertView, ViewGroup parent);

	private static class ViewHolder {
		ViewGroup titleParent;
		ViewGroup contentParent;
		View titleView;
		View contentView;
	}

	private static class RootView extends LinearLayout {

		private ViewGroup mTitleViewGroup;
		private ViewGroup mContentViewGroup;

		public RootView(Context context) {
			super(context);
			init();
		}

		private void init() {
			setOrientation(VERTICAL);

			mTitleViewGroup = new FrameLayout(getContext());
			mTitleViewGroup.setId(DEFAULTTITLEPARENTRESID);
			addView(mTitleViewGroup);

			mContentViewGroup = new FrameLayout(getContext());
			mContentViewGroup.setId(DEFAULTCONTENTPARENTRESID);
			addView(mContentViewGroup);
		}
	}

	private class TitleViewOnClickListener implements View.OnClickListener {

		private View mContentParent;

		private TitleViewOnClickListener(View contentParent) {
			this.mContentParent = contentParent;
		}

		@Override
		public void onClick(View view) {
			boolean isVisible = mContentParent.getVisibility() == View.VISIBLE;
			if (!isVisible && mLimit > 0 && mVisibleIds.size() >= mLimit) {
				Long firstId = mVisibleIds.get(0);
				View firstEV = mExpandedViews.get(firstId);
				if (firstEV != null) {
					ViewHolder firstVH = ((ViewHolder) firstEV.getTag());
					ViewGroup contentParent = firstVH.contentParent;
					ExpandCollapseHelper.animateCollapsing(contentParent);
					mExpandedViews.remove(mVisibleIds.get(0));
				}
				mVisibleIds.remove(mVisibleIds.get(0));
			}

			if (isVisible) {
				ExpandCollapseHelper.animateCollapsing(mContentParent);
				mVisibleIds.remove(mContentParent.getTag());
				mExpandedViews.remove(mContentParent.getTag());
			} else {
				ExpandCollapseHelper.animateExpanding(mContentParent);
				mVisibleIds.add((Long) mContentParent.getTag());

				if (mLimit > 0) {
					View parent = (View) mContentParent.getParent();
					mExpandedViews.put((Long) mContentParent.getTag(), parent);
				}
			}
		}
	}

	private static class ExpandCollapseHelper {

		public static void animateCollapsing(final View view) {
			int origHeight = view.getHeight();

			ValueAnimator animator = createHeightAnimator(view, origHeight, 0);
			animator.addListener(new AnimatorListenerAdapter() {

				@Override
				public void onAnimationEnd(Animator animator) {
					view.setVisibility(View.GONE);
				}
			});
			animator.start();
		}

		public static void animateExpanding(final View view) {
			view.setVisibility(View.VISIBLE);

			final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			view.measure(widthSpec, heightSpec);

			ValueAnimator animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
			animator.start();
		}

		public static ValueAnimator createHeightAnimator(final View view, int start, int end) {
			ValueAnimator animator = ValueAnimator.ofInt(start, end);
			animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

				@Override
				public void onAnimationUpdate(ValueAnimator valueAnimator) {
					int value = (Integer) valueAnimator.getAnimatedValue();

					ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
					layoutParams.height = value;
					view.setLayoutParams(layoutParams);
				}
			});
			return animator;
		}
	}
}
