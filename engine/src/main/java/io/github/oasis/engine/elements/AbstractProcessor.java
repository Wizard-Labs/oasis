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

package io.github.oasis.engine.elements;

import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.EventReadWrite;
import io.github.oasis.engine.model.EventExecutionFilter;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.model.Event;

import java.io.Serializable;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractProcessor<R extends AbstractRule, S extends Signal> implements BiConsumer<Event, ExecutionContext>, Serializable {

    protected final Db dbPool;
    protected final EventReadWrite eventLoader;
    protected final R rule;
    private final RuleContext<R> ruleContext;

    public AbstractProcessor(Db dbPool, RuleContext<R> ruleCtx) {
        this(dbPool, null, ruleCtx);
    }

    public AbstractProcessor(Db dbPool, EventReadWrite eventLoader, RuleContext<R> ruleCtx) {
        this.dbPool = dbPool;
        this.ruleContext = ruleCtx;
        this.rule = ruleCtx.getRule();
        this.eventLoader = eventLoader;
    }

    public boolean isDenied(Event event, ExecutionContext context) {
        return !isMatchEvent(event, rule) || unableToProcess(event, rule, context);
    }

    @Override
    public void accept(Event event, ExecutionContext context) {
        if (isDenied(event, context)) {
            return;
        }

        try (DbContext db = dbPool.createContext()) {
            List<S> signals = process(event, rule, context, db);
            if (signals != null) {
                signals.forEach(signal -> {
                    beforeEmit(signal, event, rule, context, db);
                    ruleContext.getCollector().accept(signal, context, rule);
                });
            }
            afterEmitAll(signals, event, rule, context, db);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns timestamp related to user's timezone as specified in his/her profile.
     *
     * @param userId user id.
     * @param ts timestamp to convert.
     * @param db db context to retrieve settings.
     * @return converted timestamp to user's timezone.
     */
    protected long getUserSpecificEpochTs(long userId, long ts, DbContext db) {
        return ts;
    }

    /**
     * Calls before sending each and every signal to the collector.
     *
     * @param signal signal to be sent.
     * @param event event triggered this.
     * @param rule rule reference.
     * @param db db context.
     */
    protected abstract void beforeEmit(S signal, Event event, R rule, ExecutionContext context, DbContext db);

    /**
     * Calls after all of signals are sent to the collector.
     *
     * @param signals list of signals generated.
     * @param event event caused to trigger.
     * @param rule rule reference.
     * @param db db context.
     */
    protected void afterEmitAll(List<S> signals, Event event, R rule, ExecutionContext context, DbContext db) {
        // do nothing.
    }

    /**
     * Process the given event against given rule and returns signals.
     *
     * @param event event to process.
     * @param rule rule reference.
     * @param db db context.
     * @return list of signals to notify.
     */
    public abstract List<S> process(Event event, R rule, ExecutionContext context, DbContext db);

    private boolean isMatchEvent(Event event, AbstractRule rule) {
        return rule.getEventTypeMatcher().matches(event.getEventType());
    }

    private boolean unableToProcess(Event event, AbstractRule rule, ExecutionContext context) {
        EventExecutionFilter condition = rule.getCondition();
        if (condition == null) {
            return false;
        }
        return !condition.matches(event, rule, context);
    }

}
