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
package com.axelixlabs.axelix.sbs.spring.autoconfiguration;

import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.axelixlabs.axelix.sbs.spring.core.sbom.AxelixDependenciesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.sbom.ClasspathDependencySbom;

/**
 * Auto-configuration class for the dependency SBOM custom actuator endpoint.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
@ConditionalOnAvailableEndpoint(endpoint = AxelixDependenciesEndpoint.class)
public class AxelixDependenciesEndpointAutoConfiguration {

    @Bean
    public ClasspathDependencySbom classpathDependencySbom() {
        return new ClasspathDependencySbom();
    }

    @Bean
    public AxelixDependenciesEndpoint axelixDependenciesEndpoint(ClasspathDependencySbom classpathDependencySbom) {
        return new AxelixDependenciesEndpoint(classpathDependencySbom);
    }
}
