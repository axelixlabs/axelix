package com.nucleonforge.axile.master.api.response.info.components;

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
