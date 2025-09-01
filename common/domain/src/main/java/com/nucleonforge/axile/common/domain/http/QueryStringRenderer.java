package com.nucleonforge.axile.common.domain.http;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for working with HTTP query parameters.
 *
 * @author Mikhail Polivakha
 */
public class QueryStringRenderer {

    /**
     * Renders the array of {@link QueryParameter query paramters} to the query parameter string.
     *
     * @return the rendered query parameters string, e.g. {@code ?key=v1&key2=v2}
     */
    public static String renderQueryString(List<QueryParameter<?>> queryParameters) {
        if (queryParameters.isEmpty()) {
            return "";
        }

        return queryParameters.stream().map(QueryParameter::asString).collect(Collectors.joining("&", "?", ""));
    }
}
