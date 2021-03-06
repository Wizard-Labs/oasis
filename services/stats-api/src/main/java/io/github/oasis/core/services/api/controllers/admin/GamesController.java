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

package io.github.oasis.core.services.api.controllers.admin;

import io.github.oasis.core.Game;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.annotations.ForPlayer;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.GameService;
import io.github.oasis.core.services.api.to.GameObjectRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@RestController
@RequestMapping(
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE
)
public class GamesController extends AbstractController {

    private final GameService gameService;

    public GamesController(GameService gameService) {
        this.gameService = gameService;
    }

    @ForAdmin
    @PostMapping(path = "/admin/games")
    public Game addGame(@RequestBody GameObjectRequest request) throws OasisException {
        return gameService.addGame(request);
    }

    @ForPlayer
    @GetMapping(path = "/admin/games")
    public List<Game> listGames() {
        return gameService.listAllGames();
    }

    @ForPlayer
    @GetMapping(path = "/admin/games/{gameId}")
    public Game readGame(@PathVariable("gameId") Integer gameId) {
        return gameService.readGame(gameId);
    }

    @ForAdmin
    @PutMapping(path = "/admin/games/{gameId}")
    public Game updateGame(@PathVariable("gameId") Integer gameId,
                           @RequestBody GameObjectRequest request) throws OasisException {
        Game game = new Game();
        game.setId(request.getId());
        game.setName(request.getName());
        game.setDescription(request.getDescription());
        game.setMotto(request.getMotto());

        return gameService.updateGame(gameId, game);
    }

    @ForAdmin
    @DeleteMapping(path = "/admin/games/{gameId}")
    public Game deleteGame(@PathVariable("gameId") Integer gameId) {
        return gameService.deleteGame(gameId);
    }
}
