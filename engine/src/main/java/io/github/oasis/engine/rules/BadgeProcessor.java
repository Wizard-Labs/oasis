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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.model.Event;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author Isuru Weerarathna
 */
public class BadgeProcessor extends AbstractProcessor {
    public BadgeProcessor(JedisPool pool) {
        super(pool);
    }

    long asLong(Long value) {
        return value == null ? 0L : value;
    }

    boolean isMatchEvent(Event event, BadgeRule badgeRule) {
        return badgeRule.getForEvent().equals(event.getEventType());
    }

    protected void beforeBatchEmit(BadgeSignal signal, Event event, AbstractRule options, Jedis jedis) {
        jedis.zadd(ID.getUserBadgeSpecKey(event.getGameId(), event.getUser(), options.getId()),
                signal.getStartTime(),
                String.format("%d:%s:%d:%d", signal.getEndTime(), options.getId(), signal.getStartTime(), signal.getAttribute()));
        String userBadgesMeta = ID.getUserBadgesMetaKey(event.getGameId(), event.getUser());
        String value = jedis.hget(userBadgesMeta, options.getId());
        if (value == null) {
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(signal.getEndTime()));
            jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getAttribute()));
        } else {
            long val = Long.parseLong(value);
            if (signal.getEndTime() >= val) {
                jedis.hset(userBadgesMeta, options.getId() + ".streak", String.valueOf(signal.getAttribute()));
            }
            jedis.hset(userBadgesMeta, options.getId(), String.valueOf(Math.max(signal.getEndTime(), val)));
        }
    }

}