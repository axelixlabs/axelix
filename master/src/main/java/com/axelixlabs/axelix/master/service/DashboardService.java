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
package com.axelixlabs.axelix.master.service;

import com.axelixlabs.axelix.master.api.external.response.DashboardResponse;

/**
 * Service that collects information necessary for dashboard rendering.
 *
 * @author Mikhail Polivakha
 */
public interface DashboardService {

    /**
     * It is intentional that here we return the DTO from the API layer inside the service layer.
     * As of time of writing this code, there is not that much reasoning to split the model returned
     * by the service layer from the {@link DashboardResponse} DTO returned from the UI layer.
     * @return the {@link DashboardResponse}.
     */
    DashboardResponse getDashboardInfo();
}
