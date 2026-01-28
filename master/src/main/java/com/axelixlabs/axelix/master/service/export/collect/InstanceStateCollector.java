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

import com.axelixlabs.axelix.master.exception.StateExportException;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.StateComponentSettings;

/**
 * Collector for application state data export functionality.
 *
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
public interface InstanceStateCollector<T extends StateComponentSettings> {

    /**
     * @return the {@link StateComponent state export component} that this collector is responsible for.
     */
    StateComponent responsibleFor();

    /**
     * Collects data from the specified application instance.
     *
     * @param instanceId the identifier of the application instance to collect data from
     * @return the collected data as the byte array
     */
    byte[] collect(String instanceId, T settings) throws StateExportException;
}
