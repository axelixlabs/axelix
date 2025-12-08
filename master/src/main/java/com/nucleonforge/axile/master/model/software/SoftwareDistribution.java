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
package com.nucleonforge.axile.master.model.software;

/**
 * The specific Software Distribution, i.e. specific version of the {@link SoftwareComponent}.
 *
 * @param softwareComponent the piece of software that the current distribution represents.
 * @param version the specific version/distribution of the given {@link SoftwareComponent}. Can be any
 *        {@link String} like {@code 1.6.2} for library or {@code Amazon inc.} for JDK
 * @author Mikhail Polivakha
 */
public record SoftwareDistribution(SoftwareComponent softwareComponent, String version) {}
