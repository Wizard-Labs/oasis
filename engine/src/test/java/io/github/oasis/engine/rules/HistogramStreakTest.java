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

import io.github.oasis.engine.rules.signals.HistogramBadgeRemovalSignal;
import io.github.oasis.engine.rules.signals.HistogramBadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import org.junit.Assert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Histogram Streaks")
public class HistogramStreakTest extends AbstractRuleTest {

    private static long FIFTY = 50;

    @DisplayName("Single streak")
    @Test
    public void testHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(187, "a", 88);
        TEvent e6 = TEvent.createKeyValue(205, "a", 26);
        TEvent e7 = TEvent.createKeyValue(235, "a", 96);
        TEvent e8 = TEvent.createKeyValue(265, "a", 11);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Collections.singletonList(3), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Multiple streaks")
    @Test
    public void testHistogramMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(187, "a", 88);
        TEvent e5 = TEvent.createKeyValue(205, "a", 26);
        TEvent e6 = TEvent.createKeyValue(235, "a", 96);
        TEvent e7 = TEvent.createKeyValue(265, "a", 91);
        TEvent e8 = TEvent.createKeyValue(312, "a", 80);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Arrays.asList(3, 5), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(options.getId(), 5, 100, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Breaks all in multiple streaks and creates a new streak/badge")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(187, "a", 88);
        TEvent e5 = TEvent.createKeyValue(205, "a", 26);
        TEvent e6 = TEvent.createKeyValue(235, "a", 96);
        TEvent e7 = TEvent.createKeyValue(265, "a", 91);
        TEvent e8 = TEvent.createKeyValue(312, "a", 80);
        TEvent e9 = TEvent.createKeyValue(170, "a", -88);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Arrays.asList(3, 5), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(options.getId(), 5, 100, 300, e8.getExternalId()),
                new HistogramBadgeRemovalSignal(options.getId(), 3, 100, 200),
                new HistogramBadgeRemovalSignal(options.getId(), 5, 100, 300),
                new HistogramBadgeSignal(options.getId(), 3, 200, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Breaks the latest streak in multiple streaks")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrderLower() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(187, "a", 88);
        TEvent e5 = TEvent.createKeyValue(205, "a", 26);
        TEvent e6 = TEvent.createKeyValue(235, "a", 96);
        TEvent e7 = TEvent.createKeyValue(265, "a", 91);
        TEvent e8 = TEvent.createKeyValue(312, "a", 80);
        TEvent e9 = TEvent.createKeyValue(275, "a", -88);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Arrays.asList(3, 5), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(options.getId(), 5, 100, 300, e8.getExternalId()),
                new HistogramBadgeRemovalSignal(options.getId(), 5, 100, 300));
    }

    @DisplayName("Multiple streaks: Out-of-order breaks the latest streak in multiple streaks")
    @Test
    public void testBreakHistogramMultiStreakNWithHoles() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63); // --
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(187, "a", 88); // --
        TEvent e5 = TEvent.createKeyValue(205, "a", 26);
        TEvent e6 = TEvent.createKeyValue(235, "a", 96); // --
        TEvent e7 = TEvent.createKeyValue(265, "a", 91); // --
        TEvent e9 = TEvent.createKeyValue(170, "a", -88);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Arrays.asList(3, 5), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeRemovalSignal(options.getId(), 3, 100, 200));
    }

    @DisplayName("Single streak: No streaks available yet")
    @Test
    public void testNoHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 7);
        TEvent e4 = TEvent.createKeyValue(187, "a", 18);
        TEvent e6 = TEvent.createKeyValue(205, "a", 26);
        TEvent e7 = TEvent.createKeyValue(235, "a", 96);
        TEvent e8 = TEvent.createKeyValue(265, "a", 71);
        TEvent e9 = TEvent.createKeyValue(285, "a", 21);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Collections.singletonList(3), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: No streaks due to non-existence buckets")
    @Test
    public void testNoHistogramStreakNWithHole() {
        TEvent e1 = TEvent.createKeyValue(100, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e6 = TEvent.createKeyValue(205, "a", 26);
        TEvent e7 = TEvent.createKeyValue(235, "a", 96);
        TEvent e8 = TEvent.createKeyValue(265, "a", 71);
        TEvent e9 = TEvent.createKeyValue(285, "a", 21);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Collections.singletonList(3), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Multiple streaks: Out-of-order breaks the latest streak in multiple streaks")
    @Test
    public void testHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 57);
        TEvent e4 = TEvent.createKeyValue(205, "a", 26);
        TEvent e5 = TEvent.createKeyValue(235, "a", 96);
        TEvent e6 = TEvent.createKeyValue(265, "a", 11);
        TEvent e7 = TEvent.createKeyValue(187, "a", 88);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Collections.singletonList(3), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(1, signals.size());
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order breaks the only single streak")
    @Test
    public void testBreakHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, "a", 75);
        TEvent e2 = TEvent.createKeyValue(144, "a", 63);
        TEvent e3 = TEvent.createKeyValue(156, "a", 99);
        TEvent e4 = TEvent.createKeyValue(205, "a", 26);
        TEvent e5 = TEvent.createKeyValue(235, "a", 96);
        TEvent e6 = TEvent.createKeyValue(265, "a", 11);
        TEvent e7 = TEvent.createKeyValue(187, "a", -50);

        List<Signal> signalsRef = new ArrayList<>();
        HistogramStreakNRule options = createOptions(Collections.singletonList(3), FIFTY, 80, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, options);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(options.getId(), 3, 100, 200, e5.getExternalId()),
                new HistogramBadgeRemovalSignal(options.getId(), 3, 100, 200));
    }

    private HistogramStreakNRule createOptions(List<Integer> streaks, long timeunit, long threshold, Consumer<Signal> consumer) {
        HistogramStreakNRule options = new HistogramStreakNRule();
        options.setId("abc");
        options.setStreaks(streaks);
        options.setConsecutive(true);
        options.setThreshold(BigDecimal.valueOf(threshold));
        options.setTimeUnit(timeunit);
        options.setConsumer(consumer);
        options.setValueResolver(event -> Double.parseDouble(event.getFieldValue("value").toString()));
        return options;
    }
}