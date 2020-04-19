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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.context.RuleExecutionContextSupport;
import io.github.oasis.core.context.RuntimeContextSupport;
import io.github.oasis.core.elements.AbstractProcessor;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.ElementModule;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class MilestonesModule extends ElementModule {

    private final List<Class<? extends AbstractSink>> sinks = List.of(MilestonesSink.class);

    @Override
    public List<Class<? extends AbstractSink>> getSupportedSinks() {
        return sinks;
    }

    @Override
    public AbstractSink createSink(Class<? extends AbstractSink> sinkReq, RuntimeContextSupport context) {
        return new MilestonesSink(context.getDb());
    }

    @Override
    public AbstractProcessor<? extends AbstractRule, ? extends Signal> createProcessor(AbstractRule rule, RuleExecutionContextSupport ruleExecutionContext) {
        if (rule instanceof MilestoneRule) {
            RuleContext<MilestoneRule> ruleContext = new RuleContext<>((MilestoneRule) rule, ruleExecutionContext.getSignalCollector());
            return new MilestoneProcessor(ruleExecutionContext.getDb(), ruleContext);
        }
        return null;
    }
}
