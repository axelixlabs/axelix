package com.nucleonforge.axile.sbs.metrics;

/**
 * Exporter of the JVM Code Cache related metrics.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public interface CodeCacheExporter {

    /**
     * Code cache actively used right now
     */
    MemoryValue used();

    /**
     * Code cache available
     */
    MemoryValue available();

    /**
     * Code cache initial value
     */
    MemoryValue initial();

    /**
     * Code cache maximum value
     */
    MemoryValue maximum();
}
