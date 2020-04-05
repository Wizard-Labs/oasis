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

import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.processors.ChallengeProcessor;
import io.github.oasis.engine.rules.signals.ChallengeOverSignal;
import io.github.oasis.engine.rules.signals.ChallengePointsAwardedSignal;
import io.github.oasis.engine.rules.signals.ChallengeWinSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Challenges")
public class ChallengeTest extends AbstractRuleTest {

    private static final String EVT_A = "a";
    private static final String EVT_B = "b";

    static final BigDecimal AWARD = BigDecimal.valueOf(100).setScale(Constants.SCALE, BigDecimal.ROUND_HALF_UP);

    static final long START = 0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;

    static final int WIN_3 = 3;

    @DisplayName("No relevant events, no winners")
    @Test
    public void testWithoutWinner() {
        TEvent e1 = TEvent.createKeyValue(1,100, EVT_B, 87);
        TEvent e2 = TEvent.createKeyValue(2,105, EVT_B, 53);
        TEvent e3 = TEvent.createKeyValue(3,110, EVT_B, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(ChallengeRule.ChallengeAwardMethod.REPEATABLE, rule.getAwardMethod());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("No criteria satisfied, no winners")
    @Test
    public void testNoWinnerUnsatisfiedCriteria() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 17);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 23);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 45);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Same points for each winner")
    @Test
    public void testSamePointsWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 25);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2)
                );
    }

    @DisplayName("Different points for each winner")
    @Test
    public void testDifferentPointsWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,115, EVT_A, 25);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setCustomAwardPoints(this::award);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), asDecimal(7), e1),
                new ChallengePointsAwardedSignal(rule.getId(), asDecimal(33), e2)
                );
    }

    @DisplayName("Winner limit exceeded")
    @Test
    public void testWinnerLimitExceeded() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 3, U4, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e4),
                new ChallengeOverSignal(rule.getId(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("Non-repeatable winners")
    @Test
    public void testNonRepeatableWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U1,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setAwardMethod(ChallengeRule.ChallengeAwardMethod.NON_REPEATABLE);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(ChallengeRule.ChallengeAwardMethod.NON_REPEATABLE, rule.getAwardMethod());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 3, U4, e5.getTimestamp(), e5.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e5)
        );
    }

    @DisplayName("Repeatable winners")
    @Test
    public void testRepeatableWinners() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U1,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U1,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U4,160, EVT_A, 99);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(ChallengeRule.ChallengeAwardMethod.REPEATABLE, rule.getAwardMethod());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U1, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 3, U1, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e4),
                new ChallengeOverSignal(rule.getId(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("No winners after expired")
    @Test
    public void testNoWinnersAfterExpired() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 150, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U1, e1.getTimestamp(), e1.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e1),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2)
        );
    }

    @DisplayName("No winners before the start")
    @Test
    public void testNoWinnersBeforeTheStart() {
        TEvent e1 = TEvent.createKeyValue(U1,50, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, 100, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U4, e4.getTimestamp(), e4.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e4)
        );
    }

    @DisplayName("User Scoped: single challenge")
    @Test
    public void testUserScopedChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U4,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, 1, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        Assertions.assertEquals(1, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2),
                new ChallengeOverSignal(rule.getId(), ChallengeOverSignal.CompletionType.ALL_WINNERS_FOUND)
        );
    }

    @DisplayName("User Scoped: single challenge")
    @Test
    public void testUserScopedMultipleChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U2,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        Assertions.assertEquals(ChallengeRule.ChallengeAwardMethod.REPEATABLE, rule.getAwardMethod());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 2, U2, e4.getTimestamp(), e4.getExternalId()),
                new ChallengeWinSignal(rule.getId(), 3, U2, e5.getTimestamp(), e5.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e4),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e5)
        );
    }

    @DisplayName("User Scoped: non repeatable challenge")
    @Test
    public void testUserScopedNRMultipleChallenge() {
        TEvent e1 = TEvent.createKeyValue(U1,100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(U2,105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U3,110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(U2,155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(U2,160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<ChallengeRule> ruleContext = createRule(AWARD, WIN_3, START, 200, signals);
        ChallengeRule rule = ruleContext.getRule();
        rule.setScope(ChallengeRule.ChallengeScope.USER);
        rule.setScopeId(U2);
        rule.setAwardMethod(ChallengeRule.ChallengeAwardMethod.NON_REPEATABLE);
        Assertions.assertEquals(WIN_3, rule.getWinnerCount());
        Assertions.assertEquals(U2, rule.getScopeId());
        Assertions.assertEquals(ChallengeRule.ChallengeScope.USER, rule.getScope());
        Assertions.assertEquals(ChallengeRule.ChallengeAwardMethod.NON_REPEATABLE, rule.getAwardMethod());
        ChallengeProcessor processor = new ChallengeProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        assertStrict(signals,
                new ChallengeWinSignal(rule.getId(), 1, U2, e2.getTimestamp(), e2.getExternalId()),
                new ChallengePointsAwardedSignal(rule.getId(), AWARD, e2)
        );
    }

    private BigDecimal asDecimal(long val) {
        return BigDecimal.valueOf(val).setScale(Constants.SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal award(Event event, int position) {
        return BigDecimal.valueOf((long)event.getFieldValue("value") - 50);
    }

    private boolean check(Event event, ChallengeRule rule) {
        return (long)event.getFieldValue("value") >= 50;
    }

    private RuleContext<ChallengeRule> createRule(BigDecimal points, int winners, long start, long end, Collection<Signal> signals) {
        ChallengeRule rule = new ChallengeRule("test.challenge.rule");
        rule.setForEvent(EVT_A);
        rule.setScope(ChallengeRule.ChallengeScope.GAME);
        rule.setAwardPoints(points);
        rule.setStartAt(start);
        rule.setExpireAt(end);
        rule.setCriteria(this::check);
        rule.setWinnerCount(winners);
        return new RuleContext<>(rule, signals::add);
    }

}