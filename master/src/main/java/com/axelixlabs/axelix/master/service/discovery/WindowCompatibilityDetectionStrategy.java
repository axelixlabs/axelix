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

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.common.utils.SemanticVersion;

/**
 * {@link CompatibilityDetectionStrategy} that enforces the compatibility window: Axelix Master
 * supports starters from the last {@value #WINDOW_SIZE} minor releases, counting its own. A starter
 * is considered incompatible when its major version differs from the Master's one, when it is newer
 * than the Master, or when it is older than the Master by more than {@value #WINDOW_SIZE} minus one
 * minor releases.
 *
 * <p>Only the major and minor components take part in the comparison: patch versions and qualifiers
 * (like {@code -SNAPSHOT}) are irrelevant to the window.
 *
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Component
public class WindowCompatibilityDetectionStrategy implements CompatibilityDetectionStrategy {

    /**
     * The number of minor releases (counting the Master's own) whose starters the Master supports.
     */
    public static final int WINDOW_SIZE = 4;

    private final AxelixVersionDiscoverer axelixVersionDiscoverer;

    public WindowCompatibilityDetectionStrategy(AxelixVersionDiscoverer axelixVersionDiscoverer) {
        this.axelixVersionDiscoverer = axelixVersionDiscoverer;
    }

    @Override
    public boolean isCompatible(String starterVersion) {
        SemanticVersion master = SemanticVersion.parse(axelixVersionDiscoverer.getVersion());

        return SemanticVersion.tryParse(starterVersion)
                .filter(starter -> isCompatible(master, starter))
                .isPresent();
    }

    /**
     * Applies the compatibility window rule only. Both versions are expected to be already successfully parsed:
     * validation of raw, untrusted version strings is the responsibility of {@link #isCompatible(String)}.
     *
     * @return whether the given starter version falls into the compatibility window of the given master version.
     */
    public static boolean isCompatible(SemanticVersion master, SemanticVersion starter) {
        return starter.major() == master.major()
                && starter.minor() <= master.minor()
                && master.minor() - starter.minor() < WINDOW_SIZE;
    }
}
