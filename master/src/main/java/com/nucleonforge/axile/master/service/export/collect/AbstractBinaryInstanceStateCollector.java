package com.nucleonforge.axile.master.service.export.collect;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.io.Resource;

import com.nucleonforge.axile.master.exception.StateExportException;

/**
 * Abstract {@link InstanceStateCollector} that applies common binary data handling for binary state components.
 *
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
public abstract class AbstractBinaryInstanceStateCollector implements InstanceStateCollector {

    private static final Logger log = LoggerFactory.getLogger(AbstractBinaryInstanceStateCollector.class);

    @Override
    public byte[] collect(String instanceId) throws StateExportException {
        try {
            Resource resource = collectBinaryResource(instanceId);
            return resource.getContentAsByteArray();
        } catch (IOException e) {
            throw new StateExportException(instanceId, e);
        }
    }

    protected abstract Resource collectBinaryResource(String instanceId) throws StateExportException;
}
