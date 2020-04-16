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

package io.github.oasis.engine.external.redis;

import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.Mapped;
import io.github.oasis.engine.external.Sorted;
import io.github.oasis.engine.utils.Numbers;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Isuru Weerarathna
 */
public class RedisContext implements DbContext {

    private static final String INCRALL = "O.INCRALL";
    private static final String ZINCRALL = "O.ZINCRALL";

    private final Jedis jedis;
    private final RedisDb db;

    public RedisContext(RedisDb db, Jedis jedis) {
        this.db = db;
        this.jedis = jedis;
    }

    @Override
    public void close() {
        if (jedis != null) {
            jedis.close();
        }
    }

    @Override
    public Set<String> allKeys(String pattern) {
        return jedis.keys(pattern);
    }

    @Override
    public void removeKey(String key) {
        jedis.del(key);
    }

    @Override
    public BigDecimal incrementScoreInSorted(String contextKey, String member, BigDecimal byScore) {
        return BigDecimal.valueOf(jedis.zincrby(contextKey, byScore.doubleValue(), member));
    }

    @Override
    public void setValueInMap(String contextKey, String field, String value) {
        jedis.hset(contextKey, field, value);
    }

    @Override
    public String getValueFromMap(String contextKey, String key) {
        return jedis.hget(contextKey, key);
    }

    @Override
    public void addToSorted(String contextKey, String member, long value) {
        jedis.zadd(contextKey, value, member);
    }

    @Override
    public boolean setIfNotExistsInMap(String contextKey, String key, String value) {
        return Numbers.isFirstOne(jedis.hsetnx(contextKey, key, value));
    }

    @Override
    public List<String> getValuesFromMap(String contextKey, String... keys) {
        return jedis.hmget(contextKey, keys);
    }

    @Override
    public Sorted SORTED(String contextKey) {
        return new RedisSortedSet(jedis, contextKey);
    }

    @Override
    public Mapped MAP(String contextKey) {
        return new RedisHashSet(jedis, contextKey);
    }

    @Override
    public void incrementAll(int value, String baseKey, List<String> keys) {
        incrementAll(String.valueOf(value), baseKey, keys);
    }

    @Override
    public void incrementAll(BigDecimal value, String baseKey, List<String> keys) {
        incrementAll(value.toString(), baseKey, keys);
    }

    @Override
    public void incrementAllInSorted(BigDecimal value, String commonMember, List<String> baseKeys) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(commonMember);
        allArgs.addAll(baseKeys);
        allArgs.add(value.toString());
        String[] args = allArgs.toArray(new String[0]);
        runScript(ZINCRALL, args.length - 1, args);
    }

    private void incrementAll(String value, String baseKey, List<String> keys) {
        List<String> allArgs = new ArrayList<>();
        allArgs.add(baseKey);
        allArgs.addAll(keys);
        allArgs.add(value);
        String[] args = allArgs.toArray(new String[0]);
        runScript(INCRALL, args.length - 1, args);
    }

    @Override
    public Object runScript(String scriptName, int noOfKeys, String... args) {
        RedisDb.RedisScript scriptSha = db.getScriptSha(scriptName);
        return jedis.evalsha(scriptSha.getSha(), noOfKeys, args);
    }
}
