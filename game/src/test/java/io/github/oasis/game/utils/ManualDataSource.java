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

package io.github.oasis.game.utils;

import io.github.oasis.model.Event;
import io.github.oasis.model.events.JsonEvent;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author Isuru Weerarathna
 */
public class ManualDataSource implements SourceFunction<Event> {

    private static final CloseEvent CLOSE_EVENT = new CloseEvent();

    private Queue<Event> queue = new ArrayBlockingQueue<>(10);

    public ManualDataSource emit(Event event) {
        queue.offer(event);
        return this;
    }

    @Override
    public void run(SourceContext<Event> ctx) {
        while (queue.peek() != null) {
            Event event = queue.poll();
            if (event == CLOSE_EVENT) break;

            ctx.collect(event);
        }
    }

    @Override
    public void cancel() {
        queue.offer(CLOSE_EVENT);
    }

    private static class CloseEvent extends JsonEvent {
    }
}
