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
package com.axelixlabs.axelix.sbs.spring.gclog;

import java.util.concurrent.TimeUnit;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * {@link Condition} that checks whether the {@code jcmd} tool is available
 * and can be successfully executed for the current JVM process.
 *
 * @since 29.12.2025
 * @author Nikita Kirillov
 */
public class OnJcmdCondition implements Condition {

    private static final Logger log = LoggerFactory.getLogger(OnJcmdCondition.class);

    @Override
    public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
        try {
            long pid = ProcessHandle.current().pid();

            ProcessBuilder processBuilder = new ProcessBuilder("jcmd", Long.toString(pid), "VM.version");

            processBuilder.redirectErrorStream(true);
            processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD);

            Process process = processBuilder.start();

            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0;
        } catch (Throwable t) {
            log.warn(
                    "JCMD is not available or cannot attach to the current JVM. Features requiring jcmd will be disabled.");
            return false;
        }
    }
}
