package com.nhaarman.listviewanimations.util;

import android.support.annotation.NonNull;

import com.nineoldandroids.animation.Animator;

public class AnimatorUtil {

    private AnimatorUtil() {
    }

    /**
     * Merges given Animators into one array.
     */
    @NonNull
    public static Animator[] concatAnimators(@NonNull final Animator[] childAnimators, @NonNull final Animator[] animators, @NonNull final Animator alphaAnimator) {
        Animator[] allAnimators = new Animator[childAnimators.length + animators.length + 1];
        int i;

        for (i = 0; i < animators.length; ++i) {
            allAnimators[i] = animators[i];
        }

        for (Animator childAnimator : childAnimators) {
            allAnimators[i] = childAnimator;
            ++i;
        }

        allAnimators[allAnimators.length - 1] = alphaAnimator;
        return allAnimators;
    }

}
