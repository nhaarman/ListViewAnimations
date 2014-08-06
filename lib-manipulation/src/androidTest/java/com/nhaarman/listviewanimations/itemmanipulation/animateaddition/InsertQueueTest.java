/*
 * Copyright 2014 Niek Haarman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nhaarman.listviewanimations.itemmanipulation.animateaddition;

import android.util.Pair;

import com.nhaarman.listviewanimations.itemmanipulation.animateaddition.InsertQueue;
import com.nhaarman.listviewanimations.util.Insertable;

import junit.framework.TestCase;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static com.nhaarman.listviewanimations.itemmanipulation.matchers.Matchers.*;

public class InsertQueueTest extends TestCase {

    private InsertQueue<Integer> mInsertQueue;

    @Mock
    private Insertable<Integer> mInsertable;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mInsertQueue = new InsertQueue<>(mInsertable);
    }

    /**
     * Test whether an initial insert actually inserts the item and activates the item.
     */
    public void testInitialInsert() {
        mInsertQueue.insert(0, 0);
        verify(mInsertable, times(1)).add(0, 0);

        assertThat(mInsertQueue.getActiveIndexes(), hasItem(0));
    }

    /**
     * Test whether a second insert while the first insert is still active:
     * - doesn't insert the item
     * - doesn't activate the item
     * - queues the item.
     */
    public void testSecondInsert() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.insert(0, 1);

        verify(mInsertable, times(0)).add(0, 1);

        assertThat(mInsertQueue.getActiveIndexes(), not(hasItem(1)));

        assertThat(mInsertQueue.getPendingItemsToInsert(), hasItem(pairWithValues(0, 1)));
    }

    /**
     * Test whether clearing a single activated item actually clears the item.
     */
    public void testClearActive() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.clearActive();

        assertThat(mInsertQueue.getActiveIndexes(), empty());
    }

    /**
     * Test whether inserting two items and then clearing the active items will
     * - activate the second inserted item,
     * - insert the second item
     */
    public void testDequeueOneElement() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.insert(0, 1);
        mInsertQueue.clearActive();

        verify(mInsertable).add(0, 1);

        assertThat(mInsertQueue.getActiveIndexes(), contains(0));
    }


    /**
     * Test whether adding two items to the queue remain in correct order
     */
    public void testQueueOrder() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.insert(0, 1);
        mInsertQueue.insert(0, 2);

        List<Pair<Integer, Integer>> pendingItemsToInsert = mInsertQueue.getPendingItemsToInsert();
        assertThat(pendingItemsToInsert.get(0), is(pairWithValues(0, 1)));
        assertThat(pendingItemsToInsert.get(1), is(pairWithValues(0, 2)));

    }

    /**
     * Test whether inserting three items and then clearing the active items will activate the second and third inserted item
     */
    public void testDequeueTwoElements() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.insert(0, 1);
        mInsertQueue.insert(0, 2);
        mInsertQueue.clearActive();

        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> itemCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(mInsertable, atLeast(3)).add(indexCaptor.capture(), itemCaptor.capture());

        List<Integer> indexValues = indexCaptor.getAllValues();
        List<Integer> itemValues = itemCaptor.getAllValues();

        assertThat(indexValues.get(1), is(0));
        assertThat(itemValues.get(1), is(1));
        assertThat(indexValues.get(2), is(0));
        assertThat(itemValues.get(2), is(2));

        //noinspection unchecked
        assertThat(mInsertQueue.getActiveIndexes(), hasItems(0, 1));
    }

    /**
     * Test whether inserting three items, clearing, inserting two items yields the correct order.
     */
    public void testDequeueTwoElementsTwice() {
        mInsertQueue.insert(0, 0);
        mInsertQueue.insert(0, 1);
        mInsertQueue.insert(0, 2);
        mInsertQueue.clearActive();
        mInsertQueue.insert(0, 3);
        mInsertQueue.insert(0, 4);
        mInsertQueue.clearActive();

        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> itemCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(mInsertable, atLeast(5)).add(indexCaptor.capture(), itemCaptor.capture());

        List<Integer> indexValues = indexCaptor.getAllValues();
        List<Integer> itemValues = itemCaptor.getAllValues();

        assertThat(indexValues.get(1), is(0));
        assertThat(itemValues.get(1), is(1));
        assertThat(indexValues.get(2), is(0));
        assertThat(itemValues.get(2), is(2));
        assertThat(indexValues.get(3), is(0));
        assertThat(itemValues.get(3), is(3));
        assertThat(indexValues.get(4), is(0));
        assertThat(itemValues.get(4), is(4));
    }

    /**
     * Test whether inserting three items, clearing, inserting two items yields the correct order.
     */
    public void testDequeueTwoElementsTwiceRandomOrder() {
        mInsertQueue.insert(5, 0);
        mInsertQueue.insert(2, 1);
        mInsertQueue.insert(3, 2);
        mInsertQueue.clearActive();
        mInsertQueue.insert(7, 3);
        mInsertQueue.insert(1, 4);
        mInsertQueue.clearActive();

        ArgumentCaptor<Integer> indexCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Integer> itemCaptor = ArgumentCaptor.forClass(Integer.class);

        verify(mInsertable, atLeast(5)).add(indexCaptor.capture(), itemCaptor.capture());

        List<Integer> indexValues = indexCaptor.getAllValues();
        List<Integer> itemValues = itemCaptor.getAllValues();

        assertThat(indexValues.get(1), is(2));
        assertThat(itemValues.get(1), is(1));
        assertThat(indexValues.get(2), is(3));
        assertThat(itemValues.get(2), is(2));
        assertThat(indexValues.get(3), is(7));
        assertThat(itemValues.get(3), is(3));
        assertThat(indexValues.get(4), is(1));
        assertThat(itemValues.get(4), is(4));
    }
}
