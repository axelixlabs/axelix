package com.nucleonforge.axile.common.domain.http;

/**
 * The most regular type of the parameter - single parameter http query
 * parameter, for instance {@code ?first=value} or {@code ?second=123}.
 *
 * @author Mikhail Polivakha
 */
public record SingleValueQueryParameter(String key, String value) implements QueryParameter<String> {

    @Override
    public String key() {
        return key;
    }

    @Override
    public String value() {
        return value;
    }
}
