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
package com.axelixlabs.axelix.master.service.discovery;

import java.util.Objects;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.master.utils.VersionTrimmer;

/**
 * {@link CompatibilityDetectionStrategy} that considers the provided version of the starter being
 * compatible by looking at the major version solely.
 *
 * @author Mikhail Polivakha
 */
@Component
public class MajorVersionCompatibilityDetectionStrategy implements CompatibilityDetectionStrategy {

    private final AxelixVersionDiscoverer axelixVersionDiscoverer;

    public MajorVersionCompatibilityDetectionStrategy(AxelixVersionDiscoverer axelixVersionDiscoverer) {
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
    }

    @Override
    public boolean isCompatible(String starterVersion) {
        String starterMajor = VersionTrimmer.getMajorVersion(starterVersion);
        String masterMajor = VersionTrimmer.getMajorVersion(axelixVersionDiscoverer.getVersion());

        return Objects.equals(starterMajor, masterMajor);
    }
}
