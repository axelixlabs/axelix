package com.nucleonforge.axile.spring.master;

import org.springframework.beans.factory.annotation.Value;

/**
 * Configuration for self-registartion in master.
 *
 * @author Mikhail Polivakha
 */
public class SelfRegistrationConfig {

    // TODO Can we solve it just with configuration properties?
    /**
     * The URL by which this service is reachable.
     */
    @Value("${axile.sbs.registration.instance-id:${spring.application.name:}}")
    @SuppressWarnings("NullAway.Init")
    private String instanceId;

    /**
     * The URL by which this service is reachable from master.
     */
    @Value("${axile.sbs.registration.reachable-by-url:}")
    @SuppressWarnings("NullAway.Init")
    private String reachableByUrl;

    /**
     * The URL of the Axile master deployment that must manage this service.
     */
    @Value("${axile.sbs.registration.master-url:}")
    @SuppressWarnings("NullAway.Init")
    private String masterUrl;
}
