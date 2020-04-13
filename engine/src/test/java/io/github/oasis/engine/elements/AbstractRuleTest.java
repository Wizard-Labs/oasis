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
import io.github.oasis.engine.external.redis.RedisDb;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.SignalCollector;
import io.github.oasis.engine.elements.badges.signals.BadgeSignal;
import io.github.oasis.engine.elements.milestones.MilestoneSignal;
import io.github.oasis.engine.elements.points.PointSignal;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractRuleTest {

    protected ExecutionContext defaultContext = ExecutionContext.withUserTz(0);

    protected static Db pool;

    @BeforeAll
    public static void beforeAll() throws IOException {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(5);
        JedisPool poolRedis = new JedisPool(config, "localhost");
        pool = RedisDb.create(poolRedis);
        pool.init();
    }

    @AfterAll
    public static void afterAll() throws IOException {
        pool.close();
    }

    @BeforeEach
    public void beforeEach() {
        try (DbContext db = pool.createContext()) {
            Set<String> keys = db.allKeys("*");
            System.out.println("Cleaning keys " + keys);
            for (String k : keys) {
                db.removeKey(k);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    public void afterEach() {
        try (DbContext db = pool.createContext()) {
            Map<String, String> keys = db.MAP(ID.getUserBadgesMetaKey(1, 0L)).getAll();
            System.out.println("Badges: " + keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void submitOrder(BiConsumer<Event, ExecutionContext> eventConsumer, Event... events) {
        submitOrder(eventConsumer, defaultContext, events);
    }

    protected void submitOrder(BiConsumer<Event, ExecutionContext> eventConsumer, ExecutionContext context, Event... events) {
        for (Event event : events) {
            eventConsumer.accept(event, context);
        }
    }

    protected Set<Signal> mergeSignals(List<Signal> refSignals) {
        Set<Signal> signals = new HashSet<>();
        for (Signal signal : refSignals) {
            signals.remove(signal);
            signals.add(signal);
        }
        return signals;
    }

    protected SignalCollector fromConsumer(Consumer<Signal> eventConsumer) {
        return (SignalCollector) (signal, context, rule) -> eventConsumer.accept(signal);
    }

    protected void assertSignal(Collection<Signal> signals, BadgeSignal badgeSignal) {
        Assertions.assertTrue(signals.contains(badgeSignal), "Badge not found!\n Expected: " + badgeSignal.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(badgeSignal) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided badge has different attributes! " + badgeSignal.toString());
    }

    protected void assertSignal(Collection<Signal> signals, MilestoneSignal milestoneSignal) {
        Assertions.assertTrue(signals.contains(milestoneSignal), "Milestone not found!\n Expected: " + milestoneSignal.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(milestoneSignal) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided milestone has different attributes! " + milestoneSignal.toString());
    }

    protected void assertSignal(Collection<Signal> signals, Signal signalRef) {
        Assertions.assertTrue(signals.contains(signalRef), "Signal not found!\n Expected: " + signalRef.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(signalRef) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided signal has different attributes! " + signalRef.toString());
    }

    protected void assertSignal(Collection<Signal> signals, PointSignal pointSignal) {
        Assertions.assertTrue(signals.contains(pointSignal), "Point not found!\n Expected: " + pointSignal.toString());
        Optional<Signal> signal = signals.stream().filter(s -> s.compareTo(pointSignal) == 0).findFirst();
        Assertions.assertTrue(signal.isPresent(), "Provided point has different attributes! " + pointSignal.toString());
    }

    private void assertStrict(Collection<Signal> signals, BadgeSignal... badgeSignals) {
        if (badgeSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No badges excepted but found many!");
            return;
        }
        Assertions.assertEquals(badgeSignals.length, signals.size(), "Expected number of badges are different!");
        for (BadgeSignal badgeSignal : badgeSignals) {
            assertSignal(signals, badgeSignal);
        }
    }

    protected void assertStrict(Collection<Signal> signals, Signal... challengeSignals) {
        if (challengeSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No signals excepted but found many!");
            return;
        }
        Assertions.assertEquals(challengeSignals.length, signals.size(), "Expected number of signals are different!");
        for (Signal challengeSignal : challengeSignals) {
            assertSignal(signals, challengeSignal);
        }
    }

    protected void assertStrict(Collection<Signal> signals, MilestoneSignal... milestoneSignals) {
        if (milestoneSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No milestones excepted but found many!");
            return;
        }
        Assertions.assertEquals(milestoneSignals.length, signals.size(), "Expected number of levels are different!");
        for (MilestoneSignal signal : milestoneSignals) {
            assertSignal(signals, signal);
        }
    }

    protected void assertRedisHashMapValue(String baseKey, String subKey, String value) {
        try (DbContext db = pool.createContext()) {
            String dbValue = db.MAP(baseKey).getValue(subKey);
            Assertions.assertEquals(value, dbValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void assertStrict(Collection<Signal> signals, PointSignal... pointSignals) {
        if (pointSignals == null) {
            Assertions.assertTrue(signals.isEmpty(), "No badges excepted but found many!");
            return;
        }
        Assertions.assertEquals(pointSignals.length, signals.size(), "Expected number of points are different!");
        for (PointSignal pointSignal : pointSignals) {
            assertSignal(signals, pointSignal);
        }
    }

}
