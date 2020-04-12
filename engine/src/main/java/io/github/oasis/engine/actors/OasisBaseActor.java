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

package io.github.oasis.engine.actors;

import akka.actor.AbstractActor;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Props;
import akka.actor.SupervisorStrategy;
import akka.japi.pf.DeciderBuilder;
import akka.routing.ActorRefRoutee;
import akka.routing.Routee;
import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.factory.InjectedActorSupport;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public abstract class OasisBaseActor extends AbstractActor implements InjectedActorSupport {

    private static final int MAX_NR_OF_RETRIES = 10;

    private static final SupervisorStrategy RESTART_STRATEGY = new OneForOneStrategy(
            MAX_NR_OF_RETRIES,
            Duration.ofMinutes(1),
            DeciderBuilder.matchAny(e -> SupervisorStrategy.restart())
                    .build()
    );

    protected OasisConfigs configs;

    OasisBaseActor(OasisConfigs configs) {
        this.configs = configs;
    }

    protected <T extends Actor> List<Routee> createChildRouteActorsOfType(Class<T> actorClz, Function<Integer, String> actorNameSupplier, int count) {
        List<Routee> allRoutes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ActorRef ruleActor = getContext().actorOf(Props.create(actorClz,
                    () -> injectActor(actorClz)), actorNameSupplier.apply(i));
            getContext().watch(ruleActor);
            allRoutes.add(new ActorRefRoutee(ruleActor));
        }
        return allRoutes;
    }

    @Override
    public SupervisorStrategy supervisorStrategy() {
        return RESTART_STRATEGY;
    }
}
