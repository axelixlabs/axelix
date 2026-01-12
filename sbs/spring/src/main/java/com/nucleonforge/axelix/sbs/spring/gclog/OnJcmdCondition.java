/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axelix.sbs.spring.gclog;

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
