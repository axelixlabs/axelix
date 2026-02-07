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
package com.axelixlabs.axelix.master.service.convert.response.info.components;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.api.info.components.SSLInfo;
import com.axelixlabs.axelix.master.api.external.response.info.components.SSLProfile;
import com.axelixlabs.axelix.master.service.convert.response.Converter;

/**
 * The {@link Converter} from {@link SSLInfo} to {@link SSLProfile}.
 *
 * @author Sergey Cherkasov
 */
@Service
public class SSLInfoConverter implements Converter<SSLInfo, SSLProfile> {

    @Override
    public @NonNull SSLProfile convertInternal(@NonNull SSLInfo source) {
        Set<SSLProfile.Bundles> bundles = getAllBundles(source);

        return new SSLProfile(bundles);
    }

    private Set<SSLProfile.Bundles> getAllBundles(SSLInfo source) {
        Set<SSLInfo.Bundles> bundles = source.bundles();
        if (bundles == null || bundles.isEmpty()) {
            return Collections.emptySet();
        }

        return bundles.stream().map(this::convertBundle).collect(Collectors.toSet());
    }

    private SSLProfile.Bundles convertBundle(SSLInfo.Bundles bundle) {
        Set<SSLProfile.Bundles.CertificateChains> chains = getAllCertificateChains(bundle);

        return new SSLProfile.Bundles(bundle.name(), chains);
    }

    private Set<SSLProfile.Bundles.CertificateChains> getAllCertificateChains(SSLInfo.Bundles bundle) {
        Set<SSLInfo.Bundles.CertificateChains> certificateChains = bundle.certificateChains();
        if (certificateChains == null || certificateChains.isEmpty()) {
            return Collections.emptySet();
        }

        return certificateChains.stream().map(this::convertCertificateChain).collect(Collectors.toSet());
    }

    private SSLProfile.Bundles.CertificateChains convertCertificateChain(SSLInfo.Bundles.CertificateChains chain) {
        Set<SSLProfile.Bundles.CertificateChains.Certificates> certificates = getAllCertificates(chain);

        return new SSLProfile.Bundles.CertificateChains(chain.alias(), certificates);
    }

    private Set<SSLProfile.Bundles.CertificateChains.Certificates> getAllCertificates(
            SSLInfo.Bundles.CertificateChains chain) {
        Set<SSLInfo.Bundles.CertificateChains.Certificates> certificates = chain.certificates();
        if (certificates == null || certificates.isEmpty()) {
            return Collections.emptySet();
        }

        return certificates.stream().map(this::convertCertificate).collect(Collectors.toSet());
    }

    private SSLProfile.Bundles.CertificateChains.Certificates convertCertificate(
            SSLInfo.Bundles.CertificateChains.Certificates cert) {
        SSLProfile.Bundles.CertificateChains.Certificates.Validity validity = convertValidity(cert);

        return new SSLProfile.Bundles.CertificateChains.Certificates(
                cert.version(),
                cert.issuer(),
                validity,
                cert.subject(),
                cert.serialNumber(),
                cert.signatureAlgorithmName(),
                cert.validityStarts(),
                cert.validityEnds());
    }

    private SSLProfile.Bundles.CertificateChains.Certificates.@Nullable Validity convertValidity(
            SSLInfo.Bundles.CertificateChains.Certificates cert) {
        SSLInfo.Bundles.CertificateChains.Certificates.Validity validity = cert.validity();
        if (validity != null) {
            return new SSLProfile.Bundles.CertificateChains.Certificates.Validity(validity.status());
        }

        return null;
    }
}
