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

package io.github.oasis.engine.elements.milestones;

import io.github.oasis.engine.elements.AbstractProcessor;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.model.ExecutionContext;
import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

import static io.github.oasis.engine.elements.milestones.MilestoneRule.SKIP_NEGATIVE_VALUES;
import static io.github.oasis.engine.elements.milestones.MilestoneRule.TRACK_PENALTIES;
import static io.github.oasis.engine.utils.Numbers.isNegative;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneProcessor extends AbstractProcessor<MilestoneRule, MilestoneSignal> {

    public MilestoneProcessor(Db dbPool, RuleContext<MilestoneRule> ruleCtx) {
        super(dbPool, ruleCtx);
    }

    @Override
    protected void beforeEmit(MilestoneSignal signal, Event event, MilestoneRule rule, ExecutionContext context, DbContext db) {
        // do nothing...
    }

    @Override
    public List<MilestoneSignal> process(Event event, MilestoneRule rule, ExecutionContext context, DbContext db) {
        if (Objects.isNull(rule.getValueExtractor())) {
            return null;
        }
        BigDecimal delta = rule.getValueExtractor().resolve(event, rule, context).setScale(Constants.SCALE, RoundingMode.HALF_UP);
        if (rule.hasFlag(SKIP_NEGATIVE_VALUES) && isNegative(delta)) {
            return null;
        }
        Mapped userMilestonesMap = db.MAP(ID.getGameUserMilestonesSummary(event.getGameId(), event.getUser()));
        userMilestonesMap.incrementByDecimal(rule.getId(), delta);
        userMilestonesMap.setValues(String.format("%s:lastupdated", rule.getId()),
                String.valueOf(event.getTimestamp()),
                String.format("%s:lastevent", rule.getId()),
                event.getExternalId());

        String milestoneKey = ID.getGameMilestoneKey(event.getGameId(), rule.getId());
        Mapped gameMilestoneMap = db.MAP(milestoneKey);
        BigDecimal updatedValue = gameMilestoneMap.incrementByDecimal(ID.getUserKeyUnderGameMilestone(event.getUser()), delta);
        if (rule.hasFlag(TRACK_PENALTIES) && isNegative(delta)) {
            userMilestonesMap.incrementByDecimal(String.format("%s:penalties", rule.getId()), delta);
        }

        MilestoneRule.Level currentLevel = rule.getLevelFor(updatedValue).orElse(null);
        BigDecimal prevValue = updatedValue.subtract(delta);
        MilestoneRule.Level prevLevel = rule.getLevelFor(prevValue).orElse(null);
        if (hasLevelIncreased(prevLevel, currentLevel)) {
            return List.of(new MilestoneSignal(rule.getId(),
                    defaultLevel(prevLevel),
                    defaultLevel(currentLevel),
                    updatedValue,
                    event));
        } else if (!rule.hasFlag(SKIP_NEGATIVE_VALUES) && hasLevelDecreased(prevLevel, currentLevel)) {
            return List.of(new MilestoneSignal(rule.getId(),
                    defaultLevel(prevLevel),
                    defaultLevel(currentLevel),
                    updatedValue,
                    event));
        }
        return null;
    }

    private int defaultLevel(MilestoneRule.Level level) {
        return level == null ? 0 : level.getLevel();
    }

    private boolean hasLevelDecreased(MilestoneRule.Level prevLevel, MilestoneRule.Level currentLevel) {
        if (prevLevel != null) {
            return currentLevel == null || currentLevel.getLevel() < prevLevel.getLevel();
        }
        return false;
    }

    private boolean hasLevelIncreased(MilestoneRule.Level prevLevel, MilestoneRule.Level currentLevel) {
        if (currentLevel != null) {
            return prevLevel == null || currentLevel.getLevel() > prevLevel.getLevel();
        }
        return false;
    }
}
