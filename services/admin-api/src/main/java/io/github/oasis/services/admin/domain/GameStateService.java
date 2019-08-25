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

package io.github.oasis.services.admin.domain;

import io.github.oasis.services.admin.internal.dao.IGameDao;
import org.springframework.stereotype.Component;

/**
 * @author Isuru Weerarathna
 */
@Component
public class GameStateService {

    private IGameDao gameDao;

    public GameStateService(IGameDao gameDao) {
        this.gameDao = gameDao;
    }

    public void startGame(int gameId) {
        gameDao.startGame(gameId);
    }

    public void stopGame(int gameId) {
        gameDao.stopGame(gameId);
    }

    public void pauseGame(int gameId) {
        gameDao.pauseGame(gameId);
    }

    public void deleteGame(int gameId) {
        gameDao.deactivateGame(gameId);
    }
}
