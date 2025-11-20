package com.nucleonforge.axile.common.domain.http;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MultiValueQueryParameter}.
 *
 * @author Mikhail Polivakha
 */
class MultiValueQueryParameterTest {

    @Test
    void shouldRenderCorrectMultiValuesParameter() {
        // given.
        var subject = new MultiValueQueryParameter("key", List.of("value1", "value2", "value3"));

        // when.
        String rendered = subject.asString();

        // then.
        Assertions.assertThat(rendered).isEqualTo("key=value1,value2,value3");
    }
}
