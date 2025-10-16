package com.nucleonforge.axile.sbs.spring.integrations.http;

/**
 * Version of an HTTP protocol in use.
 *
 * @since 05.07.25
 * @author Mikhail Polivakha
 */
public enum HttpVersion {
    V1_0("HTTP 1/0"),
    V1_1("HTTP 1/1"),
    V2_0("HTTP 2/0"),
    V3_0("HTTP 3/0"),
    ;

    private final String display;

    HttpVersion(String display) {
        this.display = display;
    }

    public String getDisplay() {
        return display;
    }
}
