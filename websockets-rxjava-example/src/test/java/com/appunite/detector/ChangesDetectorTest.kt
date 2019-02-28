/*
 * Copyright 2015 Jacek Marchwicki <jacek.marchwicki@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.appunite.detector

import com.google.common.base.Objects
import com.google.common.collect.ImmutableList

import org.junit.Before
import org.junit.Test

import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.verify

class ChangesDetectorTest {

    lateinit var mDetector: ChangesDetector<Cat, Cat>
    lateinit var mAdapter: ChangesDetector.ChangesAdapter

    class Cat(val id: Int, val name: String)

    @Before
    @Throws(Exception::class)
    fun setUp() {
        mDetector = ChangesDetector(object : ChangesDetector.Detector<Cat, Cat> {

            override fun apply(item: Cat?): Cat? {
                return item
            }

            override fun matches(item: Cat, newOne: Cat): Boolean {
                return item.id == newOne.id
            }

            override fun same(item: Cat, newOne: Cat): Boolean {
                return item.id == newOne.id && Objects.equal(item.name, newOne.name)
            }
        })
        mAdapter = mock(ChangesDetector.ChangesAdapter::class.java)
    }

    @Test
    @Throws(Exception::class)
    fun testStart() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one")), false)
        verify(mAdapter).notifyItemRangeInserted(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testForce() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one")), false)
        reset(mAdapter)
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one")), true)
        verify(mAdapter).notifyItemRangeChanged(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testForce2() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one")), false)
        reset(mAdapter)
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two")), true)
        verify(mAdapter).notifyItemRangeChanged(0, 1)
        verify(mAdapter).notifyItemRangeInserted(1, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testAddItemAtTheEnd() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one")), false)
        reset(mAdapter)
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two")), false)
        verify(mAdapter).notifyItemRangeInserted(1, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testAddItemAtTheBegining() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(2, "two")), false)
        reset(mAdapter)
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two")), false)
        verify(mAdapter).notifyItemRangeInserted(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testAddItemInTheMiddle() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(3, "tree")), false)
        reset(mAdapter)
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two"), Cat(3, "tree")), false)
        verify(mAdapter).notifyItemRangeInserted(1, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemChanged() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(3, "tree")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one1"), Cat(3, "tree")), false)
        verify(mAdapter).notifyItemRangeChanged(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemDeleted1() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two"), Cat(3, "tree")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(2, "two"), Cat(3, "tree")), false)
        verify(mAdapter).notifyItemRangeRemoved(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemDeleted2() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two"), Cat(3, "tree")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(3, "tree")), false)
        verify(mAdapter).notifyItemRangeRemoved(1, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemDeleted3() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two"), Cat(3, "tree")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(2, "two")), false)
        verify(mAdapter).notifyItemRangeRemoved(2, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemSwapped() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(3, "tree")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(2, "two"), Cat(3, "tree")), false)
        verify(mAdapter).notifyItemRangeChanged(0, 1)
    }

    @Test
    @Throws(Exception::class)
    fun testItemRemovedAndAdded() {
        mDetector.newData(mAdapter, ImmutableList.of(Cat(1, "one"), Cat(4, "four")), false)
        reset(mAdapter)

        mDetector.newData(mAdapter, ImmutableList.of(Cat(2, "two"), Cat(3, "tree"), Cat(4, "four")), false)
        verify(mAdapter).notifyItemRangeChanged(0, 1)
        verify(mAdapter).notifyItemRangeInserted(1, 1)
    }
}