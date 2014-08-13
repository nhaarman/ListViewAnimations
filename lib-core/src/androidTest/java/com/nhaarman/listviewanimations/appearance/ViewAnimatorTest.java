package com.nhaarman.listviewanimations.appearance;

import android.test.InstrumentationTestCase;
import android.view.View;

import com.nhaarman.listviewanimations.util.ListViewWrapper;
import com.nineoldandroids.animation.Animator;

import org.mockito.Mock;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

@SuppressWarnings({"MagicNumber", "AnonymousInnerClass"})
public class ViewAnimatorTest extends InstrumentationTestCase {

    private ViewAnimator mViewAnimator;

    @Mock
    private ListViewWrapper mListViewWrapper;

    @Mock
    private View mView;

    @Mock
    private Animator mAnimator;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        initMocks(this);

        mViewAnimator = new ViewAnimator(mListViewWrapper);
    }

    public void testFirstViewAnimated() {
        getInstrumentation().runOnMainSync(
                new Runnable() {
                    @Override
                    public void run() {
                        mViewAnimator.animateViewIfNecessary(0, mView, new Animator[]{mAnimator});
                    }
                }
        );

        verify(mAnimator, timeout(500)).start();
    }

    public void testSecondViewAnimated() throws InterruptedException {
        mViewAnimator.setAnimationDelayMillis(500);

        getInstrumentation().runOnMainSync(
                new Runnable() {
                    @Override
                    public void run() {
                        mViewAnimator.animateViewIfNecessary(0, mView, new Animator[]{mAnimator});
                        mViewAnimator.animateViewIfNecessary(1, mView, new Animator[]{mAnimator});
                    }
                }
        );

        verify(mAnimator, timeout(500)).start();
        reset(mAnimator);
        Thread.sleep(100);
        verify(mAnimator, never()).start();
        verify(mAnimator, timeout(500)).start();
    }

    public void testDisabledAnimations() throws InterruptedException {
        mViewAnimator.disableAnimations();
        getInstrumentation().runOnMainSync(
                new Runnable() {
                    @Override
                    public void run() {
                        mViewAnimator.animateViewIfNecessary(0, mView, new Animator[]{mAnimator});
                        mViewAnimator.animateViewIfNecessary(1, mView, new Animator[]{mAnimator});
                    }
                }
        );

        Thread.sleep(10000);

        verify(mAnimator, never()).start();
    }

    public void testSetAnimationDuration() {
        mViewAnimator.setAnimationDurationMillis(500);
        getInstrumentation().runOnMainSync(
                new Runnable() {
                    @Override
                    public void run() {
                        mViewAnimator.animateViewIfNecessary(0, mView, new Animator[]{mAnimator});
                    }
                }
        );

        verify(mAnimator, timeout(1000)).setDuration(500);

    }

}