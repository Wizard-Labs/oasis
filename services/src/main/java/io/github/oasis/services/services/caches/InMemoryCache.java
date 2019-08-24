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

package io.github.oasis.services.services.caches;

import io.github.oasis.model.utils.ICacheProxy;
import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.utils.LRUCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cacheMemory")
public class InMemoryCache implements ICacheProxy {

    private LRUCache<String, String> cache;

    @Autowired
    private OasisConfigurations configurations;


    @Override
    public void init() {
        this.cache = new LRUCache<>(Math.max(configurations.getCache().getMemorySize(), 100));
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.of(cache.get(key));
    }

    @Override
    public void update(String key, String value) {
        cache.put(key, value);
    }

    @Override
    public void expire(String key) {
        cache.remove(key);
    }
}
