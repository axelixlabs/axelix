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

import java.util.Optional;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.SecurityContext;
import com.axelixlabs.axelix.common.auth.core.SecurityContextExecutor;
import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;

/**
 * {@link SecurityContextExecutor} that is based upon the ThreadLocal.
 *
 * @author Nikita Kirillov
 */
@NullMarked
public class ThreadLocalSecurityContextExecutor implements SecurityContextExecutor {

    private static final ThreadLocal<@Nullable SecurityContext> SECURITY_CONTEXT_HOLDER = new ThreadLocal<>();

    @Override
    public <T extends Exception> void runWithinSecurityContext(
            ThrowingRunnable<T> runnable, SecurityContext securityContext) throws T {

        try {
            SECURITY_CONTEXT_HOLDER.set(securityContext);
            runnable.run();
        } finally {
            SECURITY_CONTEXT_HOLDER.remove();
        }
    }

    @Override
    public <V, T extends Exception> V callWithinSecurityContext(
            ThrowingCallable<V, T> callable, SecurityContext securityContext) throws T {

        try {
            SECURITY_CONTEXT_HOLDER.set(securityContext);
            return callable.call();
        } finally {
            SECURITY_CONTEXT_HOLDER.remove();
        }
    }

    @Override
    public Optional<SecurityContext> getSecurityContext() {
        SecurityContext context = SECURITY_CONTEXT_HOLDER.get();
        return context != null ? Optional.of(context) : Optional.empty();
    }
}
