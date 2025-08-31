package com.nucleonforge.axile.common.domain.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link HttpHeader}.
 *
 * @author Mikhail Polivakha
 */
class HttpHeaderTest {

    @Test
    void testHttpHeaderValueRendering() {
        // given.
        var sut = new HttpHeader("Accept", "text/xml; chatrset=utf-8", "application/json; q=0.8");

        // when.
        String value = sut.valueAsString();

        // then.
        assertThat(value).isEqualTo("text/xml; chatrset=utf-8, application/json; q=0.8");
    }
}
