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

import tools.jackson.databind.ObjectMapper;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.env.EnvironmentFeedResponse;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing environment.
 *
 * @since 27.08.2025
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@ExternalApiRestController
public class EnvironmentApi {

    private final EndpointInvoker endpointInvoker;
    private final ObjectMapper objectMapper;
    private final Converter<EnvironmentFeed, EnvironmentFeedResponse> environmentFeedConverter;

    public EnvironmentApi(
            EndpointInvoker endpointInvoker,
            ObjectMapper objectMapper,
            Converter<EnvironmentFeed, EnvironmentFeedResponse> environmentFeedConverter) {
        this.endpointInvoker = endpointInvoker;
        this.objectMapper = objectMapper;
        this.environmentFeedConverter = environmentFeedConverter;
    }

    @GetMapping(path = ApiPaths.EnvironmentApi.FEED)
    public ResponseEntity<EnvironmentFeedResponse> getAllEnvironmentProperties(
            @PathVariable("instanceId") String instanceId) {
        byte[] body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_ALL_ENV_PROPERTIES, NoHttpPayload.INSTANCE);

        EnvironmentFeed feed = objectMapper.readValue(body, EnvironmentFeed.class);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(environmentFeedConverter.convert(feed));
    }
}
