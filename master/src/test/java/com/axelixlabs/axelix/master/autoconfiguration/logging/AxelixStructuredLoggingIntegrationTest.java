/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.master.autoconfiguration.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link AxelixStructuredLoggingEnvironmentPostProcessor}.
 *
 * @author Dmitry Mazurov
 */
@ExtendWith(OutputCaptureExtension.class)
class AxelixStructuredLoggingIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(AxelixStructuredLoggingIntegrationTest.class);

    @Test
    void shouldLogInJsonFormatWhenEnabled(CapturedOutput output) {
        // given.
        try (ConfigurableApplicationContext _ = new SpringApplicationBuilder(EmptyApplication.class)
                .web(WebApplicationType.NONE)
                .properties(
                        "spring.main.banner-mode=off",
                        "axelix.master.logging.json=true",
                        "axelix.master.environment=integration-test")
                .run()) {

            // when.
            logger.info("structured logging integration test message");
        }

        // then.
        assertThat(output.getOut()).contains("\"@timestamp\"");
        assertThat(output.getOut()).contains("\"environment\":\"integration-test\"");
        assertThat(output.getOut()).contains("structured logging integration test message");
    }

    @Test
    void shouldLogPlainTextWhenDisabled(CapturedOutput output) {
        // given.
        try (ConfigurableApplicationContext _ = new SpringApplicationBuilder(EmptyApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off")
                .run()) {

            // when.
            logger.info("plain logging integration test message");
        }

        // then.
        assertThat(output.getOut()).doesNotContain("\"@timestamp\"");
        assertThat(output.getOut()).contains("plain logging integration test message");
    }

    @Configuration
    static class EmptyApplication {}
}
