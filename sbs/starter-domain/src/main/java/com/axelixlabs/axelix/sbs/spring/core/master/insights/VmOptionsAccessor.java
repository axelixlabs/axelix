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
package com.axelixlabs.axelix.sbs.spring.core.master.insights;

import java.util.List;

/**
 * Read-only accessor over the JVM launch options of the current process.
 *
 * <p>The accessor is intended to work with the output of
 * {@code ManagementFactory.getRuntimeMXBean().getInputArguments()} and focuses on
 * HotSpot advanced options that use the {@code -XX:} prefix.
 *
 * @author Mikhail Polivakha
 */
public class VmOptionsAccessor {

    private static final String ADVANCED_FVM_FLAG_PREFIX = "-XX:";

    private final List<String> vmOptions;

    /**
     * Creates a new accessor for the given VM options.
     *
     * @param vmOptions the JVM input arguments of the current process
     */
    public VmOptionsAccessor(List<String> vmOptions) {
        this.vmOptions = vmOptions;
    }

    /**
     * Checks whether a boolean HotSpot advanced option is explicitly enabled.
     *
     * <p>This method matches options in the {@code -XX:+FlagName} / {@code -XX:-FlagName}
     * form. The first matching {@code -XX:} option that contains the given {@code flag}
     * determines the result based on the sign immediately following the {@code -XX:} prefix.
     *
     * @param flag the flag name without the {@code -XX:+/-} prefix, for example {@code UseCompactObjectHeaders}.
     * @return {@code true} when the first matching option uses the {@code +} form, otherwise {@code false}.
     */
    public boolean isAdvancedFeatureEnabled(String flag) {
        for (String vmOption : vmOptions) {
            if (vmOption.contains(flag) && vmOption.startsWith(ADVANCED_FVM_FLAG_PREFIX)) {
                char statusSymbol = vmOption.charAt(ADVANCED_FVM_FLAG_PREFIX.length());
                return statusSymbol == '+';
            }
        }
        return false;
    }

    /**
     * Checks whether a HotSpot advanced option with the given name is present.
     *
     * <p>This method matches options in the {@code -XX:FlagName[=value]} form, for example
     * {@code -XX:SharedArchiveFile=/path/to/archive.jsa} or {@code -XX:AOTCache=/path/to/cache}.
     *
     * @param flag the flag name without the {@code -XX:} prefix, for example {@code SharedArchiveFile}.
     * @return {@code true} when any VM option starts with {@code -XX:FlagName}.
     */
    public boolean isAdvancedFeatureSpecified(String flag) {
        for (String vmOption : vmOptions) {
            if (vmOption.startsWith(ADVANCED_FVM_FLAG_PREFIX + flag)) {
                return true;
            }
        }

        return false;
    }
}
