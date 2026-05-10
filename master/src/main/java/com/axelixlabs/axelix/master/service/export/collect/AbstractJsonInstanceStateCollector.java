/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.service.export.collect;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.exception.StateExportException;

/**
 * Abstract {@link InstanceStateCollector} that applies common marshalling and exception
 * handling logic.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public abstract class AbstractJsonInstanceStateCollector implements InstanceStateCollector {

    private static final Logger log = LoggerFactory.getLogger(AbstractJsonInstanceStateCollector.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public byte[] collect(String instanceId) throws StateExportException {
        try {
            // TODO:
            //  Now, here, we're essentially reading JSON just to "prettify" it.
            //  So it may and probably would have some performance implications.
            byte[] state = collectByte(instanceId);
            JsonNode json = OBJECT_MAPPER.readTree(state);

            return OBJECT_MAPPER.writeValueAsBytes(json);
        } catch (InstanceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Unable to serialize state provided by collector responsible for : {}", this.responsibleFor(), e);
            throw new StateExportException(instanceId, e);
        }
    }

    protected abstract byte[] collectByte(String instanceId);
}
