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

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.axelixlabs.axelix.common.auth.core.Authority;
import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.master.utils.TestRestTemplateBuilder;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The {@link TestTemplateInvocationContext} that is specific for checking that any other authority except
 * the provided in {@link ProtectedEndpointTests} will result in {@link HttpStatus#FORBIDDEN}.
 *
 * @author Mikhail Polivakha
 */
public class BadAuthorityEndpointInvocationContext implements TestTemplateInvocationContext {

    private final DefaultAuthority accessAuthority;

    /**
     * @param accessAuthority  the authority that is required to access the given endpoint. The assumption is that for
     *                         any given endpoint there will be just one authority required. Therefore, everything else,
     *                         any other authority except this one MUST produce 403 from the backend.
     */
    public BadAuthorityEndpointInvocationContext(DefaultAuthority accessAuthority) {
        this.accessAuthority = accessAuthority;
    }

    @Override
    public @NonNull List<Extension> getAdditionalExtensions() {

        EnumSet<DefaultAuthority> allTheOtherAuthorities = EnumSet.complementOf(EnumSet.of(accessAuthority));

        return allTheOtherAuthorities.stream()
                .map(defaultAuthority -> (Extension) new TestInvocationCallback(defaultAuthority))
                .toList();
    }

    public static class TestInvocationCallback implements BeforeTestExecutionCallback {

        private final Authority authority;

        TestInvocationCallback(Authority authority) {
            this.authority = authority;
        }

        @Override
        public void beforeTestExecution(ExtensionContext context) {
            ProtectedEndpointTests meta = context.getRequiredTestMethod().getAnnotation(ProtectedEndpointTests.class);

            ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);

            TestRestTemplateBuilder testRestTemplateBuilder = applicationContext.getBean(TestRestTemplateBuilder.class);

            TestRestTemplate integrationTestsRole =
                    testRestTemplateBuilder.withRole(new DefaultRole("INTEGRATION_TESTS_ROLE", Set.of(authority)));

            ResponseEntity<Void> result = integrationTestsRole.exchange(
                    meta.path(),
                    org.springframework.http.HttpMethod.valueOf(meta.method().name()),
                    ProtectedEndpointRequestSupport.httpEntity(meta),
                    Void.class);

            assertThat(HttpStatus.FORBIDDEN.value())
                    .isEqualTo(result.getStatusCode().value());
        }
    }
}
