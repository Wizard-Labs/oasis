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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventDispatchSupport;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.serviceproxy.ServiceBinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class DispatcherVerticle extends AbstractVerticle {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherVerticle.class);

    private final EventDispatchSupport eventDispatcher;

    public DispatcherVerticle(EventDispatchSupport eventDispatcher) {
        this.eventDispatcher = eventDispatcher;
    }

    @Override
    public void start(Promise<Void> promise) {
        LOG.info("Initializing event dispatcher {}...", eventDispatcher.getClass().getName());
        JsonObject dispatcherConfigs = config().copy();
        VertxDispatcherContext ctx = new VertxDispatcherContext(configToMap(dispatcherConfigs));

        vertx.executeBlocking(onConnectPromise -> {
            try {
                eventDispatcher.init(ctx);
                onConnectPromise.complete();
            } catch (Exception e) {
                onConnectPromise.fail(e);
            }
        }, res -> {
            if (res.succeeded()) {
                WrappedDispatcherService wrappedDispatcherService = new WrappedDispatcherService(vertx, eventDispatcher);
                ServiceBinder binder = new ServiceBinder(vertx);
                binder.setAddress(EventDispatcherService.DISPATCHER_SERVICE_QUEUE)
                        .register(EventDispatcherService.class, wrappedDispatcherService);
                LOG.info("Dispatcher initialization successful!");
                promise.complete();
            } else {
                LOG.error("Failed to establish connection to RabbitMQ!", res.cause());
                promise.fail(res.cause());
            }
        });
    }

    @Override
    public void stop() throws Exception {
        LOG.warn("Stopping event dispatcher...");
        eventDispatcher.close();
    }

    private Map<String, Object> configToMap(JsonObject config) {
        Yaml yaml = new Yaml();
        return yaml.load(config.toString());
    }

    static class VertxDispatcherContext implements EventDispatchSupport.DispatcherContext {

        private Map<String, Object> configs;

        VertxDispatcherContext(Map<String, Object> configs) {
            this.configs = configs;
        }

        @Override
        public Map<String, Object> getConfigs() {
            return configs;
        }

    }
}
