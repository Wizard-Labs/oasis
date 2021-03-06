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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class ElementService {

    private final OasisRepository oasisRepository;
    private final OasisMetadataSupport metadataSupport;

    public ElementService(OasisRepository oasisRepository, OasisMetadataSupport metadataSupport) {
        this.oasisRepository = oasisRepository;
        this.metadataSupport = metadataSupport;
    }

    public ElementDef readElement(int gameId, String elementId) {
        return oasisRepository.readElement(gameId, elementId);
    }

    public ElementDef addElement(int gameId, ElementDef elementDef) {
        return oasisRepository.addNewElement(gameId, elementDef);
    }

    public ElementDef updateElement(int gameId, String elementId, ElementDef elementDef) {
        return oasisRepository.updateElement(gameId, elementId, elementDef);
    }

    public ElementDef deleteElement(int gameId, String elementId) {
        return oasisRepository.deleteElement(gameId, elementId);
    }

    public List<SimpleElementDefinition> listElementsByType(int gameId, String type) throws OasisException {
        return metadataSupport.listAllElementDefinitions(gameId, type);
    }
}
