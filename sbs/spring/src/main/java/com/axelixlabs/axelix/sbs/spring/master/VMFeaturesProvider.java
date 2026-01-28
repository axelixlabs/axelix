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
package com.axelixlabs.axelix.sbs.spring.master;

import java.util.List;

import com.axelixlabs.axelix.common.api.registration.ServiceMetadata.VMFeature;

/**
 * Provides the information about specific VM features that are either used, or can potentially
 * be used for the benefit of the current application, for example AppCDS, AotCache, Compressed Object Headers etc.
 *
 * @author Mikhail Polivakha
 */
public interface VMFeaturesProvider {

    /**
     * @return the actual list of VM features.
     */
    List<VMFeature> discover();
}
