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
package com.axelixlabs.axelix.master.api.external.request.deserilize;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import com.axelixlabs.axelix.master.api.external.request.state.BeansStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.CachesStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.ConditionsStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.ConfigPropsStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.EnvStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.GcLogFileStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.HeapDumpStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.ScheduledTasksStateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.StateComponentSettings;
import com.axelixlabs.axelix.master.api.external.request.state.StateExportComponent;
import com.axelixlabs.axelix.master.api.external.request.state.ThreadDumpStateComponentSettings;

/**
 * {@link ValueDeserializer} for the {@link List} of {@link StateExportComponent StateExportComponents}.
 *
 * @author Mikhail Polivakha
 */
public class StateExportComponentDeserializer extends ValueDeserializer<List<StateComponentSettings>> {

    @Override
    public List<StateComponentSettings> deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonNode componentsNode = p.readValueAsTree();

        if (componentsNode.isArray()) {
            return parseComponents(p, componentsNode);
        } else {
            throw new IllegalArgumentException("The 'components' is expected to be an array");
        }
    }

    // null away is simply wrong here
    // cyclomatic complexity skyrockets because of the switch
    @SuppressWarnings({"NullAway", "PMD.CyclomaticComplexity"})
    private static List<StateComponentSettings> parseComponents(JsonParser p, JsonNode componentsNode) {
        List<StateComponentSettings> results = new ArrayList<>(componentsNode.size());

        for (JsonNode childNode : componentsNode) {
            var stateComponentAsText =
                    childNode.get(StateComponentSettings.COMPONENT).asText();

            var stateExportComponent = StateExportComponent.valueOfIgnoreCase(stateComponentAsText);

            if (stateExportComponent == null) {
                throwUnexpectedStateExportValue(p, stateComponentAsText);
            }

            switch (stateExportComponent) {
                case HEAP_DUMP -> results.add(new HeapDumpStateComponentSettings());
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

    private static void throwUnexpectedStateExportValue(JsonParser p, String stateComponentAsText) {
        throw new IllegalArgumentException("The 'component' field is expected to be one of %s but was %s"
                .formatted(Arrays.toString(StateExportComponent.values()), stateComponentAsText));
    }
}
