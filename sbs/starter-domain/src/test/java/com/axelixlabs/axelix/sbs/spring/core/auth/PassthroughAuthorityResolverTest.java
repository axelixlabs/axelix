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
package com.axelixlabs.axelix.sbs.spring.core.auth;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link PassthroughAuthorityResolver}.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("removal")
class PassthroughAuthorityResolverTest {

    private final PassthroughAuthorityResolver resolver = new PassthroughAuthorityResolver();

    @Test
    void resolve_returnsEmpty_forAnyPath() {
        // given.
        String path = "/actuator/axelix-beans";
        // when.
        var result = resolver.resolve(path);
        // then.
        assertThat(result).isEmpty();
    }

    @Test
    void resolve_returnsEmpty_forActuatorEnvPath() {
        // given.
        String path = "/actuator/axelix-env";
        // when.
        var result = resolver.resolve(path);
        // then.
        assertThat(result).isEmpty();
    }
}
