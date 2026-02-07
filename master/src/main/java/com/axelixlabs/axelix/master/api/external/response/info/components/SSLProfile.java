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
package com.axelixlabs.axelix.master.api.external.response.info.components;

import java.util.Collections;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.jspecify.annotations.Nullable;

/**
 * The profile of a given SSL.
 *
 * @param bundles    The bundles information of the SSL.
 *
 * @author Sergey Cherkasov
 */
public record SSLProfile(Set<Bundles> bundles) {

    public SSLProfile {
        if (bundles == null) {
            bundles = Collections.emptySet();
        }
    }

    /**
     * The profile of a given bundle.
     *
     * @param name               The name of the SSL bundle.
     * @param certificateChains  The certificate chains in the bundle.
     *
     * @author Sergey Cherkasov
     */
    public record Bundles(String name, Set<CertificateChains> certificateChains) {

        public Bundles {
            if (certificateChains == null) {
                certificateChains = Collections.emptySet();
            }
        }

        /**
         * The profile of a given certificate chain.
         *
         * @param alias         The alias of the certificate chain.
         * @param certificates  The certificates in the chain.
         *
         * @author Sergey Cherkasov
         */
        public record CertificateChains(String alias, Set<Certificates> certificates) {

            public CertificateChains {
                if (certificates == null) {
                    certificates = Collections.emptySet();
                }
            }

            /**
             * The profile of a given certificate.
             *
             * @param version                 The version of the certificate.
             * @param issuer                  The issuer of the certificate.
             * @param validity                The certificate validity information.
             * @param subject                 The subject of the certificate.
             * @param serialNumber            The serial number of the certificate.
             * @param signatureAlgorithmName  The signature algorithm name.
             * @param validityStarts          The validity start date of the certificate.
             * @param validityEnds            The validity end date of the certificate.
             *
             * @author Sergey Cherkasov
             */
            @JsonInclude(JsonInclude.Include.NON_NULL)
            public record Certificates(
                    String version,
                    String issuer,
                    @Nullable Validity validity,
                    String subject,
                    String serialNumber,
                    String signatureAlgorithmName,
                    String validityStarts,
                    String validityEnds) {

                /**
                 * The profile of a given certificate validity.
                 *
                 * @param status  The certificate validity status.
                 *
                 * @author Sergey Cherkasov
                 */
                public record Validity(String status) {}
            }
        }
    }
}
