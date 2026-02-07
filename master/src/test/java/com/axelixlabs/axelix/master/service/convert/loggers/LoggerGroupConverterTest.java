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
package com.axelixlabs.axelix.master.service.convert.loggers;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.axelixlabs.axelix.common.api.loggers.LoggerGroup;
import com.axelixlabs.axelix.master.api.external.response.loggers.GroupProfileResponse;
import com.axelixlabs.axelix.master.service.convert.response.loggers.LoggerGroupConverter;

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
