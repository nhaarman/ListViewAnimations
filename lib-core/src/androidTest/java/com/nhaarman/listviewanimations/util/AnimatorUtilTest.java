package com.nhaarman.listviewanimations.util;


import com.nineoldandroids.animation.Animator;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;


public class AnimatorUtilTest extends TestCase {

    private List<Animator> mAnimators = new ArrayList<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();

        for (int i = 0; i < 5; i++) {
            mAnimators.add(mock(Animator.class));
        }
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mAnimators.clear();
    }

    public void testEmptyArrays() {
        Animator[] animators = AnimatorUtil.concatAnimators(new Animator[0], new Animator[0], mAnimators.get(0));

        assertThat(animators.length, is(1));
        assertThat(animators[0], is(mAnimators.get(0)));
    }

    public void testSingleLengthArrays() {
        Animator[] animators = AnimatorUtil.concatAnimators(
                new Animator[]{mAnimators.get(0)},
                new Animator[]{mAnimators.get(1)},
                mAnimators.get(2)
        );

        assertThat(animators.length, is(3));
        assertThat(animators[0], is(mAnimators.get(0)));
        assertThat(animators[1], is(mAnimators.get(1)));
        assertThat(animators[2], is(mAnimators.get(2)));
    }

    public void testMultipleLengthArrays() {
        Animator[] animators = AnimatorUtil.concatAnimators(
                new Animator[]{mAnimators.get(0), mAnimators.get(1)},
                new Animator[]{mAnimators.get(2), mAnimators.get(3)},
                mAnimators.get(4)
        );

        assertThat(animators.length, is(5));
        assertThat(mAnimators.indexOf(animators[0]), is(0));
        assertThat(mAnimators.indexOf(animators[1]), is(1));
        assertThat(mAnimators.indexOf(animators[2]), is(2));
        assertThat(mAnimators.indexOf(animators[3]), is(3));
        assertThat(mAnimators.indexOf(animators[4]), is(4));
    }
}