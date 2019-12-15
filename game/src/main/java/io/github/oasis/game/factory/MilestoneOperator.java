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

package io.github.oasis.game.factory;

import io.github.oasis.game.Oasis;
import io.github.oasis.game.process.EventUserSelector;
import io.github.oasis.game.process.MilestoneCountProcess;
import io.github.oasis.game.process.MilestonePointSumProcess;
import io.github.oasis.game.process.MilestoneSumDoubleProcess;
import io.github.oasis.game.process.MilestoneSumProcess;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.AggregatorType;
import io.github.oasis.model.Event;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.events.PointEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class MilestoneOperator {

    public static MilestoneOpResponse createPipeline(KeyedStream<PointEvent, Long> userPointStream,
                                                            DataStream<Event> eventDataStream,
                                                            Milestone milestone,
                                                            OutputTag<MilestoneStateEvent> stateOutputTag,
                                                            Oasis oasis) {
        FilterFunction<Event> filterFunction;
        if (milestone.getCondition() != null) {
            filterFunction = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event event) throws Exception {
                    return Utils.eventEquals(event, milestone.getEvent())
                            && (milestone.getCondition() == null
                            || Utils.evaluateCondition(milestone.getCondition(), event.getAllFieldValues()));
                }
            };
        } else {
            filterFunction = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event event) {
                    return Utils.eventEquals(event, milestone.getEvent());
                }
            };
        }

        boolean usedPointStream = false;
        SingleOutputStreamOperator<MilestoneEvent> stream;
        if (milestone.getAggregator() == AggregatorType.COUNT) {
            stream = eventDataStream.filter(filterFunction)
                        .keyBy(new EventUserSelector<>())
                        .process(new MilestoneCountProcess(milestone, stateOutputTag));

        } else {
            if (milestone.isRealValues() || milestone.getFrom() != null) {
                List<Double> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().doubleValue())
                        .collect(Collectors.toList());
                if (milestone.isFromPoints()) {
                    stream = userPointStream.process(new MilestonePointSumProcess(levels, milestone, stateOutputTag));
                    usedPointStream = true;
                } else {
                    stream = eventDataStream.filter(filterFunction)
                            .keyBy(new EventUserSelector<>())
                            .process(new MilestoneSumDoubleProcess(levels, milestone.getAccumulatorExpr(),
                                    milestone, stateOutputTag));
                }
            } else {
                List<Long> levels = milestone.getLevels().stream()
                        .map(l -> l.getNumber().longValue())
                        .collect(Collectors.toList());
                stream = eventDataStream.filter(filterFunction)
                            .keyBy(new EventUserSelector<>())
                            .process(new MilestoneSumProcess(levels,
                                milestone.getAccumulatorExpr(), milestone, stateOutputTag));
            }
        }

        SingleOutputStreamOperator<MilestoneEvent> milestoneStream = stream
                .uid(String.format("oasis-%s-milestone-processor-%d", oasis.getId(), milestone.getId()));
        DataStream<MilestoneStateEvent> stateStream = milestoneStream.getSideOutput(stateOutputTag);
        return new MilestoneOpResponse(milestoneStream, stateStream)
                .setPointStreamUsed(usedPointStream);
    }

    public static class MilestoneOpResponse {
        private final DataStream<MilestoneEvent> milestoneEventStream;
        private final DataStream<MilestoneStateEvent> milestoneStateStream;
        private boolean pointStreamUsed = false;

        MilestoneOpResponse(DataStream<MilestoneEvent> milestoneEventStream,
                            DataStream<MilestoneStateEvent> milestoneStateStream) {
            this.milestoneEventStream = milestoneEventStream;
            this.milestoneStateStream = milestoneStateStream;
        }

        public DataStream<MilestoneStateEvent> getMilestoneStateStream() {
            return milestoneStateStream;
        }

        public DataStream<MilestoneEvent> getMilestoneEventStream() {
            return milestoneEventStream;
        }

        public boolean isPointStreamUsed() {
            return pointStreamUsed;
        }

        MilestoneOpResponse setPointStreamUsed(boolean pointStreamUsed) {
            this.pointStreamUsed = pointStreamUsed;
            return this;
        }
    }
}
