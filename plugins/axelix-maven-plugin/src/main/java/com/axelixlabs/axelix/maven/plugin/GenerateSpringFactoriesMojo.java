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
package com.axelixlabs.axelix.maven.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.inject.Inject;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mojo that generates {@code spring.factories} file for {@code spring-test-profiler}.
 *
 * @implNote Note, that there may be the case, that the end-user will have the spring.factories in hte META-INF
 *           for the tests already. And we cannot just add spring.factories and overwrite it. As a result, this mojo
 *           tries to merge the existing spring.factories (if found any) with the one that we want to add.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@Mojo(name = "axelix-generate-spring-factories", defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES)
public class GenerateSpringFactoriesMojo extends AbstractMojo {
    public static final Map<String, Set<String>> PROFILER_SPRING_FACTORIES = Map.of(
            "org.springframework.test.context.TestExecutionListener",
            Set.of("digital.pragmatech.testing.SpringTestProfilerListener"),
            "org.springframework.context.ApplicationContextInitializer",
            Set.of("digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer"));
    public static final String GENERATED_RESOURCES_BASE = "generated-test-resources/axelix";
    public static final String SPRING_FACTORIES_PATH = "META-INF/spring.factories";
    private static final Logger log = LoggerFactory.getLogger(GenerateSpringFactoriesMojo.class);

    @Parameter(readonly = true, defaultValue = "${project}")
    @SuppressWarnings("NullAway")
    private MavenProject mavenProject;

    @Inject
    private SpringFactoriesUtilities springFactoriesUtilities;

    @Override
    public void execute() {
        List<Map<String, Set<String>>> factories = new ArrayList<>();

        for (Resource testResource : mavenProject.getTestResources()) {
            File springFactories = new File(testResource.getDirectory(), SPRING_FACTORIES_PATH);

            if (springFactories.exists()) {
                factories.add(springFactoriesUtilities.load(springFactories.getAbsolutePath()));
                // Maven resources plugin does not overwrite by default; exclude the original so the
                // merged generated file is what ends up on the test classpath.
                testResource.addExclude(SPRING_FACTORIES_PATH);
            }
        }

        factories.add(PROFILER_SPRING_FACTORIES);

        writeToFile(factories);

        addResource();
    }

    /**
     * Adds maven resource to {@code GENERATED_RESOURCES_BASE}
     */
    private void addResource() {
        String dir = Paths.get(mavenProject.getBuild().getDirectory(), GENERATED_RESOURCES_BASE)
                .toAbsolutePath()
                .toString();

        Resource resource = new Resource();
        resource.setDirectory(dir);

        mavenProject.addTestResource(resource);
    }

    /**
     * Writes {@code spring.factories} to file
     * @param factories List of {@code spring.factories} files content to merge
     */
    private void writeToFile(List<Map<String, Set<String>>> factories) {
        Properties result = springFactoriesUtilities.convertToProperties(springFactoriesUtilities.merge(factories));
        Path outputDir =
                Paths.get(mavenProject.getBuild().getDirectory(), GENERATED_RESOURCES_BASE, SPRING_FACTORIES_PATH);

        try {
            Files.createDirectories(outputDir.getParent());

            try (OutputStream out =
                    Files.newOutputStream(outputDir, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
                result.store(out, null);
            }
        } catch (IOException e) {
            log.error("Failed to write spring.factories file for project: {}", mavenProject.getName());
            throw new RuntimeException(e);
        }
    }
}
