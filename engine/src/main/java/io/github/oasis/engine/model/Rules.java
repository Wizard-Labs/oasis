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
import io.github.oasis.model.Event;

import java.io.Serializable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class Rules implements Serializable {

    private final ConcurrentHashMap<String, AbstractRule> ruleReferences = new ConcurrentHashMap<>();
    private final SignalCollector collector;

    private Rules(SignalCollector collector) {
        this.collector = collector;
    }

    public static Rules get(ActorSignalCollector collector) {
        return new Rules(collector);
    }

    public SignalCollector getCollector() {
        return collector;
    }

    public Iterator<AbstractRule> getAllRulesForEvent(Event event) {
        return ruleReferences.values().iterator();
    }

    public void addRule(AbstractRule rule) {
        ruleReferences.put(rule.getId(), rule);
    }

    public void updateRule(AbstractRule rule) {
        addRule(rule);
    }

    public void removeRule(String ruleId) {
        ruleReferences.remove(ruleId);
    }

}
