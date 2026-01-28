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
package com.axelixlabs.axelix.common.api.info.components;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.domain.spring.actuator.ActuatorEndpoint;

/**
 * DTO that encapsulates the SSL information of the given artifact.
 *
 * @see ActuatorEndpoint
 * @apiNote <a href="https://docs.spring.io/spring-boot/api/rest/actuator/info.html">Info Endpoint</a>
 * @author Sergey Cherkasov
 */
public record SSLInfo(@JsonProperty("bundles") @Nullable Set<Bundles> bundles) {

    public record Bundles(
            @JsonProperty("name") String name,
            @JsonProperty("certificateChains") @Nullable Set<CertificateChains> certificateChains) {

        public record CertificateChains(
                @JsonProperty("alias") String alias,
                @JsonProperty("certificates") @Nullable Set<Certificates> certificates) {

            public record Certificates(
                    @JsonProperty("version") String version,
                    @JsonProperty("issuer") String issuer,
                    @JsonProperty("validity") @Nullable Validity validity,
                    @JsonProperty("subject") String subject,
                    @JsonProperty("serialNumber") String serialNumber,
                    @JsonProperty("signatureAlgorithmName") String signatureAlgorithmName,
                    @JsonProperty("validityStarts") String validityStarts,
                    @JsonProperty("validityEnds") String validityEnds) {

                public record Validity(@JsonProperty("status") String status) {}
            }
        }
    }
}
