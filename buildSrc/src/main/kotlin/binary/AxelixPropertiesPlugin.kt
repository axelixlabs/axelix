package binary

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create

/**
 * Internal plugin for various Axelix modules components build.
 *
 * @author Mikhail Polivakha
 */
class AxelixPropertiesPlugin : Plugin<Project> {

    companion object {
        private const val AXELIX_DSL_CONFIG = "axelix"

        private const val AXELIX_PROPERTIES_FILE_NAME = "axelix.properties"
    }

    override fun apply(project: Project) {
        val extension = project.extensions.create<AxelixPluginExtension>(AXELIX_DSL_CONFIG)

        val axelixGenResourcesDir = project.layout.buildDirectory.dir("generated-resources/axelix")

        val genTask = project.tasks.register("generatePropsFile", AxelixPropertiesGenerationTask::class.java) {
            properties.set(extension.properties)

            outputFile.set(axelixGenResourcesDir.map { it.file("META-INF/$AXELIX_PROPERTIES_FILE_NAME") })
        }

        project.plugins.withId("java") {
            project.extensions.configure<SourceSetContainer> {
                named("main") {
                    // we want to put the entire META-INF directory's content into final resources
                    // bundle, that is why we have two parent calls below
                    resources.srcDir(genTask.map { it.outputFile.get().asFile.parentFile.parent })
                }
            }
        }
    }

    /**
     * Interface that provides the DSL extension for the [AxelixPropertiesPlugin].
     *
     * @author Mikhail Polivakha
     */
    interface AxelixPluginExtension {

        val properties: MapProperty<String, String>
    }
}