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
package com.axelixlabs.axelix.master.api.request.deserilize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.axelixlabs.axelix.master.api.request.state.BeansStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.CachesStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.ConditionsStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.ConfigPropsStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.EnvStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.GcLogFileStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.HeapDumpStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.ScheduledTasksStateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.StateComponentSettings;
import com.axelixlabs.axelix.master.api.request.state.StateExportComponent;
import com.axelixlabs.axelix.master.api.request.state.ThreadDumpStateComponentSettings;

/**
 * {@link JsonDeserializer} for the {@link List} of {@link StateExportComponent StateExportComponents}.
 *
 * @author Mikhail Polivakha
 */
public class StateExportComponentDeserializer extends JsonDeserializer<List<StateComponentSettings>> {

    private static final String SANITIZED_FIELD = "sanitized";

    @Override
    public List<StateComponentSettings> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode componentsNode = p.getCodec().readTree(p);

        if (componentsNode.isArray()) {
            return parseComponents(p, componentsNode);
        } else {
            throw new JsonParseException(p, "The 'components' is expected to be an array");
        }
    }

    // null away is simply wrong here
    // cyclomatic complexity skyrockets because of the switch
    @SuppressWarnings({"NullAway", "PMD.CyclomaticComplexity"})
    private static List<StateComponentSettings> parseComponents(JsonParser p, JsonNode componentsNode)
            throws JsonParseException {
        List<StateComponentSettings> results = new ArrayList<>(componentsNode.size());

        for (JsonNode childNode : componentsNode) {
            var stateComponentAsText =
                    childNode.get(StateComponentSettings.COMPONENT).asText();

            var stateExportComponent = StateExportComponent.valueOfIgnoreCase(stateComponentAsText);

            if (stateExportComponent == null) {
                throwUnexpectedStateExportValue(p, stateComponentAsText);
            }

            switch (stateExportComponent) {
                case HEAP_DUMP -> {
                    // TODO: can we make it better? Like via readValue or smth
                    boolean sanitized = extractSanitizedFlag(childNode);
                    var heapDumpStateComponentSettings = new HeapDumpStateComponentSettings(sanitized);
                    results.add(heapDumpStateComponentSettings);
                }
                case THREAD_DUMP -> results.add(new ThreadDumpStateComponentSettings());
                case BEANS -> results.add(new BeansStateComponentSettings());
                case CACHES -> results.add(new CachesStateComponentSettings());
                case CONDITIONS -> results.add(new ConditionsStateComponentSettings());
                case CONFIG_PROPS -> results.add(new ConfigPropsStateComponentSettings());
                case ENV -> results.add(new EnvStateComponentSettings());
                case GC_LOG_FILE -> results.add(new GcLogFileStateComponentSettings());
                case SCHEDULED_TASKS -> results.add(new ScheduledTasksStateComponentSettings());
            }
        }
        return results;
    }

    private static Boolean extractSanitizedFlag(JsonNode childNode) {
        return Optional.ofNullable(childNode.get(SANITIZED_FIELD))
                .map(it -> it.asBoolean(true))
                .orElse(true);
    }

    private static void throwUnexpectedStateExportValue(JsonParser p, String stateComponentAsText)
            throws JsonParseException {
        throw new JsonParseException(
                p,
                "The 'component' field is expected to be one of %s but was %s"
                        .formatted(Arrays.toString(StateExportComponent.values()), stateComponentAsText));
    }
}
