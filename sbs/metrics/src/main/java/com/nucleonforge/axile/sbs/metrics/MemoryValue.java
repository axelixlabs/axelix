/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.metrics;

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
