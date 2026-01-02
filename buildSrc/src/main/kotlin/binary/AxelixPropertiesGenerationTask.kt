package binary

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.util.Properties

/**
 * Gradle task for generating the {@code axelix.properties} file.
 *
 * @author Mikhail Polivakha
 */
abstract class AxelixPropertiesGenerationTask : DefaultTask() {

    /**
     * The version of Axelix distribution.
     */
    @get:Input
    abstract val properties: MapProperty<String, String>

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    @TaskAction
    fun generatePropertiesFile() {
        val props = Properties()
        props.putAll(properties.get())

        val propertiesFile = outputFile.get().asFile
        propertiesFile.parentFile.mkdirs()

        propertiesFile.outputStream().use {
            props.store(it, null)
        }
    }
}