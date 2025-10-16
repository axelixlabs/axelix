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
