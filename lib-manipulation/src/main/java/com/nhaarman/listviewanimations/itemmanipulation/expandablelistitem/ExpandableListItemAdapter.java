/*
 * Copyright 2014 Niek Haarman
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
 */
package com.nhaarman.listviewanimations.itemmanipulation.expandablelistitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.nhaarman.listviewanimations.ArrayAdapter;
import com.nhaarman.listviewanimations.util.AdapterViewUtil;
import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nhaarman.listviewanimations.util.ListViewWrapperSetter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * An {@link ArrayAdapter} which allows items to be expanded using an animation.
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class ExpandableListItemAdapter<T> extends ArrayAdapter<T> implements ListViewWrapperSetter {

    private static final int DEFAULTTITLEPARENTRESID = 10000;
    private static final int DEFAULTCONTENTPARENTRESID = 10001;

    @NonNull
    private final Context mContext;
    private final int mTitleParentResId;
    private final int mContentParentResId;

    @NonNull
    private final List<Long> mExpandedIds;
    private int mViewLayoutResId;
    private int mActionViewResId;
    private int mLimit;

    @Nullable
    private ListViewWrapper mListViewWrapper;

    @Nullable
    private ExpandCollapseListener mExpandCollapseListener;

    /**
     * Creates a new ExpandableListItemAdapter with an empty list.
     */
    protected ExpandableListItemAdapter(@NonNull final Context context) {
        this(context, null);
    }

    /**
     * Creates a new {@code ExpandableListItemAdapter} with the specified list,
     * or an empty list if items == null.
     */
    protected ExpandableListItemAdapter(@NonNull final Context context, @Nullable final List<T> items) {
        super(items);
        mContext = context;
        mTitleParentResId = DEFAULTTITLEPARENTRESID;
        mContentParentResId = DEFAULTCONTENTPARENTRESID;

        mExpandedIds = new ArrayList<>();
    }

    /**
     * Creates a new ExpandableListItemAdapter with an empty list. Uses given
     * layout resource for the view; titleParentResId and contentParentResId
     * should be identifiers for ViewGroups within that layout.
     */
    protected ExpandableListItemAdapter(@NonNull final Context context, final int layoutResId, final int titleParentResId, final int contentParentResId) {
        this(context, layoutResId, titleParentResId, contentParentResId, null);
    }

    /**
     * Creates a new ExpandableListItemAdapter with the specified list, or an
     * empty list if items == null. Uses given layout resource for the view;
     * titleParentResId and contentParentResId should be identifiers for
     * ViewGroups within that layout.
     */
    protected ExpandableListItemAdapter(@NonNull final Context context, final int layoutResId, final int titleParentResId, final int contentParentResId,
                                        @Nullable final List<T> items) {
        super(items);
        mContext = context;
        mViewLayoutResId = layoutResId;
        mTitleParentResId = titleParentResId;
        mContentParentResId = contentParentResId;

        mExpandedIds = new ArrayList<>();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void setListViewWrapper(@NonNull final ListViewWrapper listViewWrapper) {
        mListViewWrapper = listViewWrapper;
    }

    /**
     * Set the resource id of the child {@link android.view.View} contained in the View
     * returned by {@link #getTitleView(int, android.view.View, android.view.ViewGroup)} that will be the
     * actuator of the expand / collapse animations.<br>
     * If there is no View in the title View with given resId, a
     * {@link NullPointerException} is thrown.</p> Default behavior: the whole
     * title View acts as the actuator.
     *
     * @param resId the resource id.
     */
    public void setActionViewResId(final int resId) {
        mActionViewResId = resId;
    }

    /**
     * Set the maximum number of items allowed to be expanded. When the
     * (limit+1)th item is expanded, the first expanded item will collapse.
     *
     * @param limit the maximum number of items allowed to be expanded. Use <= 0
     *              for no limit.
     */
    public void setLimit(final int limit) {
        mLimit = limit;
        mExpandedIds.clear();
        notifyDataSetChanged();
    }

    /**
     * Set the {@link ExpandCollapseListener} that should be notified of expand / collapse events.
     */
    public void setExpandCollapseListener(@Nullable final ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
    }

    @Override
    @NonNull
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
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

        View titleView = getTitleView(position, viewHolder.titleView, viewHolder.titleParent);
        if (!titleView.equals(viewHolder.titleView)) {
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
        if (!contentView.equals(viewHolder.contentView)) {
            viewHolder.contentParent.removeAllViews();
            viewHolder.contentParent.addView(contentView);
        }
        viewHolder.contentView = contentView;

        viewHolder.contentParent.setVisibility(mExpandedIds.contains(getItemId(position)) ? View.VISIBLE : View.GONE);
        viewHolder.contentParent.setTag(getItemId(position));

        LayoutParams layoutParams = viewHolder.contentParent.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        viewHolder.contentParent.setLayoutParams(layoutParams);

        return view;
    }

    /**
     * Get a View that displays the <b>title of the data</b> at the specified
     * position in the data set. You can either create a View manually or
     * inflate it from an XML layout file. When the View is inflated, the parent
     * View (GridView, ListView...) will apply default layout parameters unless
     * you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the
     *                    item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check
     *                    that this view is non-null and of an appropriate type before
     *                    using. If it is not possible to convert this view to display
     *                    the correct data, this method can create a new view.
     * @param parent      The parent that this view will eventually be attached to
     *
     * @return A View corresponding to the title of the data at the specified
     * position.
     */
    @NonNull
    public abstract View getTitleView(int position, @Nullable View convertView, @NonNull ViewGroup parent);

    /**
     * Get a View that displays the <b>content of the data</b> at the specified
     * position in the data set. You can either create a View manually or
     * inflate it from an XML layout file. When the View is inflated, the parent
     * View (GridView, ListView...) will apply default layout parameters unless
     * you use
     * {@link android.view.LayoutInflater#inflate(int, android.view.ViewGroup, boolean)}
     * to specify a root view and to prevent attachment to the root.
     *
     * @param position    The position of the item within the adapter's data set of the
     *                    item whose view we want.
     * @param convertView The old view to reuse, if possible. Note: You should check
     *                    that this view is non-null and of an appropriate type before
     *                    using. If it is not possible to convert this view to display
     *                    the correct data, this method can create a new view.
     * @param parent      The parent that this view will eventually be attached to
     *
     * @return A View corresponding to the content of the data at the specified
     * position.
     */
    @NonNull
    public abstract View getContentView(int position, @Nullable View convertView, @NonNull ViewGroup parent);

    /**
     * Indicates if the item at the specified position is expanded.
     *
     * @param position Index of the view whose state we want.
     *
     * @return true if the view is expanded, false otherwise.
     */
    public boolean isExpanded(final int position) {
        long itemId = getItemId(position);
        return mExpandedIds.contains(itemId);
    }

    /**
     * Return the title view at the specified position.
     *
     * @param position Index of the view we want.
     *
     * @return the view if it exist, null otherwise.
     */
    @Nullable
    public View getTitleView(final int position) {
        View titleView = null;

        View parentView = findViewForPosition(position);
        if (parentView != null) {
            Object tag = parentView.getTag();
            if (tag instanceof ViewHolder) {
                titleView = ((ViewHolder) tag).titleView;
            }
        }

        return titleView;
    }

    /**
     * Return the content view at the specified position.
     *
     * @param position Index of the view we want.
     *
     * @return the view if it exist, null otherwise.
     */
    @Nullable
    public View getContentView(final int position) {
        View contentView = null;

        View parentView = findViewForPosition(position);
        if (parentView != null) {
            Object tag = parentView.getTag();
            if (tag instanceof ViewHolder) {
                contentView = ((ViewHolder) tag).contentView;
            }
        }

        return contentView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();

        Collection<Long> removedIds = new HashSet<>(mExpandedIds);

        for (int i = 0; i < getCount(); ++i) {
            long id = getItemId(i);
            removedIds.remove(id);
        }

        mExpandedIds.removeAll(removedIds);
    }

    /**
     * Expand the view at given position. Will do nothing if the view is already expanded.
     *
     * @param position the position to expand.
     */
    public void expand(final int position) {
        long itemId = getItemId(position);
        if (mExpandedIds.contains(itemId)) {
            return;
        }

        toggle(position);
    }

    /**
     * Collapse the view at given position. Will do nothing if the view is already collapsed.
     *
     * @param position the position to collapse.
     */
    public void collapse(final int position) {
        long itemId = getItemId(position);
        if (!mExpandedIds.contains(itemId)) {
            return;
        }

        toggle(position);
    }

    /**
     * Toggle the {@link android.view.View} at given position, ignores header or footer Views.
     *
     * @param position the position of the view to toggle.
     */
    public void toggle(final int position) {
        long itemId = getItemId(position);
        boolean isExpanded = mExpandedIds.contains(itemId);

        View contentParent = getContentParent(position);
        if (contentParent != null) {
            toggle(contentParent);
        }

        if (contentParent == null && isExpanded) {
            mExpandedIds.remove(itemId);
        } else if (contentParent == null) {
            mExpandedIds.add(itemId);
        }
    }

    @NonNull
    private ViewGroup createView(@NonNull final ViewGroup parent) {
        ViewGroup view;

        if (mViewLayoutResId == 0) {
            view = new RootView(mContext);
        } else {
            view = (ViewGroup) LayoutInflater.from(mContext).inflate(mViewLayoutResId, parent, false);
        }

        return view;
    }

    /**
     * Return the content parent at the specified position.
     *
     * @param position Index of the view we want.
     *
     * @return the view if it exist, null otherwise.
     */
    @Nullable
    private View getContentParent(final int position) {
        View contentParent = null;

        View parentView = findViewForPosition(position);
        if (parentView != null) {
            Object tag = parentView.getTag();
            if (tag instanceof ViewHolder) {
                contentParent = ((ViewHolder) tag).contentParent;
            }
        }

        return contentParent;
    }

    @Nullable
    private View findViewForPosition(final int position) {
        if (mListViewWrapper == null) {
            throw new IllegalStateException("Call setAbsListView on this ExpanableListItemAdapter!");
        }

        View result = null;
        for (int i = 0; i < mListViewWrapper.getChildCount() && result == null; i++) {
            View childView = mListViewWrapper.getChildAt(i);
            if (childView != null && AdapterViewUtil.getPositionForView(mListViewWrapper, childView) == position) {
                result = childView;
            }
        }
        return result;
    }

    private int findPositionForId(final long id) {
        for (int i = 0; i < getCount(); i++) {
            if (getItemId(i) == id) {
                return i;
            }
        }
        return -1;
    }

    private void toggle(@NonNull final View contentParent) {
        if (mListViewWrapper == null) {
            throw new IllegalStateException("No ListView set!");
        }


        boolean isVisible = contentParent.getVisibility() == View.VISIBLE;
        boolean shouldCollapseOther = !isVisible && mLimit > 0 && mExpandedIds.size() >= mLimit;
        if (shouldCollapseOther) {
            Long firstId = mExpandedIds.get(0);

            int firstPosition = findPositionForId(firstId);
            View firstEV = getContentParent(firstPosition);
            if (firstEV != null) {
                ExpandCollapseHelper.animateCollapsing(firstEV);
            }
            mExpandedIds.remove(firstId);

            if (mExpandCollapseListener != null) {
                mExpandCollapseListener.onItemCollapsed(firstPosition);
            }
        }

        Long id = (Long) contentParent.getTag();
        int position = findPositionForId(id);
        if (isVisible) {
            ExpandCollapseHelper.animateCollapsing(contentParent);
            mExpandedIds.remove(id);

            if (mExpandCollapseListener != null) {
                mExpandCollapseListener.onItemCollapsed(position);
            }

        } else {
            ExpandCollapseHelper.animateExpanding(contentParent, mListViewWrapper);
            mExpandedIds.add(id);

            if (mExpandCollapseListener != null) {
                mExpandCollapseListener.onItemExpanded(position);
            }
        }
    }

    public interface ExpandCollapseListener {

        void onItemExpanded(int position);

        void onItemCollapsed(int position);
    }

    private static class RootView extends LinearLayout {

        private ViewGroup mTitleViewGroup;
        private ViewGroup mContentViewGroup;

        private RootView(@NonNull final Context context) {
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

    private static class ViewHolder {
        ViewGroup titleParent;
        ViewGroup contentParent;
        View titleView;
        View contentView;
    }

    private static class ExpandCollapseHelper {

        public static void animateCollapsing(final View view) {
            int origHeight = view.getHeight();

            ValueAnimator animator = createHeightAnimator(view, origHeight, 0);
            animator.addListener(
                    new AnimatorListenerAdapter() {

                        @Override
                        public void onAnimationEnd(final Animator animation) {
                            view.setVisibility(View.GONE);
                        }
                    }
            );
            animator.start();
        }

        public static void animateExpanding(@NonNull final View view, @NonNull final ListViewWrapper listViewWrapper) {
            view.setVisibility(View.VISIBLE);

            View parent = (View) view.getParent();
            final int widthSpec = View.MeasureSpec.makeMeasureSpec(parent.getMeasuredWidth() - parent.getPaddingLeft() - parent.getPaddingRight(), View.MeasureSpec.AT_MOST);
            final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
            view.measure(widthSpec, heightSpec);

            ValueAnimator animator = createHeightAnimator(view, 0, view.getMeasuredHeight());
            animator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        final int listViewHeight = listViewWrapper.getListView().getHeight();
                        final int listViewBottomPadding = listViewWrapper.getListView().getPaddingBottom();
                        final View v = findDirectChild(view, listViewWrapper.getListView());

                        @Override
                        public void onAnimationUpdate(final ValueAnimator animation) {
                            final int bottom = v.getBottom();
                            if (bottom > listViewHeight) {
                                final int top = v.getTop();
                                if (top > 0) {
                                    listViewWrapper.smoothScrollBy(Math.min(bottom - listViewHeight + listViewBottomPadding, top), 0);
                                }
                            }
                        }
                    }
            );
            animator.start();
        }

        public static ValueAnimator createHeightAnimator(final View view, final int start, final int end) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {

                        @Override
                        public void onAnimationUpdate(final ValueAnimator animation) {
                            int value = (Integer) animation.getAnimatedValue();

                            LayoutParams layoutParams = view.getLayoutParams();
                            layoutParams.height = value;
                            view.setLayoutParams(layoutParams);
                        }
                    }
            );
            return animator;
        }

        @NonNull
        private static View findDirectChild(@NonNull final View view, @NonNull final ViewGroup listView) {
            View result = view;
            View parent = (View) result.getParent();
            while (!parent.equals(listView)) {
                result = parent;
                parent = (View) result.getParent();
            }
            return result;
        }
    }

    private class TitleViewOnClickListener implements View.OnClickListener {

        private final View mContentParent;

        private TitleViewOnClickListener(final View contentParent) {
            mContentParent = contentParent;
        }

        @Override
        public void onClick(final View view) {
            toggle(mContentParent);
        }
    }
}
