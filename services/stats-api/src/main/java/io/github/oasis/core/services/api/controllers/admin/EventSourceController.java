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

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.annotations.ForAdmin;
import io.github.oasis.core.services.api.controllers.AbstractController;
import io.github.oasis.core.services.api.services.EventSourceService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
public class EventSourceController extends AbstractController {

    private final EventSourceService eventSourceService;

    public EventSourceController(EventSourceService eventSourceService) {
        this.eventSourceService = eventSourceService;
    }

    @ForAdmin
    @PostMapping("/admin/event-sources")
    public EventSource registerEventSource(@RequestBody EventSource eventSource) throws OasisException {
        return eventSourceService.registerEventSource(eventSource);
    }

    @ForAdmin
    @GetMapping("/admin/event-sources/games/{gameId}")
    public List<EventSource> getEventSourcesOfGame(@PathVariable("gameId") Integer gameId) {
        return eventSourceService.listAllEventSourcesOfGame(gameId);
    }

    @ForAdmin
    @GetMapping("/admin/event-sources")
    public List<EventSource> getAllEventSources() {
        return eventSourceService.listAllEventSources();
    }

    @ForAdmin
    @PostMapping("/admin/event-sources/{eventSourceId}/games/{gameId}")
    public void assignEventSourceToGame(@PathVariable("eventSourceId") Integer eventSourceId,
                                        @PathVariable("gameId") Integer gameId) {
        eventSourceService.assignEventSourceToGame(eventSourceId, gameId);
    }

    @ForAdmin
    @DeleteMapping("/admin/event-sources/{eventSourceId}")
    public void deleteEventSource(@PathVariable("eventSourceId") Integer eventSourceId) {
        eventSourceService.deleteEventSource(eventSourceId);
    }
}
