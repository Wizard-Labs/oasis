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

package io.github.oasis.core.services.api.configs;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisDbException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.db.redis.RedisDb;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * @author Isuru Weerarathna
 */
@Configuration
public class DatabaseConfigs {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseConfigs.class);

    @Value("${oasis.configs.path}")
    private String oasisConfigFilePath;

    @Value("${oasis.db.retries:5}")
    private int numberOfDbRetries;

    @Value("${oasis.db.retry.interval:3000}")
    private int dbRetryInterval;

    @Bean
    public OasisConfigs loadOasisConfigs() {
        if (Texts.isEmpty(oasisConfigFilePath)) {
            LOG.warn("Loading default configurations bundled with artifacts!");
            return OasisConfigs.defaultConfigs();
        } else {
            File file = new File(oasisConfigFilePath);
            if (file.exists()) {
                LOG.info("Loading configuration file in {}...", oasisConfigFilePath);
                return OasisConfigs.create(oasisConfigFilePath);
            }
            throw new IllegalStateException("Cannot load Oasis configurations! Config file not found in " + oasisConfigFilePath + "!");
        }
    }

    @Bean
    public Db createDbService(OasisConfigs oasisConfigs) throws Exception {
        LOG.info("Trying to create database connection... (with retries {})", numberOfDbRetries);
        return loadDbService(oasisConfigs, numberOfDbRetries);
    }

    private Db loadDbService(OasisConfigs oasisConfigs, int retries) throws Exception {
        if (retries > 0) {
            try {
                RedisDb redisDb = RedisDb.create(oasisConfigs);
                redisDb.init();
                return redisDb;
            } catch (Throwable e) {
                LOG.error("Could not load redis connection! Trying again later after {}ms... [Remaining: {}]", dbRetryInterval, retries);
                Thread.sleep(dbRetryInterval);
                return loadDbService(oasisConfigs, retries - 1);
            }
        }
        throw new OasisDbException("Unable to create a redis connection!");
    }

}
