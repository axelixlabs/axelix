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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

import com.axelixlabs.axelix.common.auth.core.DefaultAuthority;
import com.axelixlabs.axelix.common.domain.http.HttpMethod;

/**
 * The annotation should be placed over the test class's method to signify that this API resource is supposed
 * to be a protected one, i.e. we require the access token to access it with the correct privilege.
 * <p>
 * By placing this annotation on the test template method of the Junit test class, the {@link ProtectedEndpointExtension}
 * listener will be invoked to create necessary {@link BadAuthorityEndpointInvocationContext invocation contexts}
 * that are themselves going to check the negative authentication/authorization cases.
 *
 * @see org.junit.jupiter.api.TestTemplate
 * @see ProtectedEndpointExtension
 * @see BadAuthorityEndpointInvocationContext
 *
 * @author Mikhail Polivakha
 */
@Documented
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@TestTemplate
@ExtendWith(ProtectedEndpointExtension.class)
public @interface ProtectedEndpointTests {

    /**
     * Path to the endpoint (e.g. /api/external/...).
     */
    String path();

    /**
     * Http method of the endpoint.
     */
    HttpMethod method();

    /**
     * The authority that is required to access this endpoint. The size of array is expected to always be either
     * zero or one, where zero means there is no authority to check for this endpoint.
     */
    DefaultAuthority[] requiredAuthority() default {};

    /**
     * Optional JSON request body for POST, PUT, or PATCH. When empty, the request is sent without a body (same as
     * {@code null} {@link org.springframework.http.HttpEntity}).
     */
    String jsonBody() default "";
}
