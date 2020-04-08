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

package io.github.oasis.engine.model;

import io.github.oasis.engine.utils.Timestamps;

import java.time.ZonedDateTime;
import java.time.temporal.IsoFields;

/**
 * @author Isuru Weerarathna
 */
public class TimeContext {

    private int year;
    private String month;
    private String day;
    private String week;
    private String quarter;

    public TimeContext(long ts, int offset) {
        ZonedDateTime userTime = Timestamps.getUserSpecificTime(ts, offset);
        year = userTime.getYear();
        month = String.format("%d%02d", year, userTime.getMonth().getValue());
        day = String.format("%s%02d", month, userTime.getDayOfMonth());
        week = String.format("%dW%02d", year, userTime.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR));
        quarter = String.format("%dQ%02d", year, userTime.get(IsoFields.QUARTER_OF_YEAR));
    }

    public int getYear() {
        return year;
    }

    public String getMonth() {
        return month;
    }

    public String getDay() {
        return day;
    }

    public String getWeek() {
        return week;
    }

    public String getQuarter() {
        return quarter;
    }
}
