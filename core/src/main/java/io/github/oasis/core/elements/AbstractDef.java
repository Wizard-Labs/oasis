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

import io.github.oasis.core.elements.matchers.EventTypeMatcherFactory;
import io.github.oasis.core.exception.InvalidGameElementException;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractDef implements Serializable {

    protected static final String EMPTY = "";

    private int id;
    private String name;
    private String description;

    private Object event;
    private Object events;

    private Set<String> flags;
    private Object condition;

    public static AbstractRule defToRule(AbstractDef def, AbstractRule source) {
        source.setName(def.getName());
        source.setDescription(def.getDescription());
        source.setFlags(Objects.isNull(def.flags) ? Set.of() : Set.copyOf(def.getFlags()));
        source.setEventTypeMatcher(def.deriveEventMatcher());
        source.setCondition(EventExecutionFilterFactory.create(def.condition));
        return source;
    }

    @SuppressWarnings("unchecked")
    private EventTypeMatcher deriveEventMatcher() {
        if (Objects.nonNull(event)) {
            return EventTypeMatcherFactory.createMatcher((String) event);
        } else if (Objects.nonNull(events)) {
            return EventTypeMatcherFactory.create((Collection<String>) events);
        }
        return null;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = event;
    }

    protected List<String> getSensitiveAttributes() {
        return List.of(
                Utils.firstNonNullAsStr(event, EMPTY),
                Utils.firstNonNullAsStr(events, EMPTY),
                Utils.firstNonNullAsStr(flags, EMPTY),
                Utils.firstNonNullAsStr(condition, EMPTY)
        );
    }

    public final String generateUniqueHash() {
        return Texts.md5Digest(String.join("", getSensitiveAttributes()));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Set<String> getFlags() {
        return flags;
    }

    public void setFlags(Set<String> flags) {
        this.flags = flags;
    }

    public Object getCondition() {
        return condition;
    }

    public void setCondition(Object condition) {
        this.condition = condition;
    }

    public Object getEvents() {
        return events;
    }

    public void setEvents(Object events) {
        this.events = events;
    }
}