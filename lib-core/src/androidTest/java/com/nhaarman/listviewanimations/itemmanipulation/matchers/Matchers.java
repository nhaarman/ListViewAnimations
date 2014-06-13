package com.nhaarman.listviewanimations.itemmanipulation.matchers;

import android.util.Pair;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.atomic.AtomicInteger;

public class Matchers {

    private Matchers() {
    }

    @Factory
    public static <T, F> Matcher<Pair<T, F>> pairWithValues(final T first, final F second) {
        return new PairWithValues<>(first, second);
    }

    @Factory
    public static Matcher<AtomicInteger> atomicIntegerWithValue(final int value) {
        return new AtomicIntegerWithValue(value);
    }

    private static class PairWithValues<T, F> extends TypeSafeMatcher<Pair<T, F>> {

        private final T mFirst;
        private final F mSecond;

        private PairWithValues(final T first, final F second) {
            mFirst = first;
            mSecond = second;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("Dunno!");
        }

        @Override
        protected boolean matchesSafely(final Pair<T, F> item) {
            return item.first.equals(mFirst) && item.second.equals(mSecond);
        }
    }

    private static class AtomicIntegerWithValue extends TypeSafeMatcher<AtomicInteger> {

        private final int mValue;

        AtomicIntegerWithValue(final int value) {
            mValue = value;
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText(String.valueOf(mValue));
        }

        @Override
        protected boolean matchesSafely(final AtomicInteger item) {
            return mValue == item.intValue();
        }
    }
}
