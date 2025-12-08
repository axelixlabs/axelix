/*
 * Copyright 2025-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.master.service.export.collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * Abstract {@link InstanceStateCollector} that applies common marshalling and exception
 * handling logic.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractJsonInstanceStateCollector<T extends StateComponentSettings>
        implements InstanceStateCollector<T> {

    private static final Logger log = LoggerFactory.getLogger(AbstractJsonInstanceStateCollector.class);

    protected final ObjectMapper objectMapper;

    public AbstractJsonInstanceStateCollector() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public byte[] collect(String instanceId, T settings) throws StateExportException {
        Object state = collectInternal(instanceId, settings);
        try {
            return objectMapper.writeValueAsBytes(state);
        } catch (JsonProcessingException e) {
            log.warn("Unable to serialize state provided by collector responsible for : {}", this.responsibleFor(), e);
            throw new StateExportException(instanceId, e);
        }
    }

    /**
     * Actual state collection function.
     *
     * @return the JSON marshalling-capable object that represents the price of state of the particular application.
     */
    protected abstract Object collectInternal(String instanceId, T settings);
}
