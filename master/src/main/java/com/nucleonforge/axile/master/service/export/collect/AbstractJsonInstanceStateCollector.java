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
