package com.nucleonforge.axile.master.service.convert;

import org.assertj.core.api.Assertions;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.nucleonforge.axile.master.api.response.InstancesGridResponse.InstanceShortProfile;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.convert.response.InstancesToShortProfileConverter;

/**
 * Unit tests for {@link InstancesToShortProfileConverter}.
 *
 * @author Mikhail Polivakha
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = InstancesToShortProfileConverter.class)
class InstancesToShortProfileConverterTest {

    @Autowired
    private InstancesToShortProfileConverter subject;

    @Test
    void shouldConvertInstanceToItsShortProfile() {
        // given.
        Instance input = Instancio.create(Instance.class);

        // when.
        InstanceShortProfile result = subject.convert(input);

        // then.
        Assertions.assertThat(result)
                .usingRecursiveComparison()
                .ignoringFields("deployedFor", "instanceId")
                .isEqualTo(input);
    }
}
