package com.nucleonforge.axile.common.domain.spring.actuator;

import com.nucleonforge.axile.common.domain.http.HttpMethod;
import com.nucleonforge.axile.common.domain.http.HttpUrl;

/**
 * Spring Actuator Endpoint.
 *
 * @param httpMethod the HTTP method by which this actuator endpoint should be reached.
 * @param path the specific path for this actuator endpoint, that follows the {@code /actuator}. For instance, for the
 *      beans endpoint, the path would be {@literal /beans}
 * @author Mikhail Polivakha
 */
public record ActuatorEndpoint(HttpUrl path, HttpMethod httpMethod) {

    public static ActuatorEndpoint of(String path, HttpMethod httpMethod) {
        HttpUrl httpUrl = new HttpUrl(path);
        return new ActuatorEndpoint(httpUrl, httpMethod);
    }
}
