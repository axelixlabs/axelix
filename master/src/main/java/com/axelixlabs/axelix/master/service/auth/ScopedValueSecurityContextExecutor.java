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
package com.axelixlabs.axelix.master.service.auth;

import java.util.Optional;

import org.jspecify.annotations.NullMarked;

import org.springframework.stereotype.Component;

import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;

/**
 * {@link SecurityContextExecutor} that is based upon the JDK 25 {@link ScopedValue}.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@Component
public class ScopedValueSecurityContextExecutor implements SecurityContextExecutor {

    public static final ScopedValue<SecurityContext> SECURITY_CONTEXT = ScopedValue.newInstance();

    @Override
    public <T extends Exception> void runWithinSecurityContext(
            ThrowingRunnable<T> runnable, SecurityContext securityContext) throws T {

        ScopedValue.where(SECURITY_CONTEXT, securityContext).call(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public <V, T extends Exception> V callWithinSecurityContext(
            ThrowingCallable<V, T> callable, SecurityContext securityContext) throws T {

        return ScopedValue.where(SECURITY_CONTEXT, securityContext).call(callable::call);
    }

    @Override
    public Optional<SecurityContext> getSecurityContext() {
        if (SECURITY_CONTEXT.isBound()) {
            return Optional.of(SECURITY_CONTEXT.get());
        } else {
            return Optional.empty();
        }
    }
}
