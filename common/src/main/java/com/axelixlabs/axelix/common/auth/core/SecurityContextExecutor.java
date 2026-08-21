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
package com.axelixlabs.axelix.common.auth.core;

import java.util.Optional;

import com.axelixlabs.axelix.common.domain.function.ThrowingCallable;
import com.axelixlabs.axelix.common.domain.function.ThrowingRunnable;

/**
 * Main abstraction to access the {@link SecurityContext}.
 *
 * @author Mikhail Polivakha
 */
public interface SecurityContextExecutor {

    /**
     * Run the specified callback (with no return value) inside the given {@link SecurityContext}. In case
     * any exceptions occur inside the provided callback, they are re-thrown to the caller.
     *
     * @param runnable callback to run.
     * @param securityContext {@link SecurityContext} to run the callback in.
     * @param <T> possible {@link Exception} that can be thrown by the provided {@link ThrowingRunnable}.
     *
     * @throws T re-thrown exception.
     */
    <T extends Exception> void runWithinSecurityContext(ThrowingRunnable<T> runnable, SecurityContext securityContext)
            throws T;

    /**
     * Run the specified callback (with no return value) inside the given {@link SecurityContext}. In case
     * any exceptions occur inside the provided callback, they are re-thrown to the caller.
     * <p>
     * Similar to {@link #runWithinSecurityContext(ThrowingRunnable, SecurityContext)}, with the only difference
     * is that this version accepts {@link ThrowingCallable} and thus produced certain value as a result of its execution.
     *
     * @param callable callback to run.
     * @param securityContext {@link SecurityContext} to run the callback in.
     * @param <T> possible {@link Exception} that can be thrown by the provided {@link ThrowingRunnable}.
     *
     * @return the value produced by {@link ThrowingCallable}.
     * @throws T re-thrown exception.
     */
    <V, T extends Exception> V callWithinSecurityContext(
            ThrowingCallable<V, T> callable, SecurityContext securityContext) throws T;

    /**
     * Provides the security bounded to current execution, or {@link Optional#empty()} if none.
     */
    Optional<SecurityContext> getSecurityContext();
}
