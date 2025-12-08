/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.details;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import com.nucleonforge.axile.common.api.AxileDetails;

/**
 * Custom Spring Boot Actuator endpoint. Provides comprehensive instance operational details.
 *
 * @see AxileDetails
 * @since 29.10.2025
 * @author Nikita Kirillov
 */
@Endpoint(id = "axile-details")
public class AxileDetailsEndpoint {

    private final ServiceDetailsAssembler serviceDetailsAssembler;

    public AxileDetailsEndpoint(ServiceDetailsAssembler serviceDetailsAssembler) {
        this.serviceDetailsAssembler = serviceDetailsAssembler;
    }

    @ReadOperation
    public AxileDetails details() {
        return serviceDetailsAssembler.assemble();
    }
}
