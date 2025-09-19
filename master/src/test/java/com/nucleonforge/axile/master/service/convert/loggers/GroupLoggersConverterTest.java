package com.nucleonforge.axile.master.service.convert.loggers;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.GroupLoggers;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfile;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GroupLoggersConverter}
 *
 * @author Sergey Cherkasov
 */
public class GroupLoggersConverterTest {
    private final GroupLoggersConverter subject = new GroupLoggersConverter();

    @Test
    void testConvertHappyPath() {
        List<String> members = List.of(
                "org.springframework.core.codec",
                "org.springframework.http",
                "org.springframework.web",
                "org.springframework.boot.actuate.endpoint.web",
                "org.springframework.boot.web.servlet.ServletContextInitializerBeans");
        GroupLoggers group = new GroupLoggers("web", members);

        // when.
        GroupProfile request = subject.convertInternal(group);

        // then.
        assertThat(request.configuredLevel()).isEqualTo("web");
        assertThat(request.members())
                .containsExactlyInAnyOrder(
                        "org.springframework.core.codec",
                        "org.springframework.http",
                        "org.springframework.web",
                        "org.springframework.boot.actuate.endpoint.web",
                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans");
    }
}
