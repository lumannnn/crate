/*
 * Licensed to CRATE Technology GmbH ("Crate") under one or more contributor
 * license agreements.  See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.  Crate licenses
 * this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.  You may
 * obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

package io.crate.action.sql.parser;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class SqlParseHeaderTest {

    private final SQLHeaderContext context = new SQLHeaderContext();
    private final SQLHeaderParser parser = new SQLHeaderParser(context);

    @Test(expected = SQLParseHeaderException.class)
    public void testIfMatchHeaderWrong() throws Exception {
        parser.parseIfMatch("\"table#docId1@1234wrong\"");
    }

    @Test
    public void testIfMatchHeader() throws Exception {
        // "table#docId1@1234",     "spaceTable#docId23@1339""anotherTable#docId2@4321","yetAnotherTable#docId4_-123@1338"
        String ifMatch = "\"table#docId1@1234\",         \"spaceTable#docId23@1339\"" +
                "\"anotherTable#docId2@4321\",\"yetAnotherTable#docId4_-123@1338\"";
        parser.parseIfMatch(ifMatch);
        Map<String, SQLHeaderContext.DocInfo> actualContext = context.ifMatch();
        Map<String, SQLHeaderContext.DocInfo> expectedContext = new HashMap<>();
        expectedContext.put("table", new SQLHeaderContext.DocInfo("docId1", 1234L));
        expectedContext.put("spaceTable", new SQLHeaderContext.DocInfo("docId23", 1339L));
        expectedContext.put("anotherTable", new SQLHeaderContext.DocInfo("docId2", 4321L));
        expectedContext.put("yetAnotherTable", new SQLHeaderContext.DocInfo("docId4_-123", 1338L));

        assertTrue(compareMaps(expectedContext, actualContext));
    }

    private boolean compareMaps(Map<String, SQLHeaderContext.DocInfo> m1, Map<String, SQLHeaderContext.DocInfo> m2) {
        if (m1.keySet().size() != m2.keySet().size()) {
            return false;
        }
        for (Map.Entry<String, SQLHeaderContext.DocInfo> entry : m1.entrySet()) {
            String key = entry.getKey();
            SQLHeaderContext.DocInfo v2 = m2.get(key);
            if (v2 == null || !v2.equals(entry.getValue())) {
                return false;
            }
        }
        return true;
    }

}
