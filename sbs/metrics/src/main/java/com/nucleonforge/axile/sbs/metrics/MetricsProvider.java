package com.nucleonforge.axile.sbs.metrics;

/**
 * Base contract for classes that provide metric data.
 *
 * <p>Implementations of this abstract class should define how to collect and return
 * a snapshot of system, application, or infrastructure metrics using a {@link Metrics} object.
 *
 * @since 23.06.2025
 * @author Mikhail Polivakha
 */
public abstract class MetricsProvider {

    /**
     * Collects and returns a snapshot of metrics.
     *
     * @return a {@link Metrics} instance containing the collected metrics
     */
    public abstract Metrics scratch();
}
