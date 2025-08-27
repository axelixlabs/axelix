package com.nucleonforge.axile.common.domain.http;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link HttpPayload}.
 *
 * @since 02.09.2025
 * @author Nikita Kirillov
 */
public record DefaultHttpPayload(
        List<HttpHeader> headers,
        List<QueryParameter<?>> queryParameters,
        Map<String, String> pathVariableValues,
        byte[] requestBody)
        implements HttpPayload {

    public DefaultHttpPayload(Map<String, String> pathVariableValues) {
        this(Collections.emptyList(), Collections.emptyList(), pathVariableValues, new byte[0]);
    }
}
