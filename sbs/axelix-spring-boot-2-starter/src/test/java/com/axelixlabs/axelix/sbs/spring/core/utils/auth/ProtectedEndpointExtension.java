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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;

import com.axelixlabs.axelix.sbs.spring.core.utils.InvalidAuthScenario;

/**
 * A specific extension for Junit 6 to provide {@link TestTemplateInvocationContext} instances for the given
 * {@link ProtectedEndpointTests} methods.
 *
 * @author Mikhail Polivakha
 * @author Vyacheslav Yanin
 */
public class ProtectedEndpointExtension implements TestTemplateInvocationContextProvider {

    @Override
    public boolean supportsTestTemplate(ExtensionContext context) {
        return context.getTestMethod()
                .map(method -> method.isAnnotationPresent(ProtectedEndpointTests.class))
                .orElse(false);
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
        Method requiredTestMethod = context.getRequiredTestMethod();

        ProtectedEndpointTests annotation = requiredTestMethod.getAnnotation(ProtectedEndpointTests.class);

        Stream<TestTemplateInvocationContext> badAuthorityScenarios;
        if (annotation.requiredAuthority().length > 0) {
            badAuthorityScenarios = BadAuthorityEndpointInvocationContext.allBadAuthorityScenarios(
                    annotation.requiredAuthority()[0]);
        } else {
            badAuthorityScenarios = Stream.of();
        }

        Stream<TestTemplateInvocationContext> badTokenScenarios =
                Arrays.stream(InvalidAuthScenario.values()).map(BadTokenProtectedEndpointInvocationContext::new);

        return Stream.concat(badAuthorityScenarios, badTokenScenarios);
    }
}
