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
package com.axelixlabs.axelix.master.service.export.collect;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.external.endpoint.GcLogFileApi;
import com.axelixlabs.axelix.master.exception.StateExportException;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.settings.GcLogFileStateComponentSettings;

/**
 * Collect gc-logfile for application state export.
 *
 * @see GcLogFileApi
 * @since 10.01.2026
 * @author Nikita Kirillov
 */
@Component
public class GcLogFileContributorBinaryInstance
        extends AbstractBinaryInstanceStateCollector<GcLogFileStateComponentSettings> {

    private final GcLogFileApi gcLogFileApi;

    public GcLogFileContributorBinaryInstance(GcLogFileApi gcLogFileApi) {
        this.gcLogFileApi = gcLogFileApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.GC_LOG_FILE;
    }

    @Override
    protected Resource collectResource(String instanceId, GcLogFileStateComponentSettings settings)
            throws StateExportException {
        return gcLogFileApi.getGcLogFile(instanceId);
    }
}
