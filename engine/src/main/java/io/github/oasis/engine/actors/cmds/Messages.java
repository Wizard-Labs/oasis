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

package io.github.oasis.engine.actors.cmds;

import io.github.oasis.core.elements.AbstractRule;

/**
 * @author Isuru Weerarathna
 */
public final class Messages {

    public static RuleAddedMessage createRuleAddMessage(int gameId, AbstractRule rule, Object messageId) {
        RuleAddedMessage addedMessage = new RuleAddedMessage(rule, messageId);
        addedMessage.setGameId(gameId);
        return addedMessage;
    }

    public static RuleRemovedMessage createRuleRemoveMessage(int gameId, String ruleId, Object messageId) {
        RuleRemovedMessage message = new RuleRemovedMessage(ruleId, messageId);
        message.setGameId(gameId);
        return message;
    }

    public static RuleUpdatedMessage createRuleUpdateMessage(int gameId, AbstractRule rule, Object messageId) {
        RuleUpdatedMessage message = new RuleUpdatedMessage(rule, messageId);
        message.setGameId(gameId);
        return message;
    }

    public static RuleDeactivatedMessage createRuleDeactivateMessage(int gameId, String ruleId, Object messageId) {
        RuleDeactivatedMessage message = new RuleDeactivatedMessage(ruleId, messageId);
        message.setGameId(gameId);
        return message;
    }

    public static RuleActivatedMessage createRuleActivateMessage(int gameId, String ruleId, Object messageId) {
        RuleActivatedMessage message = new RuleActivatedMessage(ruleId, messageId);
        message.setGameId(gameId);
        return message;
    }

}
