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

package io.github.oasis.elements.badges.rules;

import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventValueResolver;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class PeriodicOccurrencesRule extends PeriodicBadgeRule {
    public PeriodicOccurrencesRule(String id) {
        super(id);
    }

    @Override
    public void setCriteria(EventExecutionFilter criteria) {
        super.setCriteria(criteria);
        super.valueResolver = (event, ctx) -> BigDecimal.ONE;
    }

    @Override
    public void setValueResolver(EventValueResolver<ExecutionContext> valueResolver) {
        throw new IllegalStateException("Use condition instead of value resolver!");
    }
}
