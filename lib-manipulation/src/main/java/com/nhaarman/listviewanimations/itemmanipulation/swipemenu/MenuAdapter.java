package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

/**
 * An interface used for {@link SwipeMenuAdapter}.
 * Used to provide the menu {@link android.view.View}s.
 */
public interface MenuAdapter {
    /**
     * Returns the list of buttons({@link android.view.View}) that should be used for the left menu.
     *
     * @param position    the position of the item for which the undo {@code View} should be shown.
//     * @param convertView The old view to reuse, if possible. Note: You should check that this view
//     *                    is non-null and of an appropriate type before using. If it is not possible to convert
//     *                    this view to display the correct data, this method can create a new view.
//     * @param parent      The parent that this view will eventually be attached to
     */
    @NonNull
    View[] getLeftButtons(final int position);
//    View[] getLeftButtons(final int position, @Nullable final View convertView, @NonNull final ViewGroup parent);

    /**
    * Returns the list of buttons({@link android.view.View}) that should be used for the right menu.
    *
            * @param position    the position of the item for which the undo {@code View} should be shown.
    */
    @NonNull
    View[] getRightButtons(final int position);


    boolean canShowLeftMenu(final int position);
    boolean canShowRightMenu(final int position);
}
