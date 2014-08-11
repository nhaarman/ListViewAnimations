package com.nhaarman.listviewanimations.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import junit.framework.TestCase;

import org.mockito.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

public class AdapterViewUtilTest extends TestCase {

    private static final int POSITION = 5;

    @Mock
    private ListViewWrapper mListViewWrapper;

    @Mock
    private AbsListViewImpl mAbsListView;

    @Mock
    private ListView mListView;

    @Mock
    private View mView;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);
    }

    public void testListViewWrapperGetPositionForView() throws Exception {
        when(mListViewWrapper.getPositionForView(any(View.class))).thenReturn(5);
        when(mListViewWrapper.getHeaderViewsCount()).thenReturn(0);

        assertThat(AdapterViewUtil.getPositionForView(mListViewWrapper, mView), is(5));
    }

    public void testListViewWrapperGetPositionForViewWithHeaderViews() throws Exception {
        when(mListViewWrapper.getPositionForView(any(View.class))).thenReturn(5);
        when(mListViewWrapper.getHeaderViewsCount()).thenReturn(2);

        assertThat(AdapterViewUtil.getPositionForView(mListViewWrapper, mView), is(3));
    }

    public void testAbsListViewGetPositionForView() throws Exception {
        when(mAbsListView.getPositionForView(any(View.class))).thenReturn(5);

        assertThat(AdapterViewUtil.getPositionForView(mAbsListView, mView), is(5));
    }

    public void testListViewGetPositionForView() throws Exception {
        when(mListView.getPositionForView(any(View.class))).thenReturn(5);
        when(mListView.getHeaderViewsCount()).thenReturn(0);

        assertThat(AdapterViewUtil.getPositionForView(mListView, mView), is(5));
    }

    public void testListViewGetPositionForViewWithHeaderViews() throws Exception {
        when(mListView.getPositionForView(any(View.class))).thenReturn(5);
        when(mListView.getHeaderViewsCount()).thenReturn(2);

        assertThat(AdapterViewUtil.getPositionForView(mListView, mView), is(3));
    }

    public void testListViewWrapperGetViewForPosition() throws Exception {
        View dummyView1 = mock(View.class);
        View dummyView2 = mock(View.class);

        when(mListViewWrapper.getChildAt(0)).thenReturn(dummyView1);
        when(mListViewWrapper.getChildAt(1)).thenReturn(mView);
        when(mListViewWrapper.getChildAt(2)).thenReturn(dummyView2);

        when(mListViewWrapper.getPositionForView(dummyView1)).thenReturn(POSITION - 1);
        when(mListViewWrapper.getPositionForView(mView)).thenReturn(POSITION);
        when(mListViewWrapper.getPositionForView(dummyView2)).thenReturn(POSITION + 1);

        assertThat(AdapterViewUtil.getPositionForView(mListViewWrapper, mView), is(POSITION));
    }

    public void testListViewWrapperGetViewForPositionWithHeaderViews() throws Exception {
        View dummyView1 = mock(View.class);
        View dummyView2 = mock(View.class);

        when(mListViewWrapper.getChildAt(0)).thenReturn(dummyView1);
        when(mListViewWrapper.getChildAt(1)).thenReturn(mView);
        when(mListViewWrapper.getChildAt(2)).thenReturn(dummyView2);

        when(mListViewWrapper.getPositionForView(dummyView1)).thenReturn(POSITION - 1);
        when(mListViewWrapper.getPositionForView(mView)).thenReturn(POSITION);
        when(mListViewWrapper.getPositionForView(dummyView2)).thenReturn(POSITION + 1);

        when(mListViewWrapper.getHeaderViewsCount()).thenReturn(2);

        assertThat(AdapterViewUtil.getPositionForView(mListViewWrapper, mView), is(POSITION - 2));
    }

    public void testAbsListViewGetViewForPosition() throws Exception {
        View dummyView1 = mock(View.class);
        View dummyView2 = mock(View.class);

        when(mAbsListView.getChildAt(0)).thenReturn(dummyView1);
        when(mAbsListView.getChildAt(1)).thenReturn(mView);
        when(mAbsListView.getChildAt(2)).thenReturn(dummyView2);

        when(mAbsListView.getPositionForView(dummyView1)).thenReturn(POSITION - 1);
        when(mAbsListView.getPositionForView(mView)).thenReturn(POSITION);
        when(mAbsListView.getPositionForView(dummyView2)).thenReturn(POSITION + 1);

        assertThat(AdapterViewUtil.getPositionForView(mAbsListView, mView), is(POSITION));
    }

    public void testListViewGetViewForPosition() throws Exception {
        View dummyView1 = mock(View.class);
        View dummyView2 = mock(View.class);

        when(mListView.getChildAt(0)).thenReturn(dummyView1);
        when(mListView.getChildAt(1)).thenReturn(mView);
        when(mListView.getChildAt(2)).thenReturn(dummyView2);

        when(mListView.getPositionForView(dummyView1)).thenReturn(POSITION - 1);
        when(mListView.getPositionForView(mView)).thenReturn(POSITION);
        when(mListView.getPositionForView(dummyView2)).thenReturn(POSITION + 1);

        assertThat(AdapterViewUtil.getPositionForView(mListView, mView), is(POSITION));
    }

    public void testListViewGetViewForPositionWithHeaderViews() throws Exception {
        View dummyView1 = mock(View.class);
        View dummyView2 = mock(View.class);

        when(mListView.getChildAt(0)).thenReturn(dummyView1);
        when(mListView.getChildAt(1)).thenReturn(mView);
        when(mListView.getChildAt(2)).thenReturn(dummyView2);

        when(mListView.getPositionForView(dummyView1)).thenReturn(POSITION - 1);
        when(mListView.getPositionForView(mView)).thenReturn(POSITION);
        when(mListView.getPositionForView(dummyView2)).thenReturn(POSITION + 1);

        when(mListView.getHeaderViewsCount()).thenReturn(2);

        assertThat(AdapterViewUtil.getPositionForView(mListView, mView), is(POSITION - 2));
    }

    @SuppressWarnings("ProtectedInnerClass")
    protected abstract static class AbsListViewImpl extends AbsListView {

        protected AbsListViewImpl(final Context context) {
            super(context);
        }

        protected AbsListViewImpl(final Context context, final AttributeSet attrs) {
            super(context, attrs);
        }

        protected AbsListViewImpl(final Context context, final AttributeSet attrs, final int defStyle) {
            super(context, attrs, defStyle);
        }
    }
}