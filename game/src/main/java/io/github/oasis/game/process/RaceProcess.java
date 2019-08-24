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
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Collections;

public class RaceProcess extends ProcessFunction<Event, RaceEvent> {

    private final PointRule racePointRule;
    private final OutputTag<PointNotification> pointOutput;

    public RaceProcess(PointRule racePointRule, OutputTag<PointNotification> pointOutput) {
        this.racePointRule = racePointRule;
        this.pointOutput = pointOutput;
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<RaceEvent> out) throws Exception {
        RaceEvent raceEvent = new RaceEvent(event);
        out.collect(raceEvent);

        double points = Utils.asDouble(event.getFieldValue(RaceEvent.KEY_POINTS));
        PointNotification pointNotification = new PointNotification(event.getUser(),
                Collections.singletonList(event),
                racePointRule,
                points);
        ctx.output(pointOutput, pointNotification);
    }
}
