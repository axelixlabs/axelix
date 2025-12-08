/*
 * Copyright 2025-present, Nucleon Forge Software.
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
