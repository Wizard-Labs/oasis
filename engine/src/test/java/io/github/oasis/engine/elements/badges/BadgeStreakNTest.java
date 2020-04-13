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

package io.github.oasis.engine.elements.badges;

import io.github.oasis.engine.elements.AbstractRuleTest;
import io.github.oasis.engine.elements.Signal;
import io.github.oasis.engine.elements.badges.rules.BadgeStreakNRule;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.model.TEvent;
import io.github.oasis.engine.elements.badges.BadgeStreakN;
import io.github.oasis.engine.elements.badges.signals.BadgeRemoveSignal;
import io.github.oasis.engine.elements.badges.signals.StreakBadgeSignal;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Streaks")
public class BadgeStreakNTest extends AbstractRuleTest {

    public static final String EVT_A = "a";
    public static final String EVT_B = "b";

    @DisplayName("Single streak: not enough elements")
    @Test
    public void testNotEnoughStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: not satisfied elements")
    @Test
    public void testNoStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 47);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: No satisfied event types")
    @Test
    public void testOrderedStreakNNoEventTypes() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_B, 50);
        TEvent e4 = TEvent.createKeyValue(106, EVT_B, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak")
    @Test
    public void testOrderedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 50);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4);

        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order break")
    @Test
    public void testOutOfOrderMisMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(ruleContext.getRule().getId(), e3.asEventScope(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order falls after outside streak")
    @Test
    public void testOutOfOrderMisMatchNotAffectedStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(107, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()) );
    }

    @DisplayName("Single streak: Out-of-order falls before outside streak")
    @Test
    public void testOutOfOrderMisMatchNotAffectedBeforeAllStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 55);
        TEvent e4 = TEvent.createKeyValue(99, EVT_A, 11);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        Assertions.assertEquals(3, ruleContext.getRule().getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(ruleContext.getRule().getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order creates new streak")
    @Test
    public void testOutOfOrderMatchStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 20);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e2, 3, 100, 104, e1.getExternalId(), e2.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order modifies existing streak end time")
    @Test
    public void testStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 50);
        TEvent e4 = TEvent.createKeyValue(101, EVT_A, 81);
        TEvent e5 = TEvent.createKeyValue(106, EVT_A, 21);

        List<Signal> signals = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Collections.singletonList(3), signals::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e2, 3, 100, 104, e1.getExternalId(), e2.getExternalId()));
    }

    // ---------------------------------------
    // MULTI STREAK TESTS
    // ---------------------------------------

    @DisplayName("Multi streaks")
    @Test
    public void testMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, 100, 107, e1.getExternalId(), e5.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order creates multiple streaks")
    @Test
    public void testOutOfOrderMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(101, EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(107, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e2, 3, 100, 104, e1.getExternalId(), e2.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e4, 5, 100, 106, e1.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order breaks the latest streak")
    @Test
    public void testOutOfOrderBreakMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(105, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(106, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(107, EVT_A, 76);
        TEvent e6 = TEvent.createKeyValue(101, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e5, 5, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), 3, 100, 105, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e5.asEventScope(), 5, 100, 107, e1.getExternalId(), e5.getExternalId()),
                new StreakBadgeSignal(rule.getId(), e4, 3, 104, 106, e2.getExternalId(), e4.getExternalId()));
    }

    @DisplayName("Multi streaks: Out-of-order breaks the only streak")
    @Test
    public void testOutOfOrderBreakAllMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(104, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(106, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(107, EVT_A, 88);
        TEvent e6 = TEvent.createKeyValue(105, EVT_A, 26);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<BadgeStreakNRule> ruleContext = createStreakNOptions(Arrays.asList(3, 5), signalsRef::add);
        BadgeStreakNRule rule = ruleContext.getRule();
        Assertions.assertEquals(5, rule.getMaxStreak());
        BadgeStreakN streakN = new BadgeStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new StreakBadgeSignal(rule.getId(), e3, 3, 100, 106, e1.getExternalId(), e3.getExternalId()),
                new BadgeRemoveSignal(rule.getId(), e3.asEventScope(), 3, 100, 106, e1.getExternalId(), e3.getExternalId()));
    }

    private RuleContext<BadgeStreakNRule> createStreakNOptions(List<Integer> streaks, Consumer<Signal> consumer) {
        BadgeStreakNRule options = new BadgeStreakNRule("abc");
        options.setForEvent(EVT_A);
        options.setStreaks(streaks);
        options.setCriteria((e,r,c) -> (long) e.getFieldValue("value") >= 50);
        options.setRetainTime(10);
        return new RuleContext<>(options, fromConsumer(consumer));
    }
}
