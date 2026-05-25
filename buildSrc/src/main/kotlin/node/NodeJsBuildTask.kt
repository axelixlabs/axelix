package node

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * NodeJS tasks executor.
 *
 * @author Mikhail Polivakha
 */
abstract class NodeJsBuildTask : DefaultTask() {

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    companion object {
        private const val NPM_EXECUTABLE = "npm"
        private const val VITE_DIST_DIR_NAME = "dist"
        private const val NPM_COMMAND_TIMEOUT_MINUTES = 60L
    }

    /**
     * Steps to be performed.
     */
    @get:Input
    abstract val steps: ListProperty<String>

    /**
     * The directory that contains the front-end sources and package.json.
     */
    @get:Input
    abstract val sourceDir: Property<String>

    /**
     * The directory that contains the front-end distribution copied into Java resources.
     */
    @get:OutputDirectory
    abstract val distDirectory: DirectoryProperty

    @TaskAction
    fun buildNodeJsApplication() {
        verifyNpmIsAvailable()

        val frontEndProjectDir = File(sourceDir.get())

        steps.get().forEach { step ->
            runNpmStep(frontEndProjectDir, step)
        }

        val viteDistDir = frontEndProjectDir.resolve(VITE_DIST_DIR_NAME)
        if (!viteDistDir.isDirectory) {
            throw GradleException(
                "Expected Vite output directory at '${viteDistDir.absolutePath}' after npm build, but it does not exist",
            )
        }

        fileSystemOperations.sync {
            from(viteDistDir)
            into(distDirectory.dir(NodeJsBuildPlugin.SPA_CLASSPATH_DIRECTORY))
        }
    }

    private fun verifyNpmIsAvailable() {
        try {
            val process = ProcessBuilder(NPM_EXECUTABLE, "-v").start()
            val exited = process.waitFor(1, TimeUnit.MINUTES)
            if (!exited || process.exitValue() != 0) {
                throw GradleException("The 'npm' executable is not available or returned a non-zero exit code")
            }
        } catch (e: IOException) {
            throw GradleException(
                "The 'npm' executable is not available. Make sure you have 'npm' installed and available in PATH",
                e,
            )
        }
    }

    private fun runNpmStep(workingDirectory: File, step: String) {
        val args = step.split("\\s+".toRegex()).filter { it.isNotEmpty() }.toTypedArray()
        val process = ProcessBuilder(NPM_EXECUTABLE, *args).directory(workingDirectory).start()
        val exited = process.waitFor(NPM_COMMAND_TIMEOUT_MINUTES, TimeUnit.MINUTES)

        if (!exited) {
            throw GradleException("Timed out waiting for ${NPM_EXECUTABLE} ${step} to complete")
        }

        if (process.exitValue() != 0) {
            val errorOutput = process.errorStream.bufferedReader().readText()
            throw GradleException(
                "Step '${NPM_EXECUTABLE} ${step}' produced non-zero exit value ${process.exitValue()}${if (errorOutput.isBlank()) "" else ": $errorOutput"}",
            )
        }

        logger.lifecycle("Step '${NPM_EXECUTABLE} ${step}' executed successfully")
    }
}
