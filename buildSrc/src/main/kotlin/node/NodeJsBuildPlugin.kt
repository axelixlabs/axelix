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
package node

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Exec
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Gradle plugin that builds a Node.js front-end project and exposes its dist output as Java resources.
 *
 * @author Mikhail Polivakha
 */
class NodeJsBuildPlugin : Plugin<Project> {

    companion object {
        private const val AXELIX_DSL_CONFIG = "nodejs"
        private const val BUILD_NODE_JS_PROJECT_TASK_NAME = "buildNodeJsProject"
        private const val GENERATED_FRONT_END_DIST_DIR = "generated/front-end-dist"

        /**
         * Classpath segment under which SPA files are packaged (e.g. BOOT-INF/classes/spa/).
         * The [NodeJsBuildTask] copies Vite output into this subdirectory; [distDirectory] itself
         * is registered as a resource source dir so the segment name is preserved in the JAR.
         */
        const val SPA_CLASSPATH_DIRECTORY = "spa"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create<NodeJsBuildPluginExtension>(AXELIX_DSL_CONFIG)
        val installDependencies = project.tasks.register<Exec>("installConsoleDependencies") {
            workingDir(extension.sourceDir)
            commandLine("npm", "ci")
        }
        val checkConsole = project.tasks.register<Exec>("checkConsole") {
            dependsOn(installDependencies)
            workingDir(extension.sourceDir)
            commandLine("npm", "run", "check")
        }

        val buildNodeJsProjectTask =
            project.tasks.register<NodeJsBuildTask>(BUILD_NODE_JS_PROJECT_TASK_NAME) {
                group = "build"
                description = "Builds the Node.js front-end project and copies its dist into generated resources"

                dependsOn(checkConsole)
                val frontendInputs = listOf("src/**", "*.json", "*.ts", "*.js", ".npmrc", "*.html", ".env*")
                inputs.files(project.fileTree(project.rootProject.file("master/front-end")) { include(frontendInputs) })
                inputs.files(project.fileTree(extension.sourceDir) { include(frontendInputs) })
                inputs.property("version", project.version.toString())
                inputs.property("viteVersion", project.providers.environmentVariable("VITE_APP_VERSION").orElse(""))

                steps.set(extension.steps)
                sourceDir.set(extension.sourceDir)
                distDirectory.set(project.layout.buildDirectory.dir(GENERATED_FRONT_END_DIST_DIR))
            }

        project.plugins.withId("java") {
            project.extensions.configure<JavaPluginExtension> {
                sourceSets.named(SourceSet.MAIN_SOURCE_SET_NAME) {
                    resources.srcDir(buildNodeJsProjectTask.flatMap { it.distDirectory })
                }
            }

            project.tasks.named("processResources").configure {
                dependsOn(buildNodeJsProjectTask)
            }
        }
    }

    /**
     * DSL extension for [NodeJsBuildPlugin].
     *
     * @author Mikhail Polivakha
     */
    interface NodeJsBuildPluginExtension {
        val steps: ListProperty<String>

        val sourceDir: Property<String>
    }
}
