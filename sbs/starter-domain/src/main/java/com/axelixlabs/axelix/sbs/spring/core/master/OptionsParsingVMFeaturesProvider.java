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
package com.axelixlabs.axelix.sbs.spring.core.master;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;

import com.axelixlabs.axelix.common.api.registration.BasicDiscoveryMetadata.VMFeature;

/**
 * {@link VMFeaturesProvider} based on the scanning the virtual machine launch options.
 *
 * @author Mikhail Polivakha
 */
public class OptionsParsingVMFeaturesProvider implements VMFeaturesProvider {

    private static final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    @Override
    public List<VMFeature> discover() {
        List<String> inputArguments = runtimeMXBean.getInputArguments();

        List<VMFeature> features = new ArrayList<>();

        // Check for AppCDS (Application Class Data Sharing)
        features.add(getAppCdsFeature(inputArguments));

        int javaVersion = Runtime.version().feature();

        if (javaVersion >= 24) {
            // Check for AotCache (Ahead-of-Time Cache)
            features.add(getAotCacheEnabled(inputArguments));

            // Check for Compressed Object Headers (Project Lilliput)
            features.add(getCompressedObjectHeadersEnabled(inputArguments));
        }

        return features;
    }

    private VMFeature getAppCdsFeature(List<String> inputArguments) {
        boolean enabled = false;

        for (String arg : inputArguments) {
            // Check if explicitly disabled (highest priority)
            if (arg.equals("-Xshare:off")) {
                enabled = false;
                break;
            }

            // Check for SharedArchiveFile option (indicates AppCDS usage)
            if (arg.startsWith("-XX:SharedArchiveFile=")) {
                enabled = true;
            }
        }

        return new VMFeature(
                "AppCDS",
                "Application Class Data Sharing - allows for loading pre-processed class"
                        + " structures from shared archive to reduce memory footprint adn startup time",
                enabled);
    }

    private VMFeature getAotCacheEnabled(List<String> inputArguments) {
        boolean enabled = false;

        for (String arg : inputArguments) {
            // Check for AOTLibrary option (indicates AOT usage)
            if (arg.startsWith("-XX:AOTCache=")) {
                enabled = true;
                break;
            }
        }

        return new VMFeature(
                "AotCache", "Ahead-of-Time Cache - enables AOT compilation cache for faster startup", enabled);
    }

    private VMFeature getCompressedObjectHeadersEnabled(List<String> inputArguments) {
        boolean enabled = false;

        for (String arg : inputArguments) {
            // Check for explicit enable flag
            if (arg.equals("-XX:+UseCompactObjectHeaders")) {
                enabled = true;
                break;
            }
        }

        return new VMFeature(
                "CompressedObjectHeaders",
                "Compressed Object Headers (Project Lilliput) - reduces object header size for memory efficiency",
                enabled);
    }
}
