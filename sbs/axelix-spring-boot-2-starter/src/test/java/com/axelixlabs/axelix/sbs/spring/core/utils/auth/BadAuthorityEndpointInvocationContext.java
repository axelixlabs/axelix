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
package com.axelixlabs.axelix.sbs.spring.core.utils.auth;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@link TestTemplateInvocationContext} that is specific for checking that any other authority except
 * the provided in {@link ProtectedEndpointTests} will result in {@link HttpStatus#FORBIDDEN}.
 *
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public class BadAuthorityEndpointInvocationContext implements TestTemplateInvocationContext {

    private final DefaultAuthority accessAuthority;
    private final DefaultAuthority forbiddenAuthority;

    /**
     * @param accessAuthority  the authority that is required to access the given endpoint. The assumption is that for
     *                         any given endpoint there will be just one authority required. Therefore, everything else,
     *                         any other authority except this one MUST produce 403 from the backend.
     */
    private BadAuthorityEndpointInvocationContext(
            DefaultAuthority accessAuthority, DefaultAuthority forbiddenAuthority) {
        this.accessAuthority = accessAuthority;
        this.forbiddenAuthority = forbiddenAuthority;
    }

    public static Stream<TestTemplateInvocationContext> allBadAuthorityScenarios(DefaultAuthority accessAuthority) {
        return EnumSet.complementOf(EnumSet.of(accessAuthority)).stream()
                .map(forbiddenAuthority ->
                        new BadAuthorityEndpointInvocationContext(accessAuthority, forbiddenAuthority));
    }

    @Override
    public String getDisplayName(int invocationIndex) {
        return String.format(
                "Forbidden Auth [%d]: %s (endpoint requires: %s)",
                invocationIndex, forbiddenAuthority.name(), accessAuthority.name());
    }

    @Override
    public List<Extension> getAdditionalExtensions() {
        return Collections.singletonList((BeforeTestExecutionCallback) context -> {
            ProtectedEndpointTests meta = context.getRequiredTestMethod().getAnnotation(ProtectedEndpointTests.class);

            ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);

            TestRestTemplateBuilder testRestTemplateBuilder = applicationContext.getBean(TestRestTemplateBuilder.class);

            TestRestTemplate integrationTestsRole = testRestTemplateBuilder.withRole(
                    new DefaultRole("INTEGRATION_TESTS_ROLE", Set.of(forbiddenAuthority)));

            ResponseEntity<Void> result = integrationTestsRole.exchange(
                    meta.path(),
                    HttpMethod.valueOf(meta.method().name()),
                    ProtectedEndpointRequestSupport.httpEntity(meta),
                    Void.class);

            assertThat(result.getStatusCode().value()).isEqualTo(HttpStatus.FORBIDDEN.value());
        });
    }
}
