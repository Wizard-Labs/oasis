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

package io.github.oasis.game.process.sinks;

import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.handlers.IMilestoneHandler;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisMilestoneStateSink implements SinkFunction<MilestoneStateEvent> {

    private IMilestoneHandler milestoneHandler;

    public OasisMilestoneStateSink(IMilestoneHandler milestoneHandler) {
        this.milestoneHandler = milestoneHandler;
    }

    @Override
    public void invoke(MilestoneStateEvent value, Context context) throws Exception {
        if (value.isDouble()) {
            milestoneHandler.addMilestoneCurrState(value.getUserId(),
                    value.getMilestone(),
                    value.getValue(),
                    value.getNextValue());
        } else {
            milestoneHandler.addMilestoneCurrState(value.getUserId(),
                    value.getMilestone(),
                    value.getValueInt(),
                    value.getNextValueInt());
        }
    }
}
