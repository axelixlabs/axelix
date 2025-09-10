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
