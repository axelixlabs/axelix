package com.nucleonforge.axile.master.api.request.deserilize;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.nucleonforge.axile.master.api.request.state.BeansStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.CachesStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.ConditionsStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.ConfigPropsStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.EnvStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.HeapDumpStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.LogFileStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.ScheduledTasksStateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.StateComponentSettings;
import com.nucleonforge.axile.master.api.request.state.StateExportComponent;
import com.nucleonforge.axile.master.api.request.state.ThreadDumpStateComponentSettings;

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
                    boolean sanitized = childNode.get(SANITIZED_FIELD).asBoolean(true);
                    var heapDumpStateComponentSettings = new HeapDumpStateComponentSettings(sanitized);
                    results.add(heapDumpStateComponentSettings);
                }
                case THREAD_DUMP -> results.add(new ThreadDumpStateComponentSettings());
                case BEANS -> results.add(new BeansStateComponentSettings());
                case CACHES -> results.add(new CachesStateComponentSettings());
                case CONDITIONS -> results.add(new ConditionsStateComponentSettings());
                case CONFIG_PROPS -> results.add(new ConfigPropsStateComponentSettings());
                case ENV -> results.add(new EnvStateComponentSettings());
                case LOG_FILE -> results.add(new LogFileStateComponentSettings());
                case SCHEDULED_TASKS -> results.add(new ScheduledTasksStateComponentSettings());
            }
        }
        return results;
    }

    private static void throwUnexpectedStateExportValue(JsonParser p, String stateComponentAsText)
            throws JsonParseException {
        throw new JsonParseException(
                p,
                "The 'component' field is expected to be one of %s but was %s"
                        .formatted(Arrays.toString(StateExportComponent.values()), stateComponentAsText));
    }
}
