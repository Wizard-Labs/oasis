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

package io.github.oasis.game;

import io.github.oasis.game.process.windows.MonthlyEventTimeWindow;
import io.github.oasis.game.process.windows.OasisTimeWindowAssigner;
import io.github.oasis.game.process.windows.WeeklyEventTimeWindow;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Collection;

/**
 * @author iweerarathna
 */
public class TimeWindowTest {

    @Test
    public void testMonthlyWindow() {
        MonthlyEventTimeWindow windowAssigner = new MonthlyEventTimeWindow();

        TimeWindow window = collect("2018-01-01T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-01-01T00:00:00.00Z", "2018-02-01T00:00:00.00Z");

        window = collect("2018-01-31T23:59:59.999Z", windowAssigner);
        assertWindow(window, "2018-01-01T00:00:00.00Z", "2018-02-01T00:00:00.00Z");

        try {
            windowAssigner.assignWindows(new Object(), Long.MIN_VALUE, null);
            Assertions.fail("Timestamps with min value should not be success!");
        } catch (RuntimeException e) {
            // error is expected
        }

        window = collect("2018-02-01T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-02-01T00:00:00.00Z", "2018-03-01T00:00:00.00Z");

        window = collect("2018-02-28T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-02-01T00:00:00.00Z", "2018-03-01T00:00:00.00Z");

        window = collect("2018-02-28T23:59:59.999Z", windowAssigner);
        assertWindow(window, "2018-02-01T00:00:00.00Z", "2018-03-01T00:00:00.00Z");

        try {
            collect("2018-02-29T00:00:00.000Z", windowAssigner);
            Assertions.fail("Should fail on invalid dates!");
        } catch (DateTimeParseException e) {
            // error is expected
        }

        // long year
        window = collect("2016-02-29T23:59:59.999Z", windowAssigner);
        assertWindow(window, "2016-02-01T00:00:00.00Z", "2016-03-01T00:00:00.00Z");
    }

    @Test
    public void testWeeklyWindow() {
        OasisTimeWindowAssigner windowAssigner = new WeeklyEventTimeWindow();

        TimeWindow window = collect("2018-01-01T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-01-01T00:00:00.00Z", "2018-01-08T00:00:00.00Z");

        window = collect("2018-01-14T23:59:59.999Z", windowAssigner);
        assertWindow(window, "2018-01-08T00:00:00.00Z", "2018-01-15T00:00:00.00Z");

        try {
            windowAssigner.assignWindows(new Object(), Long.MIN_VALUE, null);
            Assertions.fail("Timestamps with min value should not be success!");
        } catch (RuntimeException e) {
            // error is expected
        }

        window = collect("2018-02-01T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-01-29T00:00:00.00Z", "2018-02-05T00:00:00.00Z");

        window = collect("2018-02-28T00:00:00.00Z", windowAssigner);
        assertWindow(window, "2018-02-26T00:00:00.00Z", "2018-03-05T00:00:00.00Z");

        window = collect("2018-03-04T23:59:59.999Z", windowAssigner);
        assertWindow(window, "2018-02-26T00:00:00.00Z", "2018-03-05T00:00:00.00Z");

        try {
            collect("2018-02-29T00:00:00.000Z", windowAssigner);
            Assertions.fail("Should fail on invalid dates!");
        } catch (DateTimeParseException e) {
            // error is expected
        }

        // long year
        window = collect("2016-02-29T00:59:59.999Z", windowAssigner);
        assertWindow(window, "2016-02-29T00:00:00.00Z", "2016-03-07T00:00:00.00Z");
    }

    private TimeWindow collect(String time, OasisTimeWindowAssigner windowAssigner) {
        Instant epoch = toEpoch(time);
        Collection<TimeWindow> timeWindows = windowAssigner.assignWindows(new Object(),
                epoch.toEpochMilli(), null);
        return timeWindows.iterator().next();
    }

    private void assertWindow(TimeWindow window, String start, String end) {
        assertTime(window.getStart(), start);
        assertTime(window.getEnd(), end);
    }

    private void assertTime(long actual, String expected) {
        Assertions.assertEquals(toEpoch(expected).toEpochMilli(), actual);
    }

    private Instant toEpoch(String isoTime) {
        return Instant.parse(isoTime);
    }

}