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
package com.axelixlabs.axelix.sbs.spring.core.validate;

import java.time.Duration;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.axelixlabs.axelix.sbs.spring.core.config.Validateable;

/**
 * Unit tests for {@link ValidationListener}.
 *
 * @author Mikhail Polivakha
 */
class ValidationListenerTest {

    @Test
    void shouldValidate_HappyPath() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(TestValidationTarget.class, () -> new TestValidationTarget(true))
                .withBean(ValidationListener.class, ValidationListener::new);

        runner.run(context -> {
            Assertions.assertThatCode(() -> {
                        context.getBean(ValidationListener.class)
                                .onApplicationEvent(new ApplicationReadyEvent(
                                        new SpringApplication(), new String[] {}, context, Duration.ZERO));
                    })
                    .doesNotThrowAnyException();
        });
    }

    @Test
    void shouldValidate_ValidationError() {
        ApplicationContextRunner runner = new ApplicationContextRunner()
                .withBean(TestValidationTarget.class, () -> new TestValidationTarget(false))
                .withBean(ValidationListener.class, ValidationListener::new);

        runner.run(context -> {
            Assertions.assertThatThrownBy(() -> {
                        context.getBean(ValidationListener.class)
                                .onApplicationEvent(new ApplicationReadyEvent(
                                        new SpringApplication(), new String[] {}, context, Duration.ZERO));
                    })
                    .isInstanceOf(IllegalArgumentException.class);
        });
    }

    static class TestValidationTarget implements Validateable {

        private final boolean valid;

        TestValidationTarget(boolean valid) {
            this.valid = valid;
        }

        @Override
        public void validate() throws IllegalArgumentException {
            if (!valid) {
                throw new IllegalArgumentException();
            }
        }
    }
}
