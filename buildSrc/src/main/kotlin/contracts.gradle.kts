import contract.ContractDocumentsValidator
import contract.ContractsExtension
import contract.invariants.stateful.ReleaseBaseline
import org.gradle.api.plugins.quality.Pmd
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

/**
 * Generates the wire classes of the Axelix Master <-> starter contracts from the OpenAPI
 * documents in :common, so that the yaml document stays the single source of truth of every
 * contract. Every document is supposed to describe exactly one operation.
 */

plugins {
    java
    id("org.openapi.generator")
}

val contracts = extensions.create<ContractsExtension>("contracts")

val contractSources = fileTree("$rootDir/common/src/main/resources/contract") { include("**/*.yaml") }
    .sortedBy { it.absolutePath }
    .map { document ->
        // We re-define some local variables here, so that the doFirst closure below captures plain values instead of a
        // reference to the whole script object, which the configuration cache cannot serialize.
        val currentVersion = version.toString()
        val repoRoot = rootDir
        val featurePackage = document.parentFile.name.replace("-", "")
        val operation = document.nameWithoutExtension
        val outputRoot = layout.buildDirectory.dir("generated/openapi/$operation").get().asFile

        val generate = tasks.register<GenerateTask>(
            "openApiGenerate" + operation.split('-').joinToString("") { part -> part.replaceFirstChar(Char::uppercase) }) {

            generatorName.set("java")
            library.set("resttemplate")
            inputSpec.set(document.absolutePath)
            outputDir.set(outputRoot.absolutePath)
            modelPackage.set(contracts.modelBasePackage.map { basePackage -> "$basePackage.$featurePackage" })
            globalProperties.set(mapOf("models" to "", "modelDocs" to "false", "modelTests" to "false"))
            configOptions.set(mapOf(
                "hideGenerationTimestamp" to "true",
                "openApiNullable" to "false",
                "serializationLibrary" to "jackson",
                "useBeanValidation" to "false",
                "annotationLibrary" to "none",
                "useJspecify" to "true",
            ))

            doFirst {
                val baseline = ReleaseBaseline.find(repoRoot)
                if (baseline == null) {
                    logger.warn(
                        "No release tag (vX.Y.Z) is reachable in $repoRoot: the stateful "
                            + "contract checks of ${document.name} are skipped")
                }
                ContractDocumentsValidator.validate(document, currentVersion, baseline)
            }

            // The generator offers no option to suppress the @Generated annotation, so it is
            // dropped right after the generation, freeing the module from a javax.annotation-api
            // dependency.
            val rewriteRoot = outputRoot.resolve("src/main/java")
            doLast {
                rewriteRoot.walkTopDown().filter { it.extension == "java" }.forEach { source ->
                    source.writeText(source.readText()
                        .lineSequence().filterNot { it.startsWith("@javax.annotation.Generated") }.joinToString("\n"))
                }
            }
        }

        // builtBy carries the task dependency to every consumer of the source set, including the
        // sourcesJar of the starters the :sbs:starter-domain module is shaded into.
        files(outputRoot.resolve("src/main/java")).builtBy(generate)
    }

sourceSets {
    main {
        java {
            contractSources.forEach { srcDir(it) }
        }
    }
}

tasks.withType<Pmd>().configureEach {
    exclude("**/contract/**")
}
