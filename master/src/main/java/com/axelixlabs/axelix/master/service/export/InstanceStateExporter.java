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
package com.axelixlabs.axelix.master.service.export;

import com.axelixlabs.axelix.master.exception.StateExportException;
import com.axelixlabs.axelix.master.model.instance.Instance;
import com.axelixlabs.axelix.master.model.instance.InstanceId;
import com.axelixlabs.axelix.master.service.export.collect.InstanceStateCollector;

/**
 * Service for exporting the state of the given {@link Instance}.
 * <p>
 * The "state" of the given instance is assembled by {@link InstanceStateCollector JsonInstanceStateCollectors}.
 *
 * @author Nikita Kirillov
 * @since 27.10.2025
 */
public interface InstanceStateExporter {

    /**
     * Exports state of the specified application instance.
     *
     * @param request request that accumulates all the info required for state export.
     * @return byte array containing the exported state data.
     * @throws StateExportException if export process fails.
     */
    byte[] exportInstanceState(StateExport request, InstanceId instanceId) throws StateExportException;
}
