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

import io.github.oasis.model.handlers.IRatingsHandler;
import io.github.oasis.model.handlers.RatingNotification;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.io.Serializable;

public class OasisRatingSink implements SinkFunction<RatingNotification>, Serializable {

    private IRatingsHandler statesHandler;

    public OasisRatingSink(IRatingsHandler statesHandler) {
        this.statesHandler = statesHandler;
    }

    @Override
    public void invoke(RatingNotification value, Context context) {
        statesHandler.handleRatingChange(value);
    }

}
