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

package io.github.oasis.game.states;

import io.github.oasis.model.Milestone;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
public class BaseMilestoneState implements Serializable {

    protected long milestoneId;

    protected int currentLevel;
    protected int nextLevel;

    public boolean isAllLevelsReached() {
        return nextLevel < 0;
    }

    public boolean hasLevelChanged(Milestone.Level derivedLevel) {
        return derivedLevel.getLevel() != currentLevel;
    }

    void updateLevelTo(Milestone.Level derivedLevelRef, Milestone.Level nextLevelRef) {
        currentLevel = derivedLevelRef.getLevel();
        nextLevel = nextLevelRef == null ? -1 : nextLevelRef.getLevel();
    }
}
