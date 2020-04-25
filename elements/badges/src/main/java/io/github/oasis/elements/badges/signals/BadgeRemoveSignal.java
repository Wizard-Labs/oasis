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

package io.github.oasis.elements.badges.signals;

import io.github.oasis.core.EventScope;
import lombok.ToString;

/**
 * @author Isuru Weerarathna
 */
@ToString
public class BadgeRemoveSignal extends BadgeSignal {
    public BadgeRemoveSignal(String ruleId, EventScope event, int attribute, long st, long et, String sid, String eid) {
        super(ruleId, event, st, attribute, st, et, sid, eid);
    }

    public BadgeRemoveSignal(String ruleId, EventScope eventScope, int attribute, long st) {
        super(ruleId, eventScope, st, attribute, st, -1, null, null);
    }

    public BadgeRemoveSignal(BadgeSignal prevBadge) {
        super(prevBadge.getRuleId(),
                prevBadge.getEventScope(),
                prevBadge.getStartTime(),
                prevBadge.getAttribute(),
                prevBadge.getStartTime(),
                prevBadge.getEndTime(),
                prevBadge.getStartId(),
                prevBadge.getEndId());
    }
}
