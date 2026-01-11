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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

/**
 * Utility class for executing JCMD processes.
 *
 * @since 29.12.2025
 * @author Nikita Kirillov
 */
public class JcmdExecutor {

    public ProcessResult execute(String... command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();

            String output;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {

                output = reader.lines().collect(Collectors.joining("\n"));
            }

            // TODO: Perhaps it's worth adding a timeout. Consider using process.waitFor(long, TimeUnit).
            int exitCode = process.waitFor();

            return new ProcessResult(exitCode, output);

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GcLogException("Failed to execute jcmd", e);
        }
    }
}
