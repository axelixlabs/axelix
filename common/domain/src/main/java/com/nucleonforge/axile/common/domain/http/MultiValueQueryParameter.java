package com.nucleonforge.axile.common.domain.http;

import java.util.List;

/**
 * The {@link QueryParameter} that can have multiple values. Renders to a
 * comma separated values string, e.g. {@code key=value1,value2,value3}. The correct
 * way to pass multivalued params in HTTP is not defined in any kind of RFC standard,
 * so we're just choosing this option.
 *
 * @author Mikhail Polivakha
 */
public record MultiValueQueryParameter(String key, List<String> values) implements QueryParameter<String> {

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return String.join(",", values);
    }
}
