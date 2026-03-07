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

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.master.api.external.endpoint.caches.CachesReadApi;
import com.axelixlabs.axelix.master.service.export.StateComponent;
import com.axelixlabs.axelix.master.service.export.settings.CachesStateComponentSettings;

/**
 * Collects Spring Caches information for application state export.
 *
 * @see CachesReadApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 * @author Sergey Cherkasov
 */
@Component
public class CacheContributorByteInstance extends AbstractByteInstanceStateCollector<CachesStateComponentSettings> {

    private final CachesReadApi cachesReadApi;

    public CacheContributorByteInstance(final CachesReadApi cachesReadApi) {
        this.cachesReadApi = cachesReadApi;
    }

    @Override
    public StateComponent responsibleFor() {
        return StateComponent.CACHES;
    }

    @Override
    protected byte[] collectByte(String instanceId, CachesStateComponentSettings settings) {
        return cachesReadApi.getAllCaches(instanceId);
    }
}
