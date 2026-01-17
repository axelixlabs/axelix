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
package com.nucleonforge.axelix.master.service.export.collect;

import java.io.IOException;

import org.springframework.core.io.Resource;

import com.nucleonforge.axelix.master.exception.StateExportException;
import com.nucleonforge.axelix.master.service.export.StateComponentSettings;

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
            Resource resource = collectResource(instanceId, settings);
            return resource.getContentAsByteArray();
        } catch (IOException e) {
            throw new StateExportException(instanceId, e);
        }
    }

    protected abstract Resource collectResource(String instanceId, T settings) throws StateExportException;
}
