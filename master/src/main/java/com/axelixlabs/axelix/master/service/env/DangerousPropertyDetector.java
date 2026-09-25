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
package com.axelixlabs.axelix.master.service.env;

import java.util.Map;

import com.axelixlabs.axelix.master.api.external.response.env.DangerousProperty;
import com.axelixlabs.axelix.master.contract.env.EnvironmentFeed;
import com.axelixlabs.axelix.master.contract.env.Property;

/**
 * Finds the properties of the environment whose values are listed in {@link DangerousProperty}.
 *
 * @author Sergey Cherkasov
 */
public interface DangerousPropertyDetector {

    /**
     * A property may be defined in several property sources, but only the value from the first of them is in
     * effect, the same way Spring Boot resolves it (see
     * <a href="https://docs.spring.io/spring-boot/reference/features/external-config.html">Externalized
     * Configuration</a>), so only this value is checked. Sanitized values are skipped, since their real value is
     * unknown.
     *
     * @param feed the environment feed to check.
     * @return the dangerous properties mapped to the matching {@link DangerousProperty}. The keys are the
     *         {@link Property} instances of the given feed, compared by identity, since the same property with the
     *         same value may be present in several sources.
     */
    Map<Property, DangerousProperty> detect(EnvironmentFeed feed);
}
