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

import java.util.Optional;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.common.api.registration.insights.persistence.PersistenceInsights;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.api.external.swagger.InstanceIdParameter;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.DatabaseHistoricalApplicationSnapshotService;

/**
 * The API for Transaction Monitoring.
 *
 * @since 20.01.2026
 * @author Nikita Kirillov
 */
@ExternalApiRestController
public class TransactionMonitoringApi {

    private final DatabaseHistoricalApplicationSnapshotService historicalApplicationSnapshotService;

    public TransactionMonitoringApi(DatabaseHistoricalApplicationSnapshotService historicalApplicationSnapshotService) {
        this.historicalApplicationSnapshotService = historicalApplicationSnapshotService;
    }

    @DefaultApiResponse(summary = "Returns transactional persistence insights for the given instance.")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PersistenceInsights.class)))
    @InstanceIdParameter
    @GetMapping(path = ApiPaths.TransactionMonitoringApi.INSTANCE_ID, produces = MediaType.APPLICATION_JSON_VALUE)
    public PersistenceInsights getTransactionFeed(@PathVariable("instanceId") String instanceId) {
        InstanceId id = InstanceId.of(instanceId);

        return Optional.ofNullable(historicalApplicationSnapshotService.getLatestPersistenceInsights(id))
                .orElseThrow(() -> new InstanceNotFoundException(id));
    }
}
