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

package io.github.oasis.elements.milestones.stats.to;

import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;

import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class GameMilestoneRequest {

    private Integer gameId;

    // summary related attributes
    private Set<String> milestoneIds;
    private Set<Integer> teamIds;
    private Set<Integer> attributeIds;

    // range related attributes
    private String milestoneId;
    private Set<Long> userIds;

    public boolean hasSummaryDetails() {
        return Utils.isNotEmpty(milestoneIds) && Utils.isNotEmpty(attributeIds);
    }

    public boolean isMultiUserRequest() {
        return Texts.isNotEmpty(milestoneId) && Utils.isNotEmpty(userIds);
    }

    public Set<Integer> getTeamIds() {
        return teamIds;
    }

    public Set<Integer> getAttributeIds() {
        return attributeIds;
    }

    public Set<String> getMilestoneIds() {
        return milestoneIds;
    }

    public void setMilestoneIds(Set<String> milestoneIds) {
        this.milestoneIds = milestoneIds;
    }

    public void setTeamIds(Set<Integer> teamIds) {
        this.teamIds = teamIds;
    }

    public void setAttributeIds(Set<Integer> attributeIds) {
        this.attributeIds = attributeIds;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }

    public String getMilestoneId() {
        return milestoneId;
    }

    public void setMilestoneId(String milestoneId) {
        this.milestoneId = milestoneId;
    }

    public Set<Long> getUserIds() {
        return userIds;
    }

    public void setUserIds(Set<Long> userIds) {
        this.userIds = userIds;
    }
}
