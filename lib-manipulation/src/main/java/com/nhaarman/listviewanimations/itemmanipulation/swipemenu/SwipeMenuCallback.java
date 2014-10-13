package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;


import android.support.annotation.NonNull;
import android.view.View;

public interface SwipeMenuCallback {
    /**
     * Called when the user start swiping to show a menu.
     *
     * @param view the parent {@code View}, which contains the buttons {@link android.view.View}s.
     * @param position the position for which the undo {@code View} is shown.
     * @param direction the direction of the shown menu (0=left, 1=right).
     */
    void onStartSwipe(@NonNull View view, int position, int direction);


    /**
     * Called when the menu {@link android.view.View} is shown.
     *
     * @param view the parent {@code View}, which contains the buttons {@link android.view.View}s.
     * @param position the position for which the undo {@code View} is shown.
     * @param direction the direction of the shown menu (0=left, 1=right).
     */
    void onMenuShown(@NonNull View view, int position, int direction);

    /**
     * Called when the menu {@link android.view.View} is closed.
     *
     * @param view the parent {@code View}, which contains the buttons {@link android.view.View}s.
     * @param position the position for which the undo {@code View} is shown.
     * @param direction the direction of the shown menu (0=left, 1=right).
     */
    void onMenuClosed(@NonNull View view, int position, int direction);


    /**
     * Called before the menu {@link android.view.View} is shown.
     *
     * @param view the parent {@code View}, which contains the buttons {@link android.view.View}s.
     * @param position the position for which the undo {@code View} is shown.
     * @param direction the direction of the shown menu (0=left, 1=right).
     */
    void beforeMenuShow(@NonNull View view, int position, int direction);


    /**
     * Called before the menu {@link android.view.View} is closed.
     *
     * @param view the parent {@code View}, which contains the buttons {@link android.view.View}s.
     * @param position the position for which the undo {@code View} is shown.
     * @param direction the direction of the shown menu (0=left, 1=right).
     */
    void beforeMenuClose(@NonNull View view, int position, int direction);
}
