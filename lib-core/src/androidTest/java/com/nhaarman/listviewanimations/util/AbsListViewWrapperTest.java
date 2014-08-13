package com.nhaarman.listviewanimations.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;

import junit.framework.TestCase;

import org.mockito.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AbsListViewWrapperTest extends TestCase {

    private AbsListViewWrapper mAbsListViewWrapper;

    @Mock
    private AbsListViewImpl mAbsListView;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
        mAbsListViewWrapper = new AbsListViewWrapper(mAbsListView);
    }

    public void testGetListView() {
        assertThat(mAbsListViewWrapper.getListView(), is((AbsListView) mAbsListView));
    }

    public void testGetChildAt() {
        View mock = mock(View.class);
        when(mAbsListView.getChildAt(anyInt())).thenReturn(mock);

        assertThat(mAbsListViewWrapper.getChildAt(0), is(mock));
    }

    public void testGetFirstVisiblePosition() {
        when(mAbsListView.getFirstVisiblePosition()).thenReturn(5);

        assertThat(mAbsListViewWrapper.getFirstVisiblePosition(), is(5));
    }

    public void testGetLastVisiblePosition() {
        when(mAbsListView.getLastVisiblePosition()).thenReturn(5);

        assertThat(mAbsListViewWrapper.getLastVisiblePosition(), is(5));
    }

    public void testGetCount() {
        when(mAbsListView.getCount()).thenReturn(5);

        assertThat(mAbsListView.getCount(), is(5));
    }

    public void testGetChildCount() {
        when(mAbsListView.getChildCount()).thenReturn(5);

        assertThat(mAbsListView.getChildCount(), is(5));
    }

    public void testGetHeaderViewsCount() {
        assertThat(mAbsListViewWrapper.getHeaderViewsCount(), is(0));
    }


    public void testListViewGetHeaderViewsCount() {
        ListView listView = mock(ListView.class);
        mAbsListViewWrapper = new AbsListViewWrapper(listView);

        when(listView.getHeaderViewsCount()).thenReturn(5);
        assertThat(mAbsListViewWrapper.getHeaderViewsCount(), is(5));
    }

    public void testGetPositionForView() {
        View mock = mock(View.class);
        when(mAbsListView.getPositionForView(any(View.class))).thenReturn(5);

        assertThat(mAbsListViewWrapper.getPositionForView(mock), is(5));
    }

    public void testGetAdapter() {
        ListAdapter mock = mock(ListAdapter.class);
        when(mAbsListView.getAdapter()).thenReturn(mock);

        assertThat(mAbsListViewWrapper.getAdapter(), is(mock));
    }

    public void testSmoothScrollBy() {
        mAbsListViewWrapper.smoothScrollBy(4, 5);

        verify(mAbsListView).smoothScrollBy(4, 5);
    }

    protected abstract static class AbsListViewImpl extends AbsListView {

        public AbsListViewImpl(final Context context) {
            super(context);
        }

        public AbsListViewImpl(final Context context, final AttributeSet attrs) {
            super(context, attrs);
        }

        public AbsListViewImpl(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);
        }
    }

}