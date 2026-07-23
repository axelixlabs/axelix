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

import java.util.List;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link SpringTestProfilerDetector}
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
class SpringTestProfilerDetectorTest {

    @Test
    void shouldTreatUnresolvableProjectAsProfilerNotPresentInsteadOfFailing() {
        // given. a project whose dependency tree cannot be resolved at all - detection must not
        // blow up the reactor build over this, since it runs against every project regardless of
        // whether that project even declared axelix-maven-plugin.
        MavenProject mavenProject = new MavenProject();
        mavenProject.setGroupId("com.example");
        mavenProject.setArtifactId("axelix-plugin-test");
        mavenProject.setVersion("1.0.0");

        SpringTestProfilerDetector detector = new SpringTestProfilerDetector();
        detector.dependencyResolver = new DependencyResolver() {
            @Override
            public List<Artifact> resolveDependencies(MavenProject project, RepositorySystemSession repoSession) {
                throw new RuntimeException("dependency resolution failed");
            }
        };

        // when.
        boolean profilerPresent = detector.isProfilerPresent(mavenProject, null);

        // then.
        assertThat(profilerPresent).isFalse();
    }
}
