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
package com.axelixlabs.axelix.master.utils.auth;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.master.utils.InvalidAuthScenario;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TestTemplateInvocationContext} for scenarios with bad token.
 *
 * @author Mikhail Polivakha
 */
public class BadTokenProtectedEndpointInvocationContext implements TestTemplateInvocationContext {

    @Override
    public List<Extension> getAdditionalExtensions() {
        return Arrays.stream(InvalidAuthScenario.values())
                .map(invalidAuthScenario -> (Extension) new BeforeTestExecutionCallback() {

                    @Override
                    public void beforeTestExecution(ExtensionContext context) {
                        ProtectedEndpointTests meta =
                                context.getRequiredTestMethod().getAnnotation(ProtectedEndpointTests.class);

                        ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);

                        TestRestTemplateBuilder testRestTemplateBuilder =
                                applicationContext.getBean(TestRestTemplateBuilder.class);

                        ResponseEntity<Void> result = invalidAuthScenario
                                .getModifier()
                                .apply(testRestTemplateBuilder)
                                .exchange(
                                        meta.path(),
                                        org.springframework.http.HttpMethod.valueOf(
                                                meta.method().name()),
                                        ProtectedEndpointRequestSupport.httpEntity(meta),
                                        Void.class);

                        assertThat(HttpStatus.UNAUTHORIZED.value())
                                .isEqualTo(result.getStatusCode().value());
                    }
                })
                .toList();
    }
}
