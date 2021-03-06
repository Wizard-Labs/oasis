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

package io.github.oasis.core.elements;

import io.github.oasis.core.Event;
import io.github.oasis.core.context.ExecutionContext;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractRule implements Serializable {

    private final String id;
    private String name;
    private String description;
    private Set<String> flags = new HashSet<>();
    private EventTypeMatcher eventTypeMatcher;
    private TimeRangeMatcher timeRangeMatcher;
    private EventExecutionFilter condition;
    private boolean active = true;

    public AbstractRule(String id) {
        this.id = id;
    }

    public boolean hasFlag(String flag) {
        return flags.contains(flag);
    }

    public boolean doesNotHaveFlag(String flag) {
        return !flags.contains(flag);
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public EventExecutionFilter getCondition() {
        return condition;
    }

    public void setCondition(EventExecutionFilter condition) {
        this.condition = condition;
    }

    public void setEventTypeMatcher(EventTypeMatcher eventTypeMatcher) {
        this.eventTypeMatcher = eventTypeMatcher;
    }

    public EventTypeMatcher getEventTypeMatcher() {
        return eventTypeMatcher;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setTimeRangeMatcher(TimeRangeMatcher timeRangeMatcher) {
        this.timeRangeMatcher = timeRangeMatcher;
    }

    public TimeRangeMatcher getTimeRangeMatcher() {
        return timeRangeMatcher;
    }

    public boolean isEventFalls(Event event, ExecutionContext executionContext) {
        return Objects.isNull(timeRangeMatcher) ||
                timeRangeMatcher.isBetween(event.getTimestamp(), event.getTimeZone());
    }

    public boolean isConditionMatches(Event event, ExecutionContext executionContext) {
        return Objects.isNull(condition) ||
                condition.matches(event, this, executionContext);
    }

    @Override
    public String toString() {
        return "{" + id + ", name=" + name + "}";
    }
}
