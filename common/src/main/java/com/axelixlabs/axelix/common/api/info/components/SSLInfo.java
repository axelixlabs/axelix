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

import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jspecify.annotations.Nullable;

/**
 * DTO that encapsulates the SSL information of the given artifact.
 *
 * @author Sergey Cherkasov
 */
public final class SSLInfo {

    @Nullable
    private final Set<Bundles> bundles;

    public SSLInfo(@JsonProperty("bundles") @Nullable Set<Bundles> bundles) {
        this.bundles = bundles;
    }

    @Nullable
    public Set<Bundles> bundles() {
        return bundles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SSLInfo sslInfo = (SSLInfo) o;
        return Objects.equals(bundles, sslInfo.bundles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bundles);
    }

    @Override
    public String toString() {
        return "SSLInfo{" + "bundles=" + bundles + '}';
    }

    public static final class Bundles {

        private final String name;

        @Nullable
        private final Set<CertificateChains> certificateChains;

        public Bundles(
                @JsonProperty("name") String name,
                @JsonProperty("certificateChains") @Nullable Set<CertificateChains> certificateChains) {
            this.name = name;
            this.certificateChains = certificateChains;
        }

        public String name() {
            return name;
        }

        @Nullable
        public Set<CertificateChains> certificateChains() {
            return certificateChains;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Bundles bundles = (Bundles) o;
            return Objects.equals(name, bundles.name) && Objects.equals(certificateChains, bundles.certificateChains);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, certificateChains);
        }

        @Override
        public String toString() {
            return "Bundles{" + "name='" + name + '\'' + ", certificateChains=" + certificateChains + '}';
        }

        public static final class CertificateChains {

            private final String alias;

            @Nullable
            private final Set<Certificates> certificates;

            public CertificateChains(
                    @JsonProperty("alias") String alias,
                    @JsonProperty("certificates") @Nullable Set<Certificates> certificates) {
                this.alias = alias;
                this.certificates = certificates;
            }

            public String alias() {
                return alias;
            }

            @Nullable
            public Set<Certificates> certificates() {
                return certificates;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || getClass() != o.getClass()) {
                    return false;
                }
                CertificateChains that = (CertificateChains) o;
                return Objects.equals(alias, that.alias) && Objects.equals(certificates, that.certificates);
            }

            @Override
            public int hashCode() {
                return Objects.hash(alias, certificates);
            }

            @Override
            public String toString() {
                return "CertificateChains{" + "alias='" + alias + '\'' + ", certificates=" + certificates + '}';
            }

            public static final class Certificates {

                private final String version;
                private final String issuer;

                @Nullable
                private final Validity validity;

                private final String subject;
                private final String serialNumber;
                private final String signatureAlgorithmName;
                private final String validityStarts;
                private final String validityEnds;

                public Certificates(
                        @JsonProperty("version") String version,
                        @JsonProperty("issuer") String issuer,
                        @JsonProperty("validity") @Nullable Validity validity,
                        @JsonProperty("subject") String subject,
                        @JsonProperty("serialNumber") String serialNumber,
                        @JsonProperty("signatureAlgorithmName") String signatureAlgorithmName,
                        @JsonProperty("validityStarts") String validityStarts,
                        @JsonProperty("validityEnds") String validityEnds) {
                    this.version = version;
                    this.issuer = issuer;
                    this.validity = validity;
                    this.subject = subject;
                    this.serialNumber = serialNumber;
                    this.signatureAlgorithmName = signatureAlgorithmName;
                    this.validityStarts = validityStarts;
                    this.validityEnds = validityEnds;
                }

                public String version() {
                    return version;
                }

                public String issuer() {
                    return issuer;
                }

                @Nullable
                public Validity validity() {
                    return validity;
                }

                public String subject() {
                    return subject;
                }

                public String serialNumber() {
                    return serialNumber;
                }

                public String signatureAlgorithmName() {
                    return signatureAlgorithmName;
                }

                public String validityStarts() {
                    return validityStarts;
                }

                public String validityEnds() {
                    return validityEnds;
                }

                @Override
                public boolean equals(Object o) {
                    if (this == o) {
                        return true;
                    }
                    if (o == null || getClass() != o.getClass()) {
                        return false;
                    }
                    Certificates that = (Certificates) o;
                    return Objects.equals(version, that.version)
                            && Objects.equals(issuer, that.issuer)
                            && Objects.equals(validity, that.validity)
                            && Objects.equals(subject, that.subject)
                            && Objects.equals(serialNumber, that.serialNumber)
                            && Objects.equals(signatureAlgorithmName, that.signatureAlgorithmName)
                            && Objects.equals(validityStarts, that.validityStarts)
                            && Objects.equals(validityEnds, that.validityEnds);
                }

                @Override
                public int hashCode() {
                    return Objects.hash(
                            version,
                            issuer,
                            validity,
                            subject,
                            serialNumber,
                            signatureAlgorithmName,
                            validityStarts,
                            validityEnds);
                }

                @Override
                public String toString() {
                    return "Certificates{"
                            + "version='"
                            + version
                            + '\''
                            + ", issuer='"
                            + issuer
                            + '\''
                            + ", validity="
                            + validity
                            + ", subject='"
                            + subject
                            + '\''
                            + ", serialNumber='"
                            + serialNumber
                            + '\''
                            + ", signatureAlgorithmName='"
                            + signatureAlgorithmName
                            + '\''
                            + ", validityStarts='"
                            + validityStarts
                            + '\''
                            + ", validityEnds='"
                            + validityEnds
                            + '\''
                            + '}';
                }

                public static final class Validity {

                    private final String status;

                    public Validity(@JsonProperty("status") String status) {
                        this.status = status;
                    }

                    public String status() {
                        return status;
                    }

                    @Override
                    public boolean equals(Object o) {
                        if (this == o) {
                            return true;
                        }
                        if (o == null || getClass() != o.getClass()) {
                            return false;
                        }
                        Validity validity = (Validity) o;
                        return Objects.equals(status, validity.status);
                    }

                    @Override
                    public int hashCode() {
                        return Objects.hash(status);
                    }

                    @Override
                    public String toString() {
                        return "Validity{" + "status='" + status + '\'' + '}';
                    }
                }
            }
        }
    }
}
