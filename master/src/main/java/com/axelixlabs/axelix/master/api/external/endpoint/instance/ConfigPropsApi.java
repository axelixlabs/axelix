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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.common.contract.configprops.ConfigurationPropertiesFeed;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.configprops.ConfigurationPropertiesFeedResponse;
import com.axelixlabs.axelix.master.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.master.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing configprops.
 *
 * @author Mikhail Polivakha
 * @author Sergey Cherkasov
 */
@ExternalApiRestController
public class ConfigPropsApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<ConfigurationPropertiesFeed, ConfigurationPropertiesFeedResponse> feedConverter;

    public ConfigPropsApi(
            EndpointInvoker endpointInvoker,
            Converter<ConfigurationPropertiesFeed, ConfigurationPropertiesFeedResponse> feedConverter) {
        this.endpointInvoker = endpointInvoker;
        this.feedConverter = feedConverter;
    }

    @GetMapping(path = ApiPaths.ConfigPropsApi.FEED)
    public ResponseEntity<ConfigurationPropertiesFeedResponse> getConfigpropsFeed(
            @PathVariable("instanceId") String instanceId) {
        ConfigurationPropertiesFeed body = endpointInvoker.invoke(
                InstanceId.of(instanceId), ActuatorEndpoints.GET_CONFIG_PROPS, NoHttpPayload.INSTANCE);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(feedConverter.convertNonNull(body));
    }
}
