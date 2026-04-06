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
package com.axelixlabs.axelix.master.utils;

import java.util.Optional;
import java.util.Set;

import com.axelixlabs.axelix.common.auth.core.DefaultSecurityContext;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;
import org.jspecify.annotations.NullMarked;

/**
 * {@link SecurityContextExecutor} for tests: always exposes a fixed token so actuator probers can build
 * {@code Authorization} headers without a servlet-bound {@link SecurityContext}.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
public final class TestFixedSecurityContextExecutor implements SecurityContextExecutor {

    private static final SecurityContext CONTEXT =
            new DefaultSecurityContext(new DefaultUser("test","", Set.of()), "test-token");

    @Override
    public <T extends Exception> void runWithinSecurityContext(
            ThrowingRunnable<T> runnable, SecurityContext securityContext) throws T {
        runnable.run();
    }

    @Override
    public <V, T extends Exception> V callWithinSecurityContext(
            ThrowingCallable<V, T> callable, SecurityContext securityContext) throws T {
        return callable.call();
    }

    @Override
    public Optional<SecurityContext> getSecurityContext() {
        return Optional.of(CONTEXT);
    }
}
