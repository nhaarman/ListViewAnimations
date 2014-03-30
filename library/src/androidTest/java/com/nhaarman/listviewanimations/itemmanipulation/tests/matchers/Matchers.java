package com.nhaarman.listviewanimations.itemmanipulation.tests.matchers;

import android.util.Pair;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.concurrent.atomic.AtomicInteger;

public class Matchers {

    @Factory
    public static <T, F> Matcher<Pair<T, F>> pairWithValues(T first, F second) {
        return new PairWithValues<T, F>(first, second);
    }

    @Factory
    public static Matcher<AtomicInteger> atomicIntegerWithValue(int value) {
        return new AtomicIntegerWithValue(value);
    }

    private static class PairWithValues<T, F> extends TypeSafeMatcher<Pair<T, F>> {

        private T first;
        private F second;

        public PairWithValues(T first, F second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Dunno!");
        }

        @Override
        protected boolean matchesSafely(Pair<T, F> tfPair) {
            return tfPair.first.equals(first) && tfPair.second.equals(second);
        }
    }

    private static class AtomicIntegerWithValue extends TypeSafeMatcher<AtomicInteger> {

        private int mValue;

        public AtomicIntegerWithValue(int value) {
            mValue = value;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(String.valueOf(mValue));
        }

        @Override
        protected boolean matchesSafely(AtomicInteger atomicInteger) {
            return mValue == atomicInteger.intValue();
        }
    }
}
