package com.nhaarman.listviewanimations;

import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.util.AbsListViewWrapper;
import com.nhaarman.listviewanimations.util.ListViewWrapper;

import junit.framework.TestCase;

import org.mockito.*;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.*;

@SuppressWarnings({"AnonymousInnerClass", "EmptyClass", "ConstantConditions"})
public class BaseAdapterDecoratorTest extends TestCase {

    private BaseAdapterDecorator mBaseAdapterDecorator;

    private BaseAdapter mBaseAdapter;

    @Mock
    private View mView;

    @Mock
    private ListView mAbsListView;

    @Mock
    private ListViewWrapper mListViewWrapper;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mBaseAdapter = spy(new BaseAdapterImpl());
        mBaseAdapterDecorator = new BaseAdapterDecorator(mBaseAdapter) {
        };
    }

    public void testGetDecoratedBaseAdapter() {
        assertThat(mBaseAdapterDecorator.getDecoratedBaseAdapter(), is(equalTo(mBaseAdapter)));
    }

    public void testGetRootAdapter() {
        assertThat(mBaseAdapterDecorator.getRootAdapter(), is(equalTo(mBaseAdapter)));
    }

    public void testGetDoubleWrappedAdapters() {
        BaseAdapterDecorator doubleWrappedAdapter = new BaseAdapterDecorator(mBaseAdapterDecorator) {
        };
        assertThat(doubleWrappedAdapter.getDecoratedBaseAdapter(), is(equalTo((BaseAdapter) mBaseAdapterDecorator)));
        assertThat(doubleWrappedAdapter.getRootAdapter(), is(equalTo(mBaseAdapter)));
    }

    public void testSetAbsListView() {
        mBaseAdapterDecorator.setAbsListView(mAbsListView);

        assertThat(mBaseAdapterDecorator.getListViewWrapper(), instanceOf(AbsListViewWrapper.class));
    }

    public void testSetListViewWrapper() {
        mBaseAdapterDecorator.setListViewWrapper(mListViewWrapper);

        assertThat(mBaseAdapterDecorator.getListViewWrapper(), is(mListViewWrapper));
    }

    public void testGetCount() {
        when(mBaseAdapter.getCount()).thenReturn(5);

        assertThat(mBaseAdapterDecorator.getCount(), is(5));
    }

    public void testGetItem() {
        Object value = new Object();
        when(mBaseAdapter.getItem(anyInt())).thenReturn(value);

        assertThat(mBaseAdapterDecorator.getItem(0), is(value));
    }

    public void testGetItemId() {
        when(mBaseAdapter.getItemId(anyInt())).thenReturn(5L);

        assertThat(mBaseAdapterDecorator.getItemId(0), is(5L));
    }

    public void testGetView() {
        when(mBaseAdapter.getView(anyInt(), any(View.class), any(ViewGroup.class))).thenReturn(mView);

        assertThat(mBaseAdapterDecorator.getView(0, null, null), is(mView));
    }

    public void testAreAllItemsEnabled() {
        when(mBaseAdapter.areAllItemsEnabled()).thenReturn(true);
        assertThat(mBaseAdapterDecorator.areAllItemsEnabled(), is(true));

        when(mBaseAdapter.areAllItemsEnabled()).thenReturn(false);
        assertThat(mBaseAdapterDecorator.areAllItemsEnabled(), is(false));
    }

    public void testGetDropDownView() {
        when(mBaseAdapter.getDropDownView(anyInt(), any(View.class), any(ViewGroup.class))).thenReturn(mView);

        assertThat(mBaseAdapterDecorator.getDropDownView(0, null, null), is(mView));
    }

    public void testGetItemViewType() {
        when(mBaseAdapter.getItemViewType(anyInt())).thenReturn(5);

        assertThat(mBaseAdapterDecorator.getItemViewType(0), is(5));
    }

    public void testGetViewTypeCount() {
        when(mBaseAdapter.getViewTypeCount()).thenReturn(5);

        assertThat(mBaseAdapterDecorator.getViewTypeCount(), is(5));
    }

    public void testHasStableIds() {
        when(mBaseAdapter.hasStableIds()).thenReturn(true);
        assertThat(mBaseAdapterDecorator.hasStableIds(), is(true));

        when(mBaseAdapter.hasStableIds()).thenReturn(false);
        assertThat(mBaseAdapterDecorator.hasStableIds(), is(false));
    }

    public void testIsEmpty() {
        when(mBaseAdapter.isEmpty()).thenReturn(true);
        assertThat(mBaseAdapterDecorator.isEmpty(), is(true));

        when(mBaseAdapter.isEmpty()).thenReturn(false);
        assertThat(mBaseAdapterDecorator.isEmpty(), is(false));
    }

    public void testIsEnabled() {
        when(mBaseAdapter.isEnabled(anyInt())).thenReturn(true);
        assertThat(mBaseAdapterDecorator.isEnabled(0), is(true));

        when(mBaseAdapter.isEnabled(anyInt())).thenReturn(false);
        assertThat(mBaseAdapterDecorator.isEnabled(0), is(false));
    }

    public void testNotifyDataSetChanged() {
        mBaseAdapterDecorator.notifyDataSetChanged();

        verify(mBaseAdapter).notifyDataSetChanged();
    }

    public void testNotifyDataSetInvalidated() {
        mBaseAdapterDecorator.notifyDataSetInvalidated();

        verify(mBaseAdapter).notifyDataSetInvalidated();
    }

    public void testRegisterDataSetObserver() {
        DataSetObserver mock = mock(DataSetObserver.class);
        mBaseAdapterDecorator.registerDataSetObserver(mock);

        verify(mBaseAdapter).registerDataSetObserver(mock);
    }

    public void testUnregisterDataSetObserver() {
        DataSetObserver mock = mock(DataSetObserver.class);
        mBaseAdapterDecorator.registerDataSetObserver(mock);
        mBaseAdapterDecorator.unregisterDataSetObserver(mock);

        verify(mBaseAdapter).unregisterDataSetObserver(mock);
    }

    @SuppressWarnings("ConstantConditions")
    protected static class BaseAdapterImpl extends BaseAdapter {

        @Override
        public int getCount() {
            return 0;
        }

        @Override
        public Object getItem(final int position) {
            return null;
        }

        @Override
        public long getItemId(final int position) {
            return 0;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            return null;
        }
    }
}