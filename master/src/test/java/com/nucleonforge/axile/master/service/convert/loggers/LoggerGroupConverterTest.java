package com.nucleonforge.axile.master.service.convert.loggers;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfileResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoggerGroupConverter}
 *
 * @author Sergey Cherkasov
 */
public class LoggerGroupConverterTest {
    private final LoggerGroupConverter subject = new LoggerGroupConverter();

    @Test
    void testConvertHappyPath() {
        List<String> members = List.of(
                "org.springframework.core.codec",
                "org.springframework.http",
                "org.springframework.web",
                "org.springframework.boot.actuate.endpoint.web",
                "org.springframework.boot.web.servlet.ServletContextInitializerBeans");
        LoggerGroup group = new LoggerGroup("web", members);

        // when.
        GroupProfileResponse request = subject.convertInternal(group);

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
