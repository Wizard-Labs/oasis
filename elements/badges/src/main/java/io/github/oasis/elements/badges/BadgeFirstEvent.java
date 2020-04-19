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

package io.github.oasis.elements.badges;

import io.github.oasis.core.Event;
import io.github.oasis.elements.badges.rules.BadgeFirstEventRule;
import io.github.oasis.elements.badges.signals.BadgeSignal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.ID;
import io.github.oasis.core.elements.RuleContext;

import java.util.List;

/**
 * Awards a badge when something occurs for the very first time.
 *
 * @author Isuru Weerarathna
 */
public class BadgeFirstEvent extends BadgeProcessor<BadgeFirstEventRule> {

    public BadgeFirstEvent(Db pool, RuleContext<BadgeFirstEventRule> ruleContext) {
        super(pool, ruleContext);
    }

    @Override
    public List<BadgeSignal> process(Event event, BadgeFirstEventRule rule, ExecutionContext context, DbContext db) {
        String key = ID.getUserFirstEventsKey(event.getGameId(), event.getUser());
        long ts = event.getTimestamp();
        String id = event.getExternalId();
        String subKey = rule.getEventName();
        String value = ts + ":" + id + ":" + System.currentTimeMillis();
        if (isFirstOne(db.setIfNotExistsInMap(key, subKey, value))) {
            return List.of(BadgeSignal.firstEvent(rule.getId(), event, rule.getAttributeId()));
        }
        return null;
    }

    private boolean isFirstOne(boolean status) {
        return status;
    }

}
