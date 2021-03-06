/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.elements.matchers;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.TimeRangeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public final class TimeRangeMatcherFactory {

    public static TimeRangeMatcher create(List<AbstractDef.TimeRangeDef> defList) {
        if (Objects.isNull(defList) || defList.isEmpty()) {
            return null;
        }

        if (defList.size() == 1) {
            return create(defList.get(0));
        } else {
            List<TimeRangeMatcher> matchers = new ArrayList<>();
            for (AbstractDef.TimeRangeDef def : defList) {
                matchers.add(create(def));
            }
            return new AnyOfTimeRangeMatcher(matchers);
        }
    }

    private static TimeRangeMatcher create(AbstractDef.TimeRangeDef def) {
        if (AbstractDef.TIME_RANGE_TYPE_SEASONAL.equals(def.getType())) {
            return AnnualDateRangeMatcher.create((String) def.getFrom(), (String) def.getTo());
        } else if (AbstractDef.TIME_RANGE_TYPE_TIME.equals(def.getType())) {
            return DailyTimeRangeMatcher.create((String) def.getFrom(), (String) def.getTo());
        } else if (AbstractDef.TIME_RANGE_TYPE_WEEKLY.equals(def.getType())) {
            return WeeklyTimeMatcher.create((String) def.getWhen());
        } else if (AbstractDef.TIME_RANGE_TYPE_CUSTOM.equals(def.getType())) {
            return ScriptedTimeMatcher.create((String) def.getExpression());
        } else {
            return AbsoluteTimeMatcher.create((long) def.getFrom(), (long) def.getTo());
        }
    }

}
