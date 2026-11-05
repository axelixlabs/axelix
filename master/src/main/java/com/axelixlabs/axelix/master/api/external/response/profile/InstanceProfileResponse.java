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
package com.axelixlabs.axelix.master.api.external.response.profile;

import java.util.List;

import com.axelixlabs.axelix.master.api.external.response.InstancesGridResponse.InstanceShortProfile;

/**
 * The profile of a single managed instance, i.e. everything the UI needs to render the instance view around its pages.
 *
 * @param instance      the short profile of the instance, the same as the instances grid renders.
 * @param siblingsCount the number of the other instances of the same application, i.e. excluding this one.
 * @param features      the ids of the features the instance has, see
 *                      {@link com.axelixlabs.axelix.common.contract.metadata.InstanceFeature}. The front-end renders a
 *                      feature's page only when its id is present here.
 *
 * @author Mikhail Polivakha
 */
public record InstanceProfileResponse(InstanceShortProfile instance, int siblingsCount, List<String> features) {}
