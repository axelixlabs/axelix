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

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.common.api.InstanceDetails;
import com.axelixlabs.axelix.common.domain.ActuatorEndpoints;
import com.axelixlabs.axelix.common.domain.http.NoHttpPayload;
import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.InstanceDetailsResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.convert.response.Converter;
import com.axelixlabs.axelix.master.service.convert.response.details.DetailsConversionRequest;
import com.axelixlabs.axelix.master.service.transport.EndpointInvoker;

/**
 * The API for managing details.
 *
 * @author Nikita Kirilov, Sergey Cherkasov
 */
@ExternalApiRestController
public class DetailsApi {

    private final EndpointInvoker endpointInvoker;
    private final Converter<DetailsConversionRequest, InstanceDetailsResponse> converter;

    public DetailsApi(
            EndpointInvoker endpointInvoker, Converter<DetailsConversionRequest, InstanceDetailsResponse> converter) {
        this.endpointInvoker = endpointInvoker;
        this.converter = converter;
    }

    @GetMapping(path = ApiPaths.DetailsApi.INSTANCE_ID)
    public InstanceDetailsResponse getDetailsResponse(@PathVariable("instanceId") String instanceId)
            throws InstanceNotFoundException {

        InstanceId id = InstanceId.of(instanceId);
        InstanceDetails instanceDetails =
                endpointInvoker.invoke(id, ActuatorEndpoints.GET_DETAILS, NoHttpPayload.INSTANCE);
        return Objects.requireNonNull(converter.convert(new DetailsConversionRequest(instanceDetails, id)));
    }
}
