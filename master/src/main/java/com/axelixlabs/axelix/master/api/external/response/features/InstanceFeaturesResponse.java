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
package com.axelixlabs.axelix.master.api.external.response.features;

import java.util.List;

/**
 * The features available for a single managed instance, resolved from the starter version it runs. The front-end
 * renders a certain or page section for a feature only when its id is present in {@link #features}.
 *
 * @param instanceId the id of the instance the features were resolved for.
 * @param features   the ids of the features available for the instance.
 *
 * @author Mikhail Polivakha
 */
public record InstanceFeaturesResponse(String instanceId, List<String> features) {}
