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
package com.nucleonforge.axelix.sbs.spring.env;

import org.jspecify.annotations.Nullable;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint;
import org.springframework.boot.actuate.env.EnvironmentEndpoint.EnvironmentDescriptor;
import org.springframework.web.bind.annotation.GetMapping;

import com.nucleonforge.axelix.common.api.env.EnvironmentFeed;

/**
 * Custom Spring Boot Actuator endpoint providing an extended view of the application's environment.
 *
 * @since 21.10.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-env")
public class AxileEnvironmentEndpoint {

    private final EnvironmentEndpoint delegate;

    private final EnvPropertyEnricher envPropertyEnricher;

    public AxileEnvironmentEndpoint(EnvironmentEndpoint delegate, EnvPropertyEnricher envPropertyEnricher) {
        this.delegate = delegate;
        this.envPropertyEnricher = envPropertyEnricher;
    }

    @GetMapping
    public EnvironmentFeed environment(@Nullable String pattern) {
        EnvironmentDescriptor originalDescriptor = delegate.environment(pattern);

        return envPropertyEnricher.enrich(originalDescriptor);
    }
}
