package com.nhaarman.listviewanimations.itemmanipulation;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.FrameLayout;

import com.nhaarman.listviewanimations.itemmanipulation.swipemenu.MenuContainerView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.ValueAnimator;
import com.nineoldandroids.view.ViewHelper;

public class DynamicListItemView extends FrameLayout {

    private final String TAG = "DynamicListItemView";

    public static final int DIRECTION_NONE = -1;
    public static final int DIRECTION_LEFT = 0;
    public static final int DIRECTION_RIGHT = 1;

    /**
     * The container view {@link android.view.View}.
     */
    @NonNull
    private final FrameLayout mContainerView;

    /**
     * The content {@link android.view.View}.
     */
    @Nullable
    private View mContentView;

    /**
     * The overlay {@link android.view.View}.
     */
    @Nullable
    private View mOverlayView;

    /**
     * The left menu {@link MenuContainerView}.
     */
    @Nullable
    private MenuContainerView mLeftMenu;

    /**
     * The right menu {@link MenuContainerView}.
     */
    @Nullable
    private MenuContainerView mRightMenu;

    /**
     * The elastic factor
     */
    private float mElasticFactor = 8;



    /**
     * The current direction of the menu being swiped.
     */
    private final Context mContext;


    /**
     * The right menu {@link MenuContainerView}.
     */
    @Nullable
    private MenuContainerView mCurrentMenu = null;

    /**
     * The current direction of the menu being swiped.
     */
    private int mCurrentDirection = DynamicListItemView.DIRECTION_NONE;
    private int mCurrentMenuWidth = -1;
    private int mWidth = 0;
    private float mCurrentPercent = 0;

    /**
     * when starting to swipe on an already opened menu
     * we use that offset to get the correct actual offset on move
     */
    private int mDeltaOffsetX = 0;


    /**
     * Creates a new {@code DynamicListItemView}.
     */
    public DynamicListItemView(final Context context) {
        super(context);
        mContainerView = new FrameLayout(context) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec); 
                //framelayout compute max size whatever the LayoutParams,
                //consequently while measuring the the buttons, the frame might get higher.
                //this makes sure we actually use the content height and layout menus accordingly
                if ((heightMeasureSpec <= 0) && mContentView != null ) {
                    int height = mContentView.getMeasuredHeight();
                    
                    setMeasuredDimension(getMeasuredWidth(), height);
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
                    if (mLeftMenu != null) {
                        int menuHeight = mLeftMenu.getMeasuredHeight();
                        if (menuHeight > height) {
                            measureChildWithMargins(mLeftMenu, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        }
                    }
                    if (mRightMenu != null) {
                        int menuHeight = mRightMenu.getMeasuredHeight();
                        if (menuHeight > height) {
                            measureChildWithMargins(mRightMenu, widthMeasureSpec, 0, heightMeasureSpec, 0);
                        }
                    }
                }
            }
        };
        addView(mContainerView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mContext = context;
    }

    /**
     * Sets the content {@link android.view.View}. Removes any existing content {@code View} if present.
     */
    public void setContentView(@NonNull final View contentView) {
        if (mContentView != null) {
            mContainerView.removeView(mContentView);
        }
        mContentView = contentView;
        if (contentView != null) {
            mContainerView.addView(mContentView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }
        else {
            //this is a possible case when for example:
            //sticky header where you want the header while the section is hidden
        }
    }

    /**
     * Sets the overlay {@link android.view.View}. Removes any existing overlay {@code View} if present, and sets the visibility of the {@code undoView} to {@link #GONE}.
     */
    public void setOverlayView(final View overlayView) {
        if (mOverlayView != null) {
            mContainerView.removeView(mOverlayView);
        }
        mOverlayView = overlayView;
        if (mOverlayView != null) {
            mOverlayView.setVisibility(GONE);
            mContainerView.addView(mOverlayView);
        }
    }

    /**
     * Returns the container {@link android.widget.FrameLayout}.
     */
    @Nullable
    public FrameLayout getContainerView() {
        return mContainerView;
    }

    /**
     * Returns the content {@link android.view.View}.
     */
    @Nullable
    public View getContentView() {
        return mContentView;
    }

    /**
     * Returns the overlay {@link android.view.View}.
     */
    @Nullable
    public View getOverlayView() {
        return mOverlayView;
    }

    /**
     * Returns the left menu {@link android.view.View}.
     */
    @Nullable
    public View getLeftMenu() {
        return mLeftMenu;
    }

    /**
     * Returns the left menu {@link android.view.View}.
     */
    @Nullable
    public View getRightMenu() {
        return mRightMenu;
    }

    public void prepareForReuse() {
        if (mLeftMenu != null) {
            mContainerView.removeView(mLeftMenu);
            mLeftMenu = null;
        }
        if (mRightMenu != null) {
            mContainerView.removeView(mRightMenu);
            mRightMenu = null;
        }
        reset();
    }

    public void reset() {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
            mCurrentAnimator = null;
        }
        mCurrentMenu = null;
        mDeltaOffsetX = 0;
        mCurrentDirection = -1;
        mCurrentMenuWidth = 0;
        mCurrentPercent = 0;
        if (mContentView != null) {
            ViewHelper.setTranslationX(mContentView, 0);
        }
    }

    public void setLeftButtons(View[] buttons) {
        if (mLeftMenu != null) {
            mContainerView.removeView(mLeftMenu);
            mLeftMenu = null;
        }
        
        if (buttons == null) return;
        mLeftMenu = new MenuContainerView(mContext, buttons);
        mContainerView.addView(mLeftMenu, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mLeftMenu.setVisibility(INVISIBLE);
    }

    public void setRightButtons(View[] buttons) {
        if (mRightMenu != null) {
            mContainerView.removeView(mRightMenu);
            mRightMenu = null;
        }

        if (buttons == null) return;
        mRightMenu = new MenuContainerView(mContext, buttons);
        mContainerView.addView(mRightMenu, 0, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
        mRightMenu.setVisibility(INVISIBLE);
    }

    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (mWidth != w) {
            mWidth = w;
            if (mCurrentMenu != null) {
                setPercent((mCurrentDirection == DIRECTION_RIGHT)?-1:1);
            }
        }
    }

    /**
     * set Menu showing position using percent, negative is for right menu
     */
    public void setPercent(float percent) {
        if (mCurrentMenu == null || mContentView == null) {
            return;
        }
        mCurrentPercent = percent;

        boolean isRight = (mCurrentDirection == DIRECTION_RIGHT);

        if (percent == 0) {
            mCurrentMenu.setVisibility(INVISIBLE);
            ViewHelper.setTranslationX(mContentView, 0);
            ViewHelper.setX(mCurrentMenu, isRight?mWidth:-mCurrentMenuWidth);
        }
        else {
            final float menuPercent = Math.max(-1, Math.min(percent, 1));
            final float deltaPercent = percent - menuPercent;
            if (deltaPercent != 0) {
                //elastic effect
                percent = menuPercent + deltaPercent / mElasticFactor;
            }
            final float deltaX = percent * mCurrentMenuWidth;

            ViewHelper.setTranslationX(mContentView, deltaX);
            if (mCurrentMenu.getVisibility() != VISIBLE) {
                mCurrentMenu.setVisibility(VISIBLE);
            }
            ViewHelper.setX(mCurrentMenu, isRight?(mWidth + menuPercent * mCurrentMenuWidth):((menuPercent - 1) * mCurrentMenuWidth));
        }
    }

    public boolean setSwipeOffset(final float deltaX) {
        //this is the case where we swipe to 0 from a current menu position
        //we must keep or current value and not risk switching to null
        if (deltaX != 0  || mCurrentDirection == -1) {
            mCurrentDirection = (deltaX > 0) ? DIRECTION_LEFT : DIRECTION_RIGHT;
        }
        MenuContainerView oldMenu = mCurrentMenu;
        mCurrentMenu = (mCurrentDirection == DIRECTION_RIGHT)?mRightMenu:mLeftMenu;
        if (mCurrentMenu != null) {
            if (oldMenu != null) {
                oldMenu.setVisibility(INVISIBLE);
            }
            mCurrentMenuWidth = mCurrentMenu.getMeasuredWidth();
            if (mCurrentMenuWidth > 0) {
                setPercent((deltaX) / mCurrentMenuWidth);
            }
        } else if (oldMenu != null) {
            //temp set mCurrentMenu to oldMenu to apply setPercent to it
            mCurrentMenu = oldMenu;
            setPercent(0);
            mCurrentMenu = null;
        }
        return mCurrentMenu != null;
    }


    public int getCurrentDeltaX() {
        if (mCurrentMenu != null) {
            return ((mCurrentDirection == DIRECTION_RIGHT) ? - mCurrentMenuWidth : mCurrentMenuWidth);
        }
        return 0;
    }

    public int getCurrentDirection() {
        return mCurrentDirection;
    }

    private final AnimatorListenerAdapter mOnClosedListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mCurrentAnimator = null;
            mDeltaOffsetX = 0;
            if (mCurrentMenu != null) {
                mCurrentMenu.setVisibility(INVISIBLE);
            }
            mCurrentDirection = -1;
        }
    };

    private final AnimatorListenerAdapter mOnOpenedListener = new AnimatorListenerAdapter() {
        @Override
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            mCurrentAnimator = null;
            if (mCurrentDirection == -1) {
                mDeltaOffsetX = 0;
            }
            else {
                mDeltaOffsetX = ((mCurrentDirection == DIRECTION_RIGHT) ? - mCurrentMenuWidth : mCurrentMenuWidth);
            }
        }
    };



    public int shouldOpenMenuOnUp(final float deltaX, final float velocityX, final float velocityY,
                                  final float minVelocity, final float maxVelocity) {
        //in some cases (fast swipe and release) the mCurrentMenuWidth might not be set yet
        // because the view is not laid out yet
        //so try to do get it
        if (mCurrentMenu != null && mCurrentMenuWidth == 0) {
            mCurrentMenuWidth = mCurrentMenu.getMeasuredWidth();
        }
        if (mCurrentMenuWidth == 0) {
            return -1;
        }
        float percent = (deltaX - mDeltaOffsetX) / mCurrentMenuWidth;
        float absVX = Math.abs(velocityX);
        float absVY = Math.abs(velocityY);
        if (minVelocity <= absVX && absVX <= maxVelocity && absVY < absVX) {
            if ((mCurrentDirection == DIRECTION_LEFT && velocityX < 0) ||
                (mCurrentDirection == DIRECTION_RIGHT && velocityX > 0)) {
                return -1;
            }
            if ((percent >= 0 && mCurrentDirection == DIRECTION_LEFT) ||
                    (percent <= 0 && mCurrentDirection == DIRECTION_RIGHT)   ) {
                return mCurrentDirection;
            }
        }
        else if ((percent > 0.5 && mCurrentDirection == DIRECTION_LEFT) ||
            (percent < -0.5 && mCurrentDirection == DIRECTION_RIGHT)) {
            return mCurrentDirection;
        }
//        else 
        return -1;
    }
    private Animator mCurrentAnimator = null;
    private void setPercentAnimated(final float percent, final long animationTime, final AnimatorListenerAdapter listener) {
        if (mCurrentAnimator != null) {
            mCurrentAnimator.cancel();
            mCurrentAnimator = null;
        }
        ValueAnimator animation = ValueAnimator.ofFloat(mCurrentPercent, percent);
        animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (mCurrentMenu != null) {
                    setPercent((Float)animation.getAnimatedValue());
                }
            }
        });
        animation.setDuration(animationTime);
        animation.addListener((percent == 0)?mOnClosedListener:mOnOpenedListener);
        if (listener != null) {
            animation.addListener(listener);
        }
        mCurrentAnimator = animation;
        animation.start();
    }

    public void closeMenu(final long animationTime, final AnimatorListenerAdapter listener) {
        if (mCurrentMenu == null) return;
        setPercentAnimated(0, animationTime, listener);
    }
    public void openMenu(final int direction, final long animationTime, final AnimatorListenerAdapter listener) {
        boolean isRight = (direction == DIRECTION_RIGHT);
        mCurrentMenu = isRight?mRightMenu:mLeftMenu;
        if (mCurrentMenu != null) {
            mCurrentDirection = direction;
            mCurrentMenuWidth = mCurrentMenu.getMeasuredWidth();
            setPercentAnimated(isRight?-1f:1f, animationTime, listener);
        }
    }
    public void openMenu(final int direction) {
        openMenu(direction, 0, null);
    }
}
