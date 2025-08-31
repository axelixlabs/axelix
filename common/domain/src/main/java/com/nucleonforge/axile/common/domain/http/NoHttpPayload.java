package com.nucleonforge.axile.common.domain.http;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link HttpPayload} that does not add any
 * additional headers into the HTTP request and also that does not
 * provide any request body.
 *
 * @author Mikhail Polivakha
 */
public class NoHttpPayload implements HttpPayload {

    public static NoHttpPayload INSTANCE = new NoHttpPayload();

    private NoHttpPayload() {}

    @Override
    public List<HttpHeader> headers() {
        return List.of();
    }

    @Override
    public List<QueryParameter<?>> queryParameters() {
        return List.of();
    }

    @Override
    public Map<String, String> pathVariableValues() {
        return Map.of();
    }

    @Override
    public byte[] requestBody() {
        return new byte[0];
    }
}
