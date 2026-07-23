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
package com.axelixlabs.axelix.gradle.plugin;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

import static com.axelixlabs.axelix.gradle.plugin.SpringTestProfilerDetector.PROFILER_DETECTED_PROPERTY;

/**
 * Generates {@code META-INF/axelix-info.properties} — build coordinates plus git commit metadata
 * — and packages it into the project's archive: {@code jar} for plain Java projects, {@code
 * bootJar} for Spring Boot ones.
 *
 * <p>The build-info is mandatory: {@code group} and {@code name} must be set (Axelix uses them
 * to tell applications apart).
 *
 * @author Nikita Kirillov
 */
public final class ProjectInfoGenerator {

    public static final String GENERATE_TASK_NAME = "generateAxelixProjectInfo";

    private static final int ABBREVIATED_ID_LENGTH = 7;

    private ProjectInfoGenerator() {}

    public static void configure(final Project project) {
        File generatedDir = new File(BuildDirAccessor.buildDir(project), "generated/axelix-info");

        Task generateTask = project.getTasks().create(GENERATE_TASK_NAME);
        generateTask.setGroup("build");
        generateTask.setDescription(
                "Generates META-INF/axelix-info.properties describing this build and its git commit.");

        generateTask.getInputs().property("projectGroup", String.valueOf(project.getGroup()));
        generateTask.getInputs().property("projectName", project.getName());
        generateTask.getInputs().property("projectVersion", String.valueOf(project.getVersion()));

        // Computed lazily (resolving configurations is only safe once Gradle holds the task's
        // execution lock, see SpringTestProfilerDetector) and cached: Gradle's up-to-date check
        // reads this provider first, then doLast reads it again - without caching, that's the
        // same build-wide profiler scan run twice per task execution.
        AtomicReference<Boolean> profilerDetectedCache = new AtomicReference<>();
        Supplier<Boolean> profilerDetected =
                () -> profilerDetectedCache.updateAndGet(cachedBoolean -> cachedBoolean != null
                        ? cachedBoolean
                        : SpringTestProfilerDetector.isProfilerPresentAnywhereInBuild(project));

        // Tracked as an input so a flip in profiler detection invalidates this task's up-to-date
        // state - otherwise Gradle could see unchanged inputs/outputs and skip doLast.
        generateTask.getInputs().property("profilerDetected", project.provider(profilerDetected::get));

        File gitDir =
                new FileRepositoryBuilder().findGitDir(project.getProjectDir()).getGitDir();
        if (gitDir != null) {
            generateTask.getInputs().file(new File(gitDir, "HEAD")).optional(true);
            generateTask.getInputs().dir(new File(gitDir, "refs")).optional(true);
        }

        generateTask.getOutputs().dir(generatedDir);
        generateTask.doLast(task -> writeProjectInfo(project, generatedDir, profilerDetected.get()));

        project.getTasks().configureEach(task -> {
            String taskName = task.getName();
            if ("jar".equals(taskName) || "bootJar".equals(taskName)) {
                task.dependsOn(generateTask);

                if (task instanceof AbstractArchiveTask) {
                    ((AbstractArchiveTask) task).from(generatedDir);
                }
            }
        });
    }

    private static void writeProjectInfo(Project project, File generatedDir, boolean profilerDetected) {
        if (profilerDetected) {
            project.getLogger().info("Spring Test Profiler detected in this build");
        } else {
            project.getLogger().info("Spring Test Profiler was not detected in this build");
        }

        Properties properties = collectBuildInfo(project, profilerDetected);
        collectGitInfo(project, properties);

        File target = new File(new File(generatedDir, "META-INF"), "axelix-info.properties");
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new GradleException("Cannot create directory " + parent);
        }

        try (OutputStream out = Files.newOutputStream(target.toPath())) {
            properties.store(out, null);
        } catch (IOException e) {
            throw new GradleException("Failed to write project information to " + target, e);
        }
    }

    private static Properties collectBuildInfo(Project project, boolean profilerDetected) {
        validateCoordinates(project);

        Properties properties = new Properties();
        properties.setProperty("build.group", String.valueOf(project.getGroup()));
        properties.setProperty("build.name", project.getName());
        properties.setProperty("build.version", String.valueOf(project.getVersion()));
        properties.setProperty("build.time", Instant.now().toString());
        properties.setProperty(PROFILER_DETECTED_PROPERTY, String.valueOf(profilerDetected));
        return properties;
    }

    private static void validateCoordinates(Project project) {
        if (String.valueOf(project.getGroup()).isBlank()) {
            throw new GradleException("Axelix requires 'group' to be set on project '" + project.getPath()
                    + "' (e.g. group = \"com.example\") to tell applications apart. Please set it in your build.");
        }
        if (project.getName().isBlank()) {
            throw new GradleException(
                    "Axelix requires the project name to be set on project '" + project.getPath() + "'.");
        }
    }

    private static void collectGitInfo(Project project, Properties properties) {
        File gitDir =
                new FileRepositoryBuilder().findGitDir(project.getProjectDir()).getGitDir();
        if (gitDir == null) {
            project.getLogger()
                    .info("Skipping git info: '{}' is not inside a git working tree", project.getProjectDir());
            return;
        }

        try (Repository repository =
                new FileRepositoryBuilder().setGitDir(gitDir).build()) {
            if (!addGitProperties(repository, properties)) {
                project.getLogger().info("Skipping git info: '{}' has no commits yet", gitDir);
            }
        } catch (IOException e) {
            project.getLogger().warn("Skipping git info: failed to read git repository at {}", gitDir, e);
        }
    }

    private static boolean addGitProperties(Repository repository, Properties properties) throws IOException {
        ObjectId head = repository.resolve("HEAD");
        if (head == null) {
            return false;
        }

        try (RevWalk revWalk = new RevWalk(repository);
                ObjectReader reader = repository.newObjectReader()) {
            RevCommit commit = revWalk.parseCommit(head);
            PersonIdent author = commit.getAuthorIdent();
            PersonIdent committer = commit.getCommitterIdent();
            AbbreviatedObjectId abbreviated = reader.abbreviate(head, ABBREVIATED_ID_LENGTH);

            properties.setProperty("git.commit.id", head.getName());
            properties.setProperty("git.commit.id.abbrev", abbreviated.name());
            properties.setProperty("git.branch", repository.getBranch());
            properties.setProperty("git.commit.user.name", author.getName());
            properties.setProperty("git.commit.user.email", author.getEmailAddress());
            properties.setProperty(
                    "git.commit.time",
                    OffsetDateTime.ofInstant(committer.getWhenAsInstant(), committer.getZoneId())
                            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
            return true;
        }
    }
}
