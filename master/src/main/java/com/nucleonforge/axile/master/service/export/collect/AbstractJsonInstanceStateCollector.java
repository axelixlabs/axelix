package com.nucleonforge.axile.master.service.export.collect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Abstract {@link JsonInstanceStateCollector} that applies common marshalling and exception
 * handling logic.
 *
 * @author Mikhail Polivakha
 */
public abstract class AbstractJsonInstanceStateCollector implements JsonInstanceStateCollector {

    private static final Logger log = LoggerFactory.getLogger(AbstractJsonInstanceStateCollector.class);

    protected final ObjectMapper objectMapper;

    public AbstractJsonInstanceStateCollector() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public String collect(String instanceId) throws StateExportException {
        Object state = collectInternal(instanceId);
        try {
            return objectMapper.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            log.warn("Unable to serialize state provided by collector : {}", this.getName(), e);
            throw new StateExportException(instanceId, e);
        }
    }

    /**
     * Actual state collection function.
     *
     * @return the JSON marshalling-capable object that represents the price of state of the particular application.
     */
    protected abstract Object collectInternal(String instanceId);
}
