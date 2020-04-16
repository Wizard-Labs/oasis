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

package io.github.oasis.engine.runners;

import io.github.oasis.engine.actors.cmds.RuleAddedMessage;
import io.github.oasis.engine.elements.points.PointRule;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.TEvent;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static io.github.oasis.engine.runners.RedisAssert.assertSorted;
import static io.github.oasis.engine.runners.RedisAssert.ofSortedEntries;

/**
 * @author Isuru Weerarathna
 */
public class EnginePointsTest extends OasisEngineTest {

    private static final int U1 = 1;
    private static final int U2 = 2;
    private static final int T1 = 100;
    private static final int T2 = 200;

    @Test
    public void testEnginePoints() {
        TEvent e1 = TEvent.createKeyValue(U1, TS("2020-03-24 07:15"), EVT_A, 15);
        TEvent e2 = TEvent.createKeyValue(U2, TS("2020-04-02 08:20"), EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(U1, TS("2020-04-03 08:45"), EVT_A, 74);
        TEvent e4 = TEvent.createKeyValue(U2, TS("2019-12-26 11:45"), EVT_A, 98);
        TEvent e5 = TEvent.createKeyValue(U1, TS("2020-03-25 08:45"), EVT_A, 61);

        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long)event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        engine.submit(e1, e2, e3, e4, e5);
        awaitTerminated();

        // total = 33 + 24 + 48 = 105
        String rid = rule.getId();
        long tid = e1.getTeam();
        RedisAssert.assertMap(dbPool,
                ID.getGameUserPointsSummary(TEvent.GAME_ID, U1),
                RedisAssert.ofEntries("all", "35",
                        "source:" + e1.getSource(), "35",
                        "all:Y2020", "35",
                        "all:Q202002", "24",
                        "all:Q202001", "11",
                        "all:M202003", "11",
                        "all:M202004", "24",
                        "all:W202014", "24",
                        "all:W202013", "11",
                        "all:D20200325", "11",
                        "all:D20200403", "24",
                        "rule:" + rid, "35",
                        "rule:"+rid+":Y2020", "35",
                        "rule:"+rid+":Q202002", "24",
                        "rule:"+rid+":Q202001", "11",
                        "rule:"+rid+":M202003", "11",
                        "rule:"+rid+":M202004", "24",
                        "rule:"+rid+":W202014", "24",
                        "rule:"+rid+":W202013", "11",
                        "rule:"+rid+":D20200325", "11",
                        "rule:"+rid+":D20200403", "24",
                        "team:"+tid, "35",
                        "team:"+tid+":Y2020", "35",
                        "team:"+tid+":Q202002", "24",
                        "team:"+tid+":Q202001", "11",
                        "team:"+tid+":M202003", "11",
                        "team:"+tid+":M202004", "24",
                        "team:"+tid+":W202014", "24",
                        "team:"+tid+":W202013", "11",
                        "team:"+tid+":D20200325", "11",
                        "team:"+tid+":D20200403", "24"
                ));
        RedisAssert.assertMap(dbPool,
                ID.getGameUserPointsSummary(TEvent.GAME_ID, U2),
                RedisAssert.ofEntries("all", "81",
                        "source:" + e1.getSource(), "81",
                        "all:Y2019", "48",
                        "all:Y2020", "33",
                        "all:Q202002", "33",
                        "all:Q201904", "48",
                        "all:M201912", "48",
                        "all:M202004", "33",
                        "all:W202014", "33",
                        "all:W201952", "48",
                        "all:D20191226", "48",
                        "all:D20200402", "33",
                        "rule:" + rid, "81",
                        "rule:"+rid+":Y2020", "33",
                        "rule:"+rid+":Y2019", "48",
                        "rule:"+rid+":Q202002", "33",
                        "rule:"+rid+":Q201904", "48",
                        "rule:"+rid+":M201912", "48",
                        "rule:"+rid+":M202004", "33",
                        "rule:"+rid+":W202014", "33",
                        "rule:"+rid+":W201952", "48",
                        "rule:"+rid+":D20191226", "48",
                        "rule:"+rid+":D20200402", "33",
                        "team:"+tid, "81",
                        "team:"+tid+":Y2020", "33",
                        "team:"+tid+":Y2019", "48",
                        "team:"+tid+":Q202002", "33",
                        "team:"+tid+":Q201904", "48",
                        "team:"+tid+":M201912", "48",
                        "team:"+tid+":M202004", "33",
                        "team:"+tid+":W202014", "33",
                        "team:"+tid+":W201952", "48",
                        "team:"+tid+":D20191226", "48",
                        "team:"+tid+":D20200402", "33"
                ));

        int gameId = TEvent.GAME_ID;
        assertSorted(dbPool,
                ID.getGameLeaderboard(gameId, "all", ""),
                ofSortedEntries(U1, 35, U2, 81));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "y", "Y2020"), ofSortedEntries(U1, 35, U2, 33));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "y", "Y2019"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "m", "M201912"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "m", "M202003"), ofSortedEntries(U1, 11));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "m", "M202004"), ofSortedEntries(U1, 24, U2, 33));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "q", "Q201904"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "q", "Q202001"), ofSortedEntries(U1, 11));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "q", "Q202002"), ofSortedEntries(U1, 24, U2, 33));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "w", "W201952"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "w", "W202013"), ofSortedEntries(U1, 11));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "w", "W202014"), ofSortedEntries(U1, 24, U2, 33));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20191226"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20200325"), ofSortedEntries(U1, 11));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20200402"), ofSortedEntries(U2, 33));
        assertSorted(dbPool, ID.getGameLeaderboard(gameId, "d", "D20200403"), ofSortedEntries(U1, 24));

    }

    @Test
    public void testEnginePointsTeamLeaderboards() {
        TEvent e1 = TEvent.createWithTeam(U1, T2, TS("2019-12-27 07:15"), EVT_A, 55);
        TEvent e2 = TEvent.createWithTeam(U2, T2, TS("2019-12-26 11:45"), EVT_A, 98);
        TEvent e3 = TEvent.createWithTeam(U1, T1, TS("2019-12-26 07:15"), EVT_A, 56);
        TEvent e4 = TEvent.createWithTeam(U2, T1, TS("2019-12-27 11:45"), EVT_A, 87);
        TEvent e5 = TEvent.createWithTeam(U1, T2, TS("2020-04-02 07:15"), EVT_A, 61);
        TEvent e6 = TEvent.createWithTeam(U2, T2, TS("2020-04-03 11:45"), EVT_A, 59);
        TEvent e7 = TEvent.createWithTeam(U1, T1, TS("2020-04-02 07:15"), EVT_A, 83);
        TEvent e8 = TEvent.createWithTeam(U2, T1, TS("2020-04-03 11:45"), EVT_A, 78);

        PointRule rule = new PointRule("test.point.rule");
        rule.setForEvent(EVT_A);
        rule.setAmountExpression((event, rule1) -> BigDecimal.valueOf((long) event.getFieldValue("value") - 50));
        rule.setCriteria((event, rule1, ctx) -> (long) event.getFieldValue("value") >= 50);

        supervisor.tell(RuleAddedMessage.create(TEvent.GAME_ID, rule), supervisor);
        engine.submit(e1, e2, e3, e4, e5, e6, e7, e8);
        awaitTerminated();

        int gameId = TEvent.GAME_ID;
        // team T1 leaderboards
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "all", ""), ofSortedEntries(U1, 39, U2, 65));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "y", "Y2019"), ofSortedEntries(U1, 6, U2, 37));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "y", "Y2020"), ofSortedEntries(U1, 33, U2, 28));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "m", "M201912"), ofSortedEntries(U1, 6, U2, 37));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "m", "M202004"), ofSortedEntries(U1, 33, U2, 28));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "q", "Q201904"), ofSortedEntries(U1, 6, U2, 37));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "q", "Q202002"), ofSortedEntries(U2, 28, U1, 33));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "w", "W201952"), ofSortedEntries(U1, 6, U2, 37));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "w", "W202014"), ofSortedEntries(U2, 28, U1, 33));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "d", "D20191226"), ofSortedEntries(U1, 6));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "d", "D20191227"), ofSortedEntries(U2, 37));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "d", "D20200402"), ofSortedEntries(U1, 33));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T1, "d", "D20200403"), ofSortedEntries(U2, 28));

        // team T2 leaderboards
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "all", ""), ofSortedEntries(U1, 16, U2, 57));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "y", "Y2019"), ofSortedEntries(U1, 5, U2, 48));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "y", "Y2020"), ofSortedEntries(U1, 11, U2, 9));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "m", "M201912"), ofSortedEntries(U1, 5, U2, 48));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "m", "M202004"), ofSortedEntries(U1, 11, U2, 9));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "q", "Q201904"), ofSortedEntries(U1, 5, U2, 48));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "q", "Q202002"), ofSortedEntries(U2, 9, U1, 11));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "w", "W201952"), ofSortedEntries(U1, 5, U2, 48));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "w", "W202014"), ofSortedEntries(U2, 9, U1, 11));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "d", "D20191226"), ofSortedEntries(U2, 48));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "d", "D20191227"), ofSortedEntries(U1, 5));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "d", "D20200402"), ofSortedEntries(U1, 11));
        assertSorted(dbPool, ID.getGameTeamLeaderboard(gameId, T2, "d", "D20200403"), ofSortedEntries(U2, 9));

    }


}
