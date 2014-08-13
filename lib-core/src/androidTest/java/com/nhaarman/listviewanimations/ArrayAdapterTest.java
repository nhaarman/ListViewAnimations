package com.nhaarman.listviewanimations;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

@SuppressWarnings("ConstantNamingConvention")
public class ArrayAdapterTest extends TestCase {

    private static final String A = "A";

    private static final String B = "B";

    private static final String C = "C";

    private static final String D = "D";

    private List<String> mItems;

    private ArrayAdapter<String> mArrayAdapter;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mItems = new ArrayList<>();
        mItems.addAll(Arrays.asList(A, B, C));
        mArrayAdapter = new TestArrayAdapter(mItems);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mItems = null;
        mArrayAdapter = null;
    }

    public void testNoArgCreation() {
        TestArrayAdapter testArrayAdapter = new TestArrayAdapter();
        assertThat(testArrayAdapter.getCount(), is(0));
    }

    public void testEmptyCreation() {
        List<String> items = new ArrayList<>();
        TestArrayAdapter testArrayAdapter = new TestArrayAdapter(items);

        assertThat(testArrayAdapter.getCount(), is(0));
        assertThat(testArrayAdapter.getItems(), is(items));
    }

    public void testNonEmptyCreation() {
        TestArrayAdapter testArrayAdapter = new TestArrayAdapter(mItems);

        assertThat(testArrayAdapter.getCount(), is(3));
        assertThat(testArrayAdapter.getItems(), is(mItems));
    }

    public void testGetItem() {
        assertThat(mArrayAdapter.getItem(0), is(A));
        assertThat(mArrayAdapter.getItem(1), is(B));
        assertThat(mArrayAdapter.getItem(2), is(C));
    }

    public void testAdd() {
        mArrayAdapter.add(D);

        assertThat(mArrayAdapter.getCount(), is(4));
        assertThat(mArrayAdapter.getItem(3), is(D));
    }

    public void testAddIndex() {
        mArrayAdapter.add(1, D);

        assertThat(mArrayAdapter.getCount(), is(4));
        assertThat(mArrayAdapter.getItem(0), is(A));
        assertThat(mArrayAdapter.getItem(1), is(D));
        assertThat(mArrayAdapter.getItem(2), is(B));
    }

    public void testContains() {
        assertThat(mArrayAdapter.contains(A), is(true));
        assertThat(mArrayAdapter.contains(D), is(false));
    }

    public void testClear() {
        mArrayAdapter.clear();

        assertThat(mArrayAdapter.getCount(), is(0));
        assertThat(mItems.size(), is(0));
    }

    public void testRemoveObject() {
        mArrayAdapter.remove(A);

        assertThat(mArrayAdapter.getCount(), is(2));
        assertThat(mArrayAdapter.getItem(0), is(B));
    }

    public void testRemovePosition() {
        mArrayAdapter.remove(0);

        assertThat(mArrayAdapter.getCount(), is(2));
        assertThat(mArrayAdapter.getItem(0), is(B));
    }

    public void testSwapItems() {
        mArrayAdapter.swapItems(0, 1);

        assertThat(mArrayAdapter.getCount(), is(3));
        assertThat(mArrayAdapter.getItem(0), is(B));
        assertThat(mArrayAdapter.getItem(1), is(A));
        assertThat(mArrayAdapter.getItem(2), is(C));
    }

    private static class TestArrayAdapter extends ArrayAdapter<String> {

        private TestArrayAdapter() {
        }

        private TestArrayAdapter(@Nullable final List<String> objects) {
            super(objects);
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            //noinspection ConstantConditions
            return null;
        }
    }
}