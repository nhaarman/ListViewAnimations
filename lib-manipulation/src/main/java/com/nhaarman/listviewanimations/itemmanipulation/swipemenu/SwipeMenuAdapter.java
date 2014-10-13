package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.nhaarman.listviewanimations.BaseAdapterDecorator;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListItemView;
import com.nhaarman.listviewanimations.itemmanipulation.DynamicListView;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.SimpleSwipeUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.undo.UndoAdapter;
import com.nhaarman.listviewanimations.util.ListViewWrapper;

/**
 * Adds swipe-menu behaviour to the {@link android.widget.AbsListView}, using a {@link SwipeMenuTouchListener}.
 */
public class SwipeMenuAdapter extends BaseAdapterDecorator {

    @NonNull
    private final Context mContext;

    /**
     * The {@link SwipeMenuTouchListener} that is set to the {@link android.widget.AbsListView}.
     */
    @Nullable
    private SwipeMenuTouchListener mSwipeMenuTouchListener;

    /**
     * The {@link SwipeMenuCallback} that is used.
     */
    @NonNull
    private SwipeMenuCallback mMenuCallback;

    private UndoAdapter mUndoAdapter;
    /**
     * Create a new {@code SwipeMenuAdapter}, decorating given {@link android.widget.BaseAdapter}.
     *
     * @param baseAdapter  the {@link android.widget.BaseAdapter} to decorate.
     * @param context         the {@link android.content.Context}.
     * @param SwipeMenuCallback the {@link SwipeMenuCallback} that is used.
     */
    public SwipeMenuAdapter(@NonNull final BaseAdapter baseAdapter, @NonNull final Context context, @NonNull final SwipeMenuCallback SwipeMenuCallback) {
        super(baseAdapter);
        mMenuCallback = SwipeMenuCallback;
        mContext = context;

        BaseAdapter undoAdapter = baseAdapter;
        while (undoAdapter instanceof BaseAdapterDecorator) {
            undoAdapter = ((BaseAdapterDecorator) undoAdapter).getDecoratedBaseAdapter();
            if (undoAdapter instanceof UndoAdapter) {
                mUndoAdapter = (UndoAdapter) undoAdapter;
                break;
            }
        }
    }

    @Override
    public void setListViewWrapper(@NonNull final ListViewWrapper listViewWrapper) {
        super.setListViewWrapper(listViewWrapper);
        mSwipeMenuTouchListener = new SwipeMenuTouchListener(listViewWrapper, getRootAdapter(), mMenuCallback);

        if (!(listViewWrapper.getListView() instanceof DynamicListView)) {
            listViewWrapper.getListView().setOnTouchListener(mSwipeMenuTouchListener);
        }
        else {
            ((DynamicListView)listViewWrapper.getListView()).setSwipeMenuTouchListener(mSwipeMenuTouchListener);
        }
    }

//    /**
//     * Sets the {@link com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.DismissableManager} to specify which views can or cannot be swiped.
//     *
//     * @param dismissableManager {@code null} for no restrictions.
//     */
//    public void setDismissableManager(@Nullable final DismissableManager dismissableManager) {
//        if (mSwipeMenuTouchListener == null) {
//            throw new IllegalStateException("You must call setAbsListView() first.");
//        }
//        mSwipeMenuTouchListener.setDismissableManager(dismissableManager);
//    }

    public void setSwipeMenuTouchListener(@NonNull final SwipeMenuTouchListener swipeMenuTouchListener) {
        mSwipeMenuTouchListener = swipeMenuTouchListener;
    }
    @Override
    public void onNotifyDataSetChanged() {
        super.onNotifyDataSetChanged();
        if (mSwipeMenuTouchListener != null) {
            mSwipeMenuTouchListener.onNotifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent) {
        DynamicListItemView view = (DynamicListItemView) convertView;
        if (view == null) {
            view = new DynamicListItemView(mContext);
        }
        else {
            view.prepareForReuse();
        }
        View contentView = super.getView(position, view.getContentView(), view);
        view.setContentView(contentView);
        
        if (mUndoAdapter != null) {
            View overlayView = mUndoAdapter.getUndoView(position, view.getOverlayView(), view);
            view.setOverlayView(overlayView);
//            mUndoAdapter.getUndoClickView(overlayView).setOnClickListener(new SimpleSwipeUndoAdapter.UndoClickListener(view, position));
        }


        boolean isOpenedMenu = position == mSwipeMenuTouchListener.getCurrentMenuPosition();
        if (isOpenedMenu) {
            view.openMenu(mSwipeMenuTouchListener.getCurrentMenuDirection());
        }
        else if (convertView != null) {
            view.prepareForReuse();
        }
//        primaryView.setVisibility(isInUndoState ? View.GONE : View.VISIBLE);
//        undoView.setVisibility(isInUndoState ? View.VISIBLE : View.GONE);

        return view;
    }


    /**
     * Sets the {@link SwipeMenuCallback} to use.
     */
    public void setSwipeMenuCallback(@NonNull final SwipeMenuCallback SwipeMenuCallback) {
        mMenuCallback = SwipeMenuCallback;
    }

    @NonNull
    public SwipeMenuCallback getSwipeMenuCallback() {
        return mMenuCallback;
    }

    public void closeMenus() {
        mSwipeMenuTouchListener.closeMenus();
    }
    public void closeMenusAnimated() {
        mSwipeMenuTouchListener.closeMenusAnimated();
    }
    public int getCurrentMenuPosition(){return mSwipeMenuTouchListener.getCurrentMenuPosition();}
}