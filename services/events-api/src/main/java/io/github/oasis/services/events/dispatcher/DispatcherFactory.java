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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.EventAsyncDispatchSupport;
import io.github.oasis.core.external.EventDispatchSupport;
import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Isuru Weerarathna
 */
public class DispatcherFactory implements VerticleFactory {

    private static final Logger LOG = LoggerFactory.getLogger(DispatcherFactory.class);

    public static final String OASIS_VERTICLE = "oasis";
    private static final String OASIS_PREFIX = OASIS_VERTICLE + ":";

    @Override
    public String prefix() {
        return OASIS_VERTICLE;
    }

    @Override
    public Verticle createVerticle(String type, ClassLoader classLoader) throws Exception {
        String impl = StringUtils.substringAfter(type, OASIS_PREFIX);
        LOG.info("Creating dispatcher of type: {}", impl);
        try {
            Object instance = classLoader.loadClass(impl).getDeclaredConstructor().newInstance();
            if (instance instanceof EventDispatchSupport) {
                EventDispatchSupport dispatchSupport = (EventDispatchSupport) instance;
                if (instance instanceof EventAsyncDispatchSupport) {
                    return new DispatcherAsyncVerticle((EventAsyncDispatchSupport) instance);
                }
                return new DispatcherVerticle(dispatchSupport);
            } else if (instance instanceof Verticle) {
                return (Verticle) instance;
            } else {
                throw new IllegalArgumentException("Unknown dispatcher type provided! " + impl);
            }
        } catch (ReflectiveOperationException e) {
            LOG.error("Cannot load provided dispatcher implementation!", e);
            throw e;
        }
    }
}
