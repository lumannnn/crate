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

public class SQLHeaderParser {

    private final SQLHeaderContext context;

    private final HeaderIfMatchParser parser = new HeaderIfMatchParser();

    public SQLHeaderParser(SQLHeaderContext context) {
        this.context = context;
    }

    private void validate() throws SQLParseHeaderException {
        // TODO
    }

    public void parseIfMatch(String ifMatch) {
        if (ifMatch != null && ifMatch.length() > 0) {
            parser.parse(ifMatch, context);
        }
    }

    private static final TableNameAppender tableNameAppender = new TableNameAppender();
    private static final DocIdAppender docIdAppender = new DocIdAppender();
    private static final VersionAppender versionAppender = new VersionAppender();

    protected static class HeaderIfMatchParser {
        protected void parse(String header, SQLHeaderContext context) {
            // Supported syntax:
            // "tableName#docId1@version1", "otherTableName#docId@version2", ...
            int sbSize = header.length();
            StringBuilder token = new StringBuilder(sbSize);

            Appender appender = tableNameAppender;
            ParserContext parserContext = new ParserContext();
            boolean beginning = false;
            for (char currentChar : header.toCharArray()) {
                switch (currentChar) {
                    case '"':
                        if (!beginning) {
                            // beginning of a new 'if-match' element. syntax requires, that it's a tableName
                            beginning = true;
                            appender = tableNameAppender;
                        } else {
                            // end of an 'if-match' element.
                            // version is parsed -> add to parserContext;
                            beginning = false;
                            try {
                                parserContext.version = (Long) appender.convert(token);
                            } catch (NumberFormatException e) {
                                throw new SQLParseHeaderException("Invalid 'If-Match' HTTP header format.");
                            }
                            // element is parsed -> add to context
                            context.ifMatch().put(
                                    parserContext.tableName,
                                    new SQLHeaderContext.DocInfo(
                                            parserContext.docId,
                                            parserContext.version
                                    )
                            );
                            token = new StringBuilder(sbSize);
                            parserContext.clear();
                        }
                        break;
                    case '#':
                        // beginning of the docId.
                        // tableName is parsed -> add to parserContext
                        parserContext.tableName = (String)appender.convert(token);
                        token = new StringBuilder(sbSize);
                        appender = docIdAppender;
                        break;
                    case '@':
                        // beginning of the version
                        // docId is parsed -> add to parserContext
                        parserContext.docId = (String)appender.convert(token);
                        token = new StringBuilder(sbSize);
                        appender = versionAppender;
                        break;
                    case ',':
                    case ' ':
                        break;
                    default:
                        appender.append(currentChar, token);
                }
            }
        }

    }

    private static class ParserContext {
        protected String tableName;
        protected String docId;
        protected Long version;

        public void clear() {
            tableName = null;
            docId = null;
            version = null;
        }
    }

    private static abstract class Appender<T> {
        public void append(char currentChar, StringBuilder token) {
            token.append(currentChar);
        };
        public abstract T convert(StringBuilder token);
    }

    private static class TableNameAppender extends Appender<String> {
        @Override
        public String convert(StringBuilder token) {
            return token.toString();
        }
    }

    private static class DocIdAppender extends Appender<String> {
        @Override
        public String convert(StringBuilder token) {
            return token.toString();
        }
    }

    private static class VersionAppender extends Appender<Long> {
        @Override
        public Long convert(StringBuilder token) {
            return new Long(token.toString());
        }
    }

}
