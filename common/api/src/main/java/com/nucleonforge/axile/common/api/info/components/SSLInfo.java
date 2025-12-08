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
package com.nucleonforge.axile.common.api.info.components;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

import com.nucleonforge.axile.common.domain.spring.actuator.ActuatorEndpoint;

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
