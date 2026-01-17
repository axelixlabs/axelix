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
package com.nucleonforge.axelix.sbs.metrics;

import java.math.BigInteger;

/**
 *  A {@link MetricValue} implementation that represents a memory size in bytes.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public class MemoryValue extends AbstractMetric<BigInteger> {

    MemoryValue(BigInteger value, String display, String alarmDescription) {
        super(value, display, alarmDescription);
    }

    MemoryValue(BigInteger value, String display) {
        super(value, display);
    }

    /**
     * Creates fine {@link MemoryValue} from the specified number of megabytes.
     *
     * @param megabytes the memory size in megabytes
     * @return a {@code MemoryValue} instance representing the equivalent in bytes
     */
    public static MemoryValue fineMegabytes(int megabytes) {
        return new MemoryValue(toBytes(megabytes), "%d Mb".formatted(megabytes));
    }

    /**
     * Creates an alarming {@link MemoryValue} from the specified number of megabytes.
     *
     * @param megabytes the memory size in megabytes
     * @return a {@code MemoryValue} instance representing the equivalent in bytes
     */
    public static MemoryValue alarmMegabytes(int megabytes, String alarmDescription) {
        return new MemoryValue(toBytes(megabytes), "%d Mb".formatted(megabytes), alarmDescription);
    }

    private static BigInteger toBytes(int megabytes) {
        return BigInteger.valueOf(megabytes).multiply(BigInteger.valueOf(1024)).multiply(BigInteger.valueOf(1024));
    }
}
