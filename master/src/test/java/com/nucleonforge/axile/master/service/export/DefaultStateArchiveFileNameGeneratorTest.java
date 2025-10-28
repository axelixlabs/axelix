package com.nucleonforge.axile.master.service.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.master.model.instance.InstanceId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit test for {@link DefaultStateArchiveFileNameGenerator}.
 *
 * @author Mikhail Polivakha
 */
class DefaultStateArchiveFileNameGeneratorTest {

    private DefaultStateArchiveFileNameGenerator subject;

    @BeforeEach
    void setUp() {
        subject = new DefaultStateArchiveFileNameGenerator();
    }

    @Test
    void shouldGenerateValidFileName() {
        // given.
        String instanceId = "ims-service-k02i302k-od20w";

        // when.
        String filename = subject.generate(InstanceId.of(instanceId));

        // then.
        assertThat(filename).contains(instanceId);
    }
}
