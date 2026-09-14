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
package com.axelixlabs.axelix.master.exception;

import com.axelixlabs.axelix.master.domain.InstanceId;

/**
 * Thrown when a managed instance is reachable but serves no dependency SBOM, which means the application was built
 * without an Axelix build plugin. The UI renders a dedicated empty state for it, telling the user to rebuild with
 * the plugin rather than suggesting the instance is broken.
 *
 * @author Mikhail Polivakha
 */
public class SbomNotAvailableException extends RuntimeException {

    public SbomNotAvailableException(InstanceId instanceId) {
        super(
                "The instance '%s' serves no dependency SBOM; the application was likely built without an Axelix build plugin"
                        .formatted(instanceId));
    }
}
