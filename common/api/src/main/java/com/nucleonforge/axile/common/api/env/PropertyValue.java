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
package com.nucleonforge.axile.common.api.env;

import org.jspecify.annotations.Nullable;

/**
 * The value of a property within a property source.
 *
 * @param value  the string representation of the property's value
 * @param origin the origin of the property if available (e.g. location in a file), may be {@code null}
 *
 * @see EnvironmentProperty
 * @since 03.09.2025
 * @author Nikita Kirillov
 */
public record PropertyValue(String value, @Nullable String origin) {}
