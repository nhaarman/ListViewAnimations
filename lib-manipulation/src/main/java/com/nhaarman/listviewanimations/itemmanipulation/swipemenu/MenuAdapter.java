package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;

import com.nhaarman.listviewanimations.itemmanipulation.DynamicListItemView;

import android.support.annotation.NonNull;
import android.view.View;

/**
 * An interface used for {@link SwipeMenuAdapter}.
 * Used to provide the menu {@link android.view.View}s.
 */
public interface MenuAdapter {
    /**
     * Returns the list of buttons({@link android.view.View}) that should be used for the left menu.
     *
     * @param position    the position of the item for which the menu should be shown.
     * @param view the view({@link DynamicListItemView}) asking for the menu
     */
    @NonNull
    View[] getLeftButtons(final int position, @NonNull final DynamicListItemView view);

    /**
     * Returns the list of buttons({@link android.view.View}) that should be used for the right menu.
     *
     * @param position    the position of the item for which the menu should be shown.
     * @param view the view({@link DynamicListItemView}) asking for the menu
     */
    @NonNull
    View[] getRightButtons(final int position, @NonNull final DynamicListItemView view);

    /**
     * Returns the whether the item should be able to show a left menu
     *
     * @param position    the position of the item for which the menu should be shown.
     * @param view the view({@link DynamicListItemView}) asking for the menu
     */
    boolean canShowLeftMenu(final int position, @NonNull final DynamicListItemView view);
    
    /**
     * Returns the whether the item should be able to show a right menu
     *
     * @param position    the position of the item for which the menu should be shown.
     * @param view the view({@link DynamicListItemView}) asking for the menu
     */
    boolean canShowRightMenu(final int position, @NonNull final DynamicListItemView view);
}
