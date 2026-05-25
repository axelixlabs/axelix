package node

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
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

        val buildNodeJsProjectTask =
            project.tasks.register<NodeJsBuildTask>(BUILD_NODE_JS_PROJECT_TASK_NAME) {
                group = "build"
                description = "Builds the Node.js front-end project and copies its dist into generated resources"

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
