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

import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;
import io.github.oasis.model.events.RatingEvent;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RatingProcess extends KeyedProcessFunction<Long, Event, RatingEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(RatingProcess.class);

    private final ValueStateDescriptor<Integer> currStateDesc;
    private final ValueStateDescriptor<Long> prevStateChangedDesc;

    private Rating rating;
    private List<Rating.RatingState> orderedStates;

    private ValueState<Integer> currState;
    private ValueState<Long> prevChangedAt;

    public RatingProcess(Rating rating) {
        this.rating = rating;
        this.orderedStates = rating.getStates();

        currStateDesc = new ValueStateDescriptor<>(String.format("states-%d-curr-state", rating.getId()),
                        Integer.class);
        prevStateChangedDesc = new ValueStateDescriptor<>(
                String.format("states-%d-prev-state-changedat", rating.getId()),
                Long.class);
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<RatingEvent> out) throws Exception {
        initDefaultState();

        Map<String, Object> allFieldValues = event.getAllFieldValues();
        int previousState = currState.value();
        long prevTs = prevChangedAt.value();
        for (Rating.RatingState oaState : orderedStates) {
            if (Utils.evaluateCondition(oaState.getCondition(), allFieldValues)) {
                // this is the state
                Serializable stateValueExpression = rating.getStateValueExpression();
                String cv = String.valueOf(Utils.executeExpression(stateValueExpression, allFieldValues));

                if (oaState.getId() != previousState) {
                    // state change
                    prevTs = event.getTimestamp();
                }

                currState.update(oaState.getId());

                out.collect(new RatingEvent(event.getUser(),
                        rating,
                        event,
                        previousState,
                        oaState,
                        cv,
                        prevTs));
                return;
            }
        }

        // @TODO what to do when no state condition is resolved???
        LOG.warn("[O-STATE] ERROR - No valid state is found for event '{}'! (State: {}, {})",
                event.getExternalId(), rating.getId(), rating.getName());
    }

    private void initDefaultState() throws IOException {
        if (Objects.equals(currState.value(), currStateDesc.getDefaultValue())) {
            currState.update(rating.getDefaultState());
        }
        if (Objects.equals(prevChangedAt.value(), prevStateChangedDesc.getDefaultValue())) {
            prevChangedAt.update(1L);
        }
    }

    @Override
    public void open(Configuration parameters) {
        currState = getRuntimeContext().getState(currStateDesc);
        prevChangedAt = getRuntimeContext().getState(prevStateChangedDesc);
    }
}