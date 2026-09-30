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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.jgit.lib.AbbreviatedObjectId;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for generating {@code META-INF/axelix-info.properties}.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Named
@Singleton
public class ProjectInfoGenerator {

    public static final String PROFILER_DETECTED_PROPERTY = "spring.test.profiler.detected";
    public static final String AXELIX_INFO_PROPERTIES_LOCATION = "META-INF/axelix-info.properties";

    private static final Logger log = LoggerFactory.getLogger(ProjectInfoGenerator.class);

    private static final int ABBREVIATED_ID_LENGTH = 7;

    public void generate(MavenProject mavenProject, RepositorySystemSession repositorySystemSession)
            throws MojoExecutionException {
        Properties properties = collectBuildInfo(mavenProject, repositorySystemSession);
        collectGitInfo(mavenProject, properties);

        Path target = Path.of(mavenProject.getBuild().getOutputDirectory(), AXELIX_INFO_PROPERTIES_LOCATION);

        try {
            Files.createDirectories(target.getParent());
            try (OutputStream out = Files.newOutputStream(target)) {
                properties.store(out, null);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write project information to " + target, e);
        }
    }

    private Properties collectBuildInfo(MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        Properties properties = new Properties();
        properties.setProperty("build.group", mavenProject.getGroupId());
        properties.setProperty("build.name", mavenProject.getArtifactId());
        properties.setProperty("build.version", mavenProject.getVersion());
        properties.setProperty("build.time", Instant.now().toString());
        return properties;
    }

    private void collectGitInfo(MavenProject mavenProject, Properties properties) {
        File gitDir = new FileRepositoryBuilder()
                .findGitDir(mavenProject.getBasedir())
                .getGitDir();
        if (gitDir == null) {
            log.info("Skipping git info: '{}' is not inside a git working tree", mavenProject.getBasedir());
            return;
        }

        try (Repository repository =
                new FileRepositoryBuilder().setGitDir(gitDir).build()) {
            if (!addGitProperties(repository, properties)) {
                log.info("Skipping git info: '{}' has no commits yet", gitDir);
            }
        } catch (Exception e) {
            log.warn("Skipping git info: failed to read git repository at {}", gitDir, e);
        }
    }

    private boolean addGitProperties(Repository repository, Properties properties) throws IOException {
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
