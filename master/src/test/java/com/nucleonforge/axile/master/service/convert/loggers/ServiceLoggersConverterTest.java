package com.nucleonforge.axile.master.service.convert.loggers;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.GroupLoggers;
import com.nucleonforge.axile.common.api.loggers.LoggerLoggers;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.api.response.loggers.GroupProfile;
import com.nucleonforge.axile.master.api.response.loggers.LoggerProfile;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ServiceLoggersConverter}
 *
 * @author Sergey Cherkasov
 */
public class ServiceLoggersConverterTest {

    private ServiceLoggersConverter subject;

    @BeforeEach
    void setUp() {
        subject = new ServiceLoggersConverter(new GroupLoggersConverter(), new LoggerLoggersConverter());
    }

    @Test
    void testConvertHappyPath() {
        // when.
        LoggersResponse response = subject.convertInternal(getLoggers());

        // then
        assertThat(response).isNotNull();

        // levels
        assertThat(response.levels())
                .containsExactlyInAnyOrder("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // loggers
        assertThat(response.loggers()).hasSize(3);
        Map<String, LoggerProfile> loggers = response.loggers();

        // loggers -> "ROOT"
        assertThat(loggers.get("ROOT").configuredLevel()).isEqualTo("INFO");
        assertThat(loggers.get("ROOT").effectiveLevel()).isEqualTo("INFO");

        // loggers -> "com.example"
        assertThat(loggers.get("com.example").configuredLevel()).isEqualTo("DEBUG");
        assertThat(loggers.get("com.example").effectiveLevel()).isEqualTo("DEBUG");

        // loggers ->
        assertThat(loggers.get("org").configuredLevel()).isNull();
        assertThat(loggers.get("org").effectiveLevel()).isEqualTo("INFO");

        // groups
        assertThat(response.groups()).hasSize(3);
        Map<String, GroupProfile> groups = response.groups();

        // groups -> "test"
        assertThat(groups.get("test").configuredLevel()).isEqualTo("INFO");
        assertThat(groups.get("test").members()).containsExactlyInAnyOrder("test.member1", "test.member2");

        // groups -> "web"
        assertThat(groups.get("web").configuredLevel()).isNull();
        assertThat(groups.get("web").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.core.codec",
                        "org.springframework.http",
                        "org.springframework.web",
                        "org.springframework.boot.actuate.endpoint.web",
                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans");

        // groups -> "sql"
        assertThat(groups.get("sql").configuredLevel()).isNull();
        assertThat(groups.get("sql").members())
                .containsExactlyInAnyOrder(
                        "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener");
    }

    private static ServiceLoggers getLoggers() {
        // levels
        List<String> levels = List.of("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // loggers
        Map<String, LoggerLoggers> loggers = Map.of(
                "ROOT",
                new LoggerLoggers("INFO", "INFO"),
                "com.example",
                new LoggerLoggers("DEBUG", "DEBUG"),
                "org",
                new LoggerLoggers(null, "INFO"));

        // groups
        Map<String, GroupLoggers> groups = Map.of(
                "test", new GroupLoggers("INFO", List.of("test.member1", "test.member2")),
                "web",
                        new GroupLoggers(
                                null,
                                List.of(
                                        "org.springframework.core.codec",
                                        "org.springframework.http",
                                        "org.springframework.web",
                                        "org.springframework.boot.actuate.endpoint.web",
                                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans")),
                "sql",
                        new GroupLoggers(
                                null,
                                List.of(
                                        "org.springframework.jdbc.core",
                                        "org.hibernate.SQL",
                                        "org.jooq.tools.LoggerListener")));

        return new ServiceLoggers(levels, loggers, groups);
    }
}
