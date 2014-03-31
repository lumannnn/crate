/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * However, if you have executed another commercial license agreement
 * with Crate these terms will supersede the license and you may use the
 * software solely pursuant to the terms of the relevant commercial agreement.
 */

package io.crate;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PartitionNameTest {

    @Test
    public void testSingleColumn() throws Exception {
        PartitionName partitionName = new PartitionName("test",
                ImmutableList.of("id"), ImmutableList.of("1"));

        assertThat(partitionName.values().size(), is(1));
        assertThat(partitionName.stringValue(),
                is(Constants.PARTITIONED_TABLE_PREFIX+".test."+PartitionName.NOT_NULL_MARKER+partitionName.values().get(0)));

        PartitionName partitionName1 = PartitionName.fromString(partitionName.stringValue(), "test", 1);
        assertEquals(partitionName.values(), partitionName1.values());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testWithoutValue() throws Exception {
        new PartitionName("test", ImmutableList.of("id"), ImmutableList.<String>of());
    }

    @Test
    public void testMultipleColumns() throws Exception {
        PartitionName partitionName = new PartitionName("test",
                ImmutableList.of("id", "name"), ImmutableList.of("1", "foo"));

        assertTrue(partitionName.isValid());
        assertThat(partitionName.values().size(), is(2));
        assertEquals(ImmutableList.of("1", "foo"), partitionName.values());

        PartitionName partitionName1 = PartitionName.fromString(partitionName.stringValue(), "test", 2);
        assertEquals(partitionName.values(), partitionName1.values());
    }

    @Test
    public void testNull() throws Exception {
        PartitionName partitionName = new PartitionName("test",
                ImmutableList.of("id"), new ArrayList<String>(){{add(null);}});

        assertTrue(partitionName.isValid());
        assertThat(partitionName.values().size(), is(1));
        assertEquals(Constants.PARTITIONED_TABLE_PREFIX+".test."+PartitionName.NULL_MARKER, partitionName.stringValue());

        PartitionName partitionName1 = PartitionName.fromString(partitionName.stringValue(), "test", 1);
        assertEquals(partitionName.values(), partitionName1.values());
    }

    @Test
    public void testEmptyStringValue() throws Exception {
        PartitionName partitionName = new PartitionName("test",
                ImmutableList.of("id"), ImmutableList.of(""));

        assertTrue(partitionName.isValid());
        assertThat(partitionName.values().size(), is(1));
        assertEquals(Constants.PARTITIONED_TABLE_PREFIX+".test."+PartitionName.NOT_NULL_MARKER, partitionName.stringValue());

        PartitionName partitionName1 = PartitionName.fromString(partitionName.stringValue(), "test", 1);
        assertEquals(partitionName.values(), partitionName1.values());
    }

    @Test (expected = IllegalArgumentException.class)
    public void testPartitionNameNotFromTable() throws Exception {
        String partitionName = Constants.PARTITIONED_TABLE_PREFIX + ".test1._1";
        PartitionName.fromString(partitionName, "test", 1);
    }

    @Test (expected = IllegalArgumentException.class)
    public void testInvalidValueString() throws Exception {
        String partitionName = Constants.PARTITIONED_TABLE_PREFIX + ".test.1";
        PartitionName.fromString(partitionName, "test", 1);
    }

    @Test
    public void testIsPartition() throws Exception {
        assertFalse(
                PartitionName.isPartition("test", "test")
        );
        assertTrue(PartitionName.isPartition(
                Constants.PARTITIONED_TABLE_PREFIX + ".test.", "test"
        ));
        assertFalse(
                PartitionName.isPartition(
                        Constants.PARTITIONED_TABLE_PREFIX + ".tast.djfhjhdgfjy",
                        "test"
                )
        );
        assertFalse(
                PartitionName.isPartition("partitioned.test.dshhjfgjsdh", "test")
        );
        assertFalse(
                PartitionName.isPartition(".test.dshhjfgjsdh", "test")
        );
    }

}