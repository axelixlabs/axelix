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
package com.axelixlabs.axelix.master.api.external.endpoint;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.service.convert.response.InstancesToShortProfileConverter;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * The API for managing applications.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class WallboardApi {

    private final InstanceRegistry instanceRegistry;
    private final InstancesToShortProfileConverter instancesToShortProfileConverter;

    public WallboardApi(
            InstanceRegistry instanceRegistry, InstancesToShortProfileConverter instancesToShortProfileConverter) {
        this.instanceRegistry = instanceRegistry;
        this.instancesToShortProfileConverter = instancesToShortProfileConverter;
    }

    @GetMapping(path = ApiPaths.InstancesApi.GRID)
    @SuppressWarnings("NullAway")
    public InstancesGridResponse getInstancesGrid() {
        Collection<Instance> all = instanceRegistry.getAll();
        return new InstancesGridResponse(instancesToShortProfileConverter.convertAll(all));
    }
}
