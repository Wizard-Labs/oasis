/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.engine.model;

import io.github.oasis.engine.utils.Texts;

/**
 * @author Isuru Weerarathna
 */
public final class EventTypeMatcherFactory {

    public static final String ANY_OF_PREFIX = "anyOf:";
    public static final String REGEX_PREFIX = "regex:";

    public static EventTypeMatcher createMatcher(String source) {
        if (source.startsWith(ANY_OF_PREFIX)) {
            return AnyOfEventTypeMatcher.create(Texts.subStrPrefixAfter(source, ANY_OF_PREFIX));
        } else if (source.startsWith(REGEX_PREFIX)) {
            return RegexEventTypeMatcher.create(Texts.subStrPrefixAfter(source, REGEX_PREFIX));
        } else {
            return new SingleEventTypeMatcher(source);
        }
    }

}
