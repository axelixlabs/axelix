package com.nucleonforge.axile.master.service.convert.loggers;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.nucleonforge.axile.common.api.loggers.LoggerGroup;
import com.nucleonforge.axile.common.api.loggers.LoggerLevels;
import com.nucleonforge.axile.common.api.loggers.ServiceLoggers;
import com.nucleonforge.axile.master.api.response.loggers.LoggersResponse;
import com.nucleonforge.axile.master.service.convert.response.loggers.ServiceLoggersConverter;

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
        subject = new ServiceLoggersConverter();
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
        List<LoggersResponse.Logger> loggers = response.loggers();

        // loggers -> "ROOT"
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("ROOT"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isEqualTo("INFO"))
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("INFO"));

        // loggers -> "com.example"
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("com.example"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isEqualTo("DEBUG"))
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("DEBUG"));

        // loggers ->
        assertThat(loggers)
                .filteredOn(logger -> logger.name().equals("org"))
                .first()
                .satisfies(logger -> assertThat(logger.configuredLevel()).isNull())
                .satisfies(logger -> assertThat(logger.effectiveLevel()).isEqualTo("INFO"));

        // groups
        assertThat(response.groups()).hasSize(3);
        List<LoggersResponse.Group> groups = response.groups();

        // groups -> "test"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("test"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isEqualTo("INFO"))
                .satisfies(
                        group -> assertThat(group.members()).containsExactlyInAnyOrder("test.member1", "test.member2"));

        // groups -> "web"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("web"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isNull())
                .satisfies(group -> assertThat(group.members())
                        .containsExactlyInAnyOrder(
                                "org.springframework.core.codec",
                                "org.springframework.http",
                                "org.springframework.web",
                                "org.springframework.boot.actuate.endpoint.web",
                                "org.springframework.boot.web.servlet.ServletContextInitializerBeans"));

        // groups -> "sql"
        assertThat(groups)
                .filteredOn(group -> group.name().equals("sql"))
                .first()
                .satisfies(group -> assertThat(group.configuredLevel()).isNull())
                .satisfies(group -> assertThat(group.members())
                        .containsExactlyInAnyOrder(
                                "org.springframework.jdbc.core", "org.hibernate.SQL", "org.jooq.tools.LoggerListener"));
    }

    private static ServiceLoggers getLoggers() {
        // levels
        List<String> levels = List.of("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE");

        // loggers
        Map<String, LoggerLevels> loggers = Map.of(
                "ROOT",
                new LoggerLevels("INFO", "INFO"),
                "com.example",
                new LoggerLevels("DEBUG", "DEBUG"),
                "org",
                new LoggerLevels(null, "INFO"));

        // groups
        Map<String, LoggerGroup> groups = Map.of(
                "test", new LoggerGroup("INFO", List.of("test.member1", "test.member2")),
                "web",
                        new LoggerGroup(
                                null,
                                List.of(
                                        "org.springframework.core.codec",
                                        "org.springframework.http",
                                        "org.springframework.web",
                                        "org.springframework.boot.actuate.endpoint.web",
                                        "org.springframework.boot.web.servlet.ServletContextInitializerBeans")),
                "sql",
                        new LoggerGroup(
                                null,
                                List.of(
                                        "org.springframework.jdbc.core",
                                        "org.hibernate.SQL",
                                        "org.jooq.tools.LoggerListener")));

        return new ServiceLoggers(levels, loggers, groups);
    }
}
