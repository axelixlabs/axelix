package com.nucleonforge.axile.common.domain.http;

import java.util.Arrays;
import java.util.List;

/**
 * Single http header. Headers are potentially multivalued in http, and
 * therefore we have {@link List} of values here.
 *
 * @author Mikhail Polivakha
 */
public record HttpHeader(String name, List<String> values) {

    public HttpHeader(String name, String... values) {
        this(name, Arrays.stream(values).toList());
    }

    /**
     * @return produced the single {@link String} value for a given HTTP header.
     */
    public String valueAsString() {
        if (values.isEmpty()) {
            return "";
        }

        return String.join(", ", values);
    }
}
