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

package io.github.oasis.game.factory.badges;

import io.github.oasis.game.process.EventUserSelector;
import io.github.oasis.game.process.triggers.ConditionalTrigger;
import io.github.oasis.game.process.triggers.StreakLevelTrigger;
import io.github.oasis.game.process.triggers.StreakTrigger;
import io.github.oasis.game.process.windows.OasisTimeWindow;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.events.MilestoneEvent;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.rules.BadgeFromEvents;
import io.github.oasis.model.rules.BadgeFromMilestone;
import io.github.oasis.model.rules.BadgeFromPoints;
import io.github.oasis.model.rules.BadgeRule;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.datastream.WindowedStream;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousEventTimeTrigger;
import org.apache.flink.streaming.api.windowing.triggers.Trigger;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.streaming.api.windowing.windows.Window;

import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public class BadgeOperator {

    public static SingleOutputStreamOperator<BadgeEvent> createBadgeFromPoints(KeyedStream<PointEvent, Long> pointStreamByUser,
                                                               KeyedStream<MilestoneEvent, Long> milestoneStream,
                                                               DataStream<Event> rawStream,
                                                               BadgeRule badgeRule) {
        if (badgeRule instanceof BadgeFromPoints) {
            return createBadgeFromPoints(pointStreamByUser, (BadgeFromPoints) badgeRule);
        } else if (badgeRule instanceof BadgeFromEvents) {
            return createBadgeFromEvents(rawStream, (BadgeFromEvents) badgeRule);
        }  else if (badgeRule instanceof BadgeFromMilestone) {
            if (milestoneStream == null) {
                throw new RuntimeException("You have NOT defined any milestones to create badges from milestone events!");
            }
            return createBadgeFromMilestones(milestoneStream, (BadgeFromMilestone) badgeRule);
        } else {
            throw new RuntimeException("Unknown badge type to process!");
        }
    }

    private static SingleOutputStreamOperator<BadgeEvent> createBadgeFromMilestones(KeyedStream<MilestoneEvent, Long> milestoneStream,
                                                                                    BadgeFromMilestone badgeRule) {
        FilterFunction<MilestoneEvent> triggerCondition = new MilestoneBadgeFilter(badgeRule);

        return milestoneStream
                .window(GlobalWindows.create())
                .trigger(new ConditionalTrigger<>(triggerCondition))
                .process(new MilestoneBadgeHandler<>(badgeRule));
    }

    private static SingleOutputStreamOperator createBadgeFromEvents(DataStream<Event> rawStream,
                                                                    BadgeFromEvents badgeRule) {

        FilterFunction<Event> eventFilterFunction = new FilterFunction<Event>() {
            @Override
            public boolean filter(Event value) {
                return Utils.eventEquals(value, badgeRule.getEventType());
            }
        };
        FilterFunction<Event> condFilterFunction = new FilterFunction<Event>() {
            @Override
            public boolean filter(Event value) throws Exception {
                return badgeRule.getCondition() == null
                        || Utils.evaluateCondition(badgeRule.getCondition(), value.getAllFieldValues());
            }
        };

        WindowedStream<Event, Long, ? extends Window> window;

        if (badgeRule.getContinuous() != null) {
            //
            // Badge rules like: Event happens for N continuous days
            //
            FilterFunction<Event> combinedFilter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return Utils.eventEquals(value, badgeRule.getEventType())
                            && Utils.evaluateCondition(badgeRule.getCondition(), value.getAllFieldValues());
                }
            };

            KeyedStream<Event, Long> keyedUserStream = rawStream.filter(combinedFilter)
                    .keyBy(new EventUserSelector<>());

            // @TODO histogram like counting support for weeks and months
            WindowedStream<Event, Long, TimeWindow> tmpWindowed = timeHistogramStream(badgeRule.getDuration(), keyedUserStream)
                    .trigger(ContinuousEventTimeTrigger.of(Time.days(1)));

            if (badgeRule.getContinuousAggregator() != null) {
                return tmpWindowed.process(new HistogramSumProcessor<>(badgeRule, new TimeConverterFunction()));
            } else {
                return tmpWindowed.process(new HistogramCountProcessor<>(badgeRule, new TimeConverterFunction()));
            }

        } else {
            KeyedStream<Event, Long> keyedUserStream = rawStream.filter(eventFilterFunction)
                    .keyBy(new EventUserSelector<>());

            if (badgeRule.hasSubStreakBadges()) {
                if (badgeRule.getDuration() != null) {
                    window = timeWindowedStream(badgeRule.getDuration(), keyedUserStream)
                            .trigger(new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(),
                                    condFilterFunction, true));
                } else {
                    window = keyedUserStream.window(GlobalWindows.create())
                            .trigger(new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(), condFilterFunction));
                }

            } else {
                if (badgeRule.getCondition() == null) {
                    if (badgeRule.getDuration() == null) {
                        window = keyedUserStream.window(GlobalWindows.create())
                                .trigger(new StreakTrigger<>(badgeRule.getStreak(), condFilterFunction));
                    } else {
                        window = timeWindowedStream(badgeRule.getDuration(), keyedUserStream)
                                .trigger(new StreakTrigger<>(badgeRule.getStreak(), condFilterFunction, true));
                    }
                } else {
                    if (badgeRule.getDuration() == null) {
                        return keyedUserStream.countWindow(1)
                                .process(new ConditionBadgeHandler<>(badgeRule));
                    } else {
                        return timeWindowedStream(badgeRule.getDuration(), keyedUserStream)
                                .process(new ConditionBadgeHandler<>(badgeRule));
                    }
                }
            }

            return window.process(new StreakBadgeHandlerEvents<>(badgeRule));
        }
    }

    private static SingleOutputStreamOperator<BadgeEvent> createBadgeFromPoints(KeyedStream<PointEvent, Long> pointStreamByUser,
                                                                                BadgeFromPoints badgeRule) {
        Trigger<PointEvent, Window> trigger;
        FilterFunction<PointEvent> filterFunction = new FilterFunction<PointEvent>() {
            @Override
            public boolean filter(PointEvent value) {
                return value.containsScoring(badgeRule.getPointsId());
            }
        };

        boolean fireEventTime = badgeRule.getDuration() != null;
        if (badgeRule.getStreak() > 0) {
            if (badgeRule.hasSubStreakBadges()) {
                trigger = new StreakLevelTrigger<>(badgeRule.getStreakBreakPoints(), filterFunction, fireEventTime);
            } else {
                trigger = new StreakTrigger<>(badgeRule.getStreak(), filterFunction, fireEventTime);
            }

            WindowedStream<PointEvent, Long, ? extends Window> windowedStream;
            if (fireEventTime) {
                windowedStream = timeWindowedStream(badgeRule, pointStreamByUser).trigger(trigger);
            } else {
                windowedStream = pointStreamByUser.window(GlobalWindows.create()).trigger(trigger);
            }

            return windowedStream.process(new StreakBadgeHandler<>(badgeRule));

        } else if (badgeRule.getAggregator() != null) {
            return timeWindowedStream(badgeRule, pointStreamByUser)
                    .aggregate(new SumAggregator(filterFunction, badgeRule))
                    .filter(b -> b.getUser() > 0);

        } else {
            throw new RuntimeException("Unknown badge from points definition received!");
        }
    }

    private static WindowedStream<Event, Long, TimeWindow> timeHistogramStream(String durationStr, KeyedStream<Event, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else if ("daily".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.DAILY());
        } else {
            Time duration = Time.of(1, TimeUnit.DAYS);
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

    private static WindowedStream<Event, Long, TimeWindow> timeWindowedStream(String durationStr, KeyedStream<Event, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else if ("daily".equalsIgnoreCase(durationStr)) {
            return inputStream.window(OasisTimeWindow.DAILY());
        } else {
            Time duration = Utils.fromStr(durationStr);
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

    private static WindowedStream<PointEvent, Long, TimeWindow> timeWindowedStream(BadgeFromPoints rule, KeyedStream<PointEvent, Long> inputStream) {
        if ("weekly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.WEEKLY());
        } else if ("monthly".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.MONTHLY());
        } else if ("daily".equalsIgnoreCase(rule.getDuration())) {
            return inputStream.window(OasisTimeWindow.DAILY());
        } else {
            Time duration = Utils.fromStr(rule.getDuration());
            return inputStream.timeWindow(duration, Time.of(1, duration.getUnit()));
        }
    }

}
