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

package io.github.oasis.game.process;

import io.github.oasis.model.Milestone;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.events.PointEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeutils.base.DoubleSerializer;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * @author iweerarathna
 */
public class MilestonePointSumProcess extends KeyedProcessFunction<Long, PointEvent, MilestoneEvent> {

    private static final double DEFAULT_POINT_VALUE = 0.0;

    private final ValueStateDescriptor<Integer> currLevelStateDesc;
    private final ValueStateDescriptor<Double> stateDesc;
    private final ValueStateDescriptor<Double> accNegSumDesc;

    private List<Double> levels;
    private Milestone milestone;
    private OutputTag<MilestoneStateEvent> outputTag;

    private boolean atEnd = false;
    private Double nextLevelValue = null;

    private ValueState<Double> accSum;
    private ValueState<Double> accNegSum;
    private ValueState<Integer> currentLevel;

    public MilestonePointSumProcess(List<Double> levels, Milestone milestone,
                                    OutputTag<MilestoneStateEvent> outputTag) {
        this.levels = levels;
        this.milestone = milestone;
        this.outputTag = outputTag;

        currLevelStateDesc =
                new ValueStateDescriptor<>(String.format("milestone-psd-%d-curr-level", milestone.getId()),
                        Integer.class);
        stateDesc =
                new ValueStateDescriptor<>(String.format("milestone-psd-%d-sum", milestone.getId()),
                        DoubleSerializer.INSTANCE);
        accNegSumDesc = new ValueStateDescriptor<>(
                String.format("milestone-psd-%d-negsum", milestone.getId()),
                DoubleSerializer.INSTANCE);
    }

    @Override
    public void processElement(PointEvent value, Context ctx, Collector<MilestoneEvent> out) throws Exception {
        double accumulatedSum;
        if (milestone.hasPointReferenceIds()) {
            accumulatedSum = milestone.getPointIds().stream()
                    .filter(value::containsPoint)
                    .mapToDouble(pid -> value.getPointsForRefId(pid, DEFAULT_POINT_VALUE))
                    .sum();
        } else {
            accumulatedSum = value.getTotalScore();
        }

        initDefaultState();

        if (milestone.isOnlyPositive() && accumulatedSum < 0) {
            accNegSum.update(accNegSum.value() + accumulatedSum);
            ctx.output(outputTag, new MilestoneStateEvent(value.getUser(), value.getGameId(), milestone, accNegSum.value()));
            return;
        }

        Integer currLevel = currentLevel.value();
        double currLevelMargin;
        if (currLevel < levels.size()) {
            double margin = levels.get(currLevel);
            currLevelMargin = margin;
            double currSum = accSum.value();
            if (currSum < margin && margin <= currSum + accumulatedSum) {
                // level changed
                int nextLevel = currLevel + 1;
                currentLevel.update(nextLevel);
                out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));

                double total = currSum + accumulatedSum;
                if (nextLevel < levels.size()) {
                    margin = levels.get(nextLevel);
                    currLevelMargin = margin;

                    // check for subsequent levels
                    while (nextLevel < levels.size() && margin < total) {
                        margin = levels.get(nextLevel);
                        if (margin < total) {
                            nextLevel = nextLevel + 1;
                            currentLevel.update(nextLevel);
                            currLevelMargin = margin;
                            out.collect(new MilestoneEvent(value.getUser(), milestone, nextLevel, value));
                        }
                    }
                }
                accSum.update(total);
                nextLevelValue = levels.size() > nextLevel ? levels.get(nextLevel) : null;
                atEnd = levels.size() >= nextLevel;

            } else {
                accSum.update(currSum + accumulatedSum);
            }
        } else {
            currLevelMargin = levels.get(levels.size() - 1);
        }

        if (!atEnd && nextLevelValue == null) {
            if (levels.size() > currentLevel.value()) {
                nextLevelValue = levels.get(Integer.parseInt(currentLevel.value().toString()));
            } else {
                nextLevelValue = null;
                atEnd = true;
            }
        }

        // update sum in db
        if (!atEnd) {
            ctx.output(outputTag, new MilestoneStateEvent(value.getUser(),
                    value.getGameId(),
                    milestone,
                    accSum.value(),
                    nextLevelValue,
                    currLevelMargin));
        }
    }

    private void initDefaultState() throws IOException {
        if (Objects.equals(accNegSum.value(), accNegSumDesc.getDefaultValue())) {
            accNegSum.update(0.0);
        }
        if (Objects.equals(currentLevel.value(), currLevelStateDesc.getDefaultValue())) {
            currentLevel.update(0);
        }
        if (Objects.equals(accSum.value(), stateDesc.getDefaultValue())) {
            accSum.update(0.0);
        }
    }

    @Override
    public void open(Configuration parameters) {
        accNegSum = getRuntimeContext().getState(accNegSumDesc);
        currentLevel = getRuntimeContext().getState(currLevelStateDesc);
        accSum = getRuntimeContext().getState(stateDesc);
    }
}
