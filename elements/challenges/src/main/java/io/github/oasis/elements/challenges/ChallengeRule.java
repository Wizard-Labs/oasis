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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventBiValueResolver;
import io.github.oasis.core.elements.EventExecutionFilter;

import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeRule extends AbstractRule {

    public static final String REPEATABLE_WINNERS = "REPEATABLE_WINNERS";
    public static final String OUT_OF_ORDER_WINNERS = "OUT_OF_ORDER_WINNERS";

    private long expireAt;
    private long startAt;
    private int winnerCount;

    private String pointId;
    private BigDecimal awardPoints = BigDecimal.ZERO;
    private EventBiValueResolver<Integer, ChallengeRule> customAwardPoints;

    private EventExecutionFilter criteria;

    private ChallengeScope scope = ChallengeScope.GAME;
    private long scopeId;

    public ChallengeRule(String id) {
        super(id);
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public long getStartAt() {
        return startAt;
    }

    public void setStartAt(long startAt) {
        this.startAt = startAt;
    }

    public int getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(int winnerCount) {
        this.winnerCount = winnerCount;
    }

    public BigDecimal getAwardPoints() {
        return awardPoints;
    }

    public void setAwardPoints(BigDecimal awardPoints) {
        this.awardPoints = awardPoints;
    }

    public EventBiValueResolver<Integer, ChallengeRule> getCustomAwardPoints() {
        return customAwardPoints;
    }

    public void setCustomAwardPoints(EventBiValueResolver<Integer, ChallengeRule> customAwardPoints) {
        this.customAwardPoints = customAwardPoints;
    }

    public EventExecutionFilter getCriteria() {
        return criteria;
    }

    public void setCriteria(EventExecutionFilter criteria) {
        this.criteria = criteria;
    }

    public ChallengeScope getScope() {
        return scope;
    }

    public void setScope(ChallengeScope scope) {
        this.scope = scope;
    }

    public long getScopeId() {
        return scopeId;
    }

    public void setScopeId(long scopeId) {
        this.scopeId = scopeId;
    }

    public enum ChallengeScope {
        USER,
        TEAM,
        GAME
    }
}
