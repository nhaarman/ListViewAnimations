package com.nhaarman.listviewanimations.itemmanipulation.swipemenu;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.Arrays;
import java.util.List;


public class MenuContainerView extends FrameLayout {

    private List<View> mButtons;
    private LinearLayout mContainer;
    /**
     * Creates a new {@code MenuContainerView}.
     */
    public MenuContainerView(final Context context, final View[] buttons) {
        super(context);
        mButtons = Arrays.asList(buttons);
        LayoutParams defaultLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        setLayoutParams(defaultLayoutParams);
        mContainer = new LinearLayout(context);
        mContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(mContainer, defaultLayoutParams);

        for (int i = 0; i < buttons.length; i++) {
            View button = buttons[i];
            if (button.getParent() != null) {
                ((ViewGroup)button.getParent()).removeView(button);
            }
            mContainer.addView(button, defaultLayoutParams);
        }
    }



}
