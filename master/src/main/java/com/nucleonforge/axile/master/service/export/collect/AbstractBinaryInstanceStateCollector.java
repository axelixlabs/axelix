package com.nucleonforge.axile.master.service.export.collect;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.nucleonforge.axile.master.exception.StateExportException;
import com.nucleonforge.axile.master.service.export.StateComponentSettings;

/**
 * Abstract {@link InstanceStateCollector} that applies common binary data handling for binary state components.
 *
 * @since 20.11.2025
 * @author Nikita Kirillov
 */
public abstract class AbstractBinaryInstanceStateCollector<T extends StateComponentSettings>
        implements InstanceStateCollector<T> {

    @Override
    public byte[] collect(String instanceId, T settings) throws StateExportException {
        try {
            Resource resource = collectBinaryResource(instanceId, settings);
            return resource.getContentAsByteArray();
        } catch (IOException e) {
            throw new StateExportException(instanceId, e);
        }
    }

    protected abstract Resource collectBinaryResource(String instanceId, T settings) throws StateExportException;
}
