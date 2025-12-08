/*
 * Copyright 2025-present the original author or authors.
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
package com.nucleonforge.axile.master.api.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.api.env.EnvironmentProperty;

/**
 * The response object that represents an environment property in the application.
 *
 * @param source the name of the property source from which this value was obtained
 * @param value the string representation of the property's value
 *
 * @see EnvironmentProperty
 * @since 27.08.2025
 * @author Nikita Kirillov
 */
public record EnvironmentPropertyResponse(String source, String value, List<PropertySource> propertySources) {

    public record PropertySource(String name, @Nullable Property property) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record Property(String value, @Nullable String origin) {}
}
