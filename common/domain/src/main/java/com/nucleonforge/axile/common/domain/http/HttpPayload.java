package com.nucleonforge.axile.common.domain.http;

import java.util.List;
import java.util.Map;

/**
 * The payload of the http requests, consisting of body and headers.
 *
 * @author Mikhail Polivakha
 */
public interface HttpPayload {

    /**
     * @return the ordered collection of {@link HttpHeader http headers}.
     */
    List<HttpHeader> headers();

    default boolean hasHeaders() {
        return !headers().isEmpty();
    }

    /**
     * @return the list of {@link QueryParameter query parameters}.
     */
    List<QueryParameter<?>> queryParameters();

    /**
     * @return map of path variable names to path variable values.
     */
    Map<String, String> pathVariableValues();

    /**
     * @return the serialized body of the http request.
     */
    byte[] requestBody();

    default boolean hasBody() {
        byte[] bytes = requestBody();

        return bytes.length != 0;
    }
}
