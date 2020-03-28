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

import io.github.oasis.engine.rules.signals.BadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Time based non-consecutive Multi Streaks")
public class TemporalNCMultiStreakTest extends AbstractRuleTest {


    @DisplayName("Multi streak: only first streak yet")
    @Test
    public void testMultiStreakNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(130, "a", 81);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Multi streak: all streaks found")
    @Test
    public void testMultiStreakAllNInTUnit() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(125, "a", 81);
        TEvent e5 = TEvent.createKeyValue(130, "a", 77); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 130, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order unsatisfying event does not affect first streak")
    @Test
    public void testOutOfOrderNInTNoBreak() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(111, "a", 1);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order unsatisfying event does not affect second streak")
    @Test
    public void testOutOfOrderNInTNoBreakSecond() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(122, "a", 87);
        TEvent e5 = TEvent.createKeyValue(124, "a", 79); // --
        TEvent e6 = TEvent.createKeyValue(123, "a", 1);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 124, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreate() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 10);
        TEvent e4 = TEvent.createKeyValue(111, "a", 91); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(1, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 111, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a badge")
    @Test
    public void testOutOfOrderNInTCreateSecondBadge() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(122, "a", 87);
        TEvent e5 = TEvent.createKeyValue(124, "a", 1); // --
        TEvent e6 = TEvent.createKeyValue(123, "a", 51);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 123, e1.getExternalId(), e6.getExternalId()));
    }

    @DisplayName("Multi streak: Out-of-order satisfying event creates a chained badge")
    @Test
    public void testOutOfOrderNInTCreateChainedBadge() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 1);
        TEvent e4 = TEvent.createKeyValue(122, "a", 87); // --
        TEvent e5 = TEvent.createKeyValue(124, "a", 64);
        TEvent e6 = TEvent.createKeyValue(123, "a", 51); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 122, e1.getExternalId(), e4.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 124, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streak: badge due to non-consecutive events")
    @Test
    public void testNInT() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(104, "a", 63);
        TEvent e3 = TEvent.createKeyValue(108, "a", 11);
        TEvent e4 = TEvent.createKeyValue(112, "a", 50); // --
        TEvent e5 = TEvent.createKeyValue(116, "a", 88);
        TEvent e6 = TEvent.createKeyValue(120, "a", 21);
        TEvent e7 = TEvent.createKeyValue(124, "a", 51); // --
        TEvent e8 = TEvent.createKeyValue(128, "a", 96);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(2, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 112, e1.getExternalId(), e4.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 124, e1.getExternalId(), e7.getExternalId()));
    }

    @DisplayName("Multi streak: not within time unit")
    @Test
    public void testNNotInT() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 11);
        TEvent e4 = TEvent.createKeyValue(131, "a", 50);

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Multi streak: two consecutive badge streaks")
    @Test
    public void testNInTManyConsecutive() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(110, "a", 63);
        TEvent e3 = TEvent.createKeyValue(120, "a", 50); // --
        TEvent e4 = TEvent.createKeyValue(125, "a", 81);
        TEvent e5 = TEvent.createKeyValue(130, "a", 77); // --
        TEvent e6 = TEvent.createKeyValue(131, "a", 75);
        TEvent e7 = TEvent.createKeyValue(140, "a", 63);
        TEvent e8 = TEvent.createKeyValue(150, "a", 50); // --
        TEvent e9 = TEvent.createKeyValue(155, "a", 81);
        TEvent e10 = TEvent.createKeyValue(160, "a", 77); // --

        List<Signal> signalsRef = new ArrayList<>();
        TemporalStreakNRule options = createStreakNOptions(Arrays.asList(3, 5), 30, signalsRef::add);
        Assertions.assertEquals(5, options.getMaxStreak());
        StreakN streakN = new TemporalStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9, e10);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(4, signals.size());

        assertSignal(signals, new BadgeSignal(options.getId(), 3, 100, 120, e1.getExternalId(), e3.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 100, 130, e1.getExternalId(), e5.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 3, 131, 150, e6.getExternalId(), e8.getExternalId()));
        assertSignal(signals, new BadgeSignal(options.getId(), 5, 131, 160, e6.getExternalId(), e10.getExternalId()));
    }

    private TemporalStreakNRule createStreakNOptions(List<Integer> streaks, long timeUnit, Consumer<Signal> consumer) {
        TemporalStreakNRule options = new TemporalStreakNRule();
        options.setId("abc");
        options.setStreaks(streaks);
        options.setConsecutive(false);
        options.setCondition(val -> val >= 50);
        options.setRetainTime(100);
        options.setCollector(consumer);
        options.setTimeUnit(timeUnit);
        return options;
    }


}