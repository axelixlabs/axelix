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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.DefaultHttpPayload;
import com.axelixlabs.axelix.common.domain.http.MultiValueQueryParameter;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.common.domain.http.QueryParameter;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing metrics.
 *
 * @since 19.11.2025
 * @author Nikita Kirillov
 */
@ExternalApiRestController
public class MetricsApi {

    private final EndpointInvoker endpointInvoker;

    public MetricsApi(EndpointInvoker endpointInvoker) {
        this.endpointInvoker = endpointInvoker;
    }

    @GetMapping(path = ApiPaths.MetricsApi.INSTANCE_ID)
    public ResponseEntity<byte[]> getMetricGroups(@PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_METRIC_GROUPS, NoHttpPayload.INSTANCE);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }

    @GetMapping(path = ApiPaths.MetricsApi.METRIC_NAME)
    public ResponseEntity<byte[]> getSingleMetric(
            @PathVariable("instanceId") String instanceId,
            @PathVariable("metric") String metric,
            @RequestParam(value = "tag", required = false) List<String> tags) {

        List<QueryParameter<?>> queryParameters = new ArrayList<>();
        if (tags != null && !tags.isEmpty()) {
            queryParameters.add(new MultiValueQueryParameter("tag", tags));
        }

        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId),
                ActuatorEndpoints.GET_SINGLE_METRIC,
                new DefaultHttpPayload(queryParameters, Map.of("metric.name", metric)));

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(body);
    }
}
