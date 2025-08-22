package com.nucleonforge.axile.common.domain.spring.actuator;

import org.jspecify.annotations.NonNull;

import com.nucleonforge.axile.common.domain.http.HttpMethod;

/**
 * Spring Actuator Endpoint.
 *
 * @param httpMethod the HTTP method by which this actuator endpoint should be reached.
 * @param path the specific path for this actuator endpoint, that follows the {@code /actuator}. For instance, for the
 *      beans endpoint, the path would be {@literal /beans }
 * @author Mikhail Polivakha
 */
public record ActuatorEndpoint(@NonNull String path, @NonNull HttpMethod httpMethod) {

    public boolean isReadOnly() {
        return httpMethod == HttpMethod.GET;
    }
}
