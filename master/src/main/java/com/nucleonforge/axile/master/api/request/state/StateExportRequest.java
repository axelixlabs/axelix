package com.nucleonforge.axile.master.api.request.state;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.master.api.request.deserilize.StateExportComponentDeserializer;

/**
 * Request for export for of the state of the application.
 *
 * @param components List of components to export with their settings.
 * @author Mikhail Polivakha
 */
public record StateExportRequest(
        @NonNull @JsonDeserialize(using = StateExportComponentDeserializer.class)
                List<StateComponentSettings> components) {

    @Override
    public List<StateComponentSettings> components() {
        return components;
    }
}
