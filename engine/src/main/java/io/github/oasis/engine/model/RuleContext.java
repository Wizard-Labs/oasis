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

import io.github.oasis.engine.elements.AbstractRule;

/**
 * @author Isuru Weerarathna
 */
public class RuleContext<R extends AbstractRule> {

    private final R rule;
    private final SignalCollector collector;

    public RuleContext(R rule, SignalCollector collector) {
        this.rule = rule;
        this.collector = collector;
    }

    public static <R extends AbstractRule> RuleContext<R> create(Class<R> clz, R rule, SignalCollector collector) {
        return new RuleContext<>(rule, collector);
    }

    public R getRule() {
        return rule;
    }

    public SignalCollector getCollector() {
        return collector;
    }
}
