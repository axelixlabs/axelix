package com.nucleonforge.axile.sbs.spring.integrations.http;

import com.nucleonforge.axile.sbs.spring.integrations.AbstractIntegration;

/**
 * Represents an HTTP-based integration with an external service.
 *
 * @since 05.07.2025
 * @author Mikhail Polivakha
 */
public final class HttpIntegration extends AbstractIntegration {

    public HttpIntegration(String networkAddress, HttpVersion httpVersion) {
        this(networkAddress, httpVersion, "External HTTP Service");
    }

    public HttpIntegration(String networkAddress, HttpVersion httpVersion, String serviceName) {
        super(networkAddress, httpVersion.getDisplay(), serviceName);
    }
}
