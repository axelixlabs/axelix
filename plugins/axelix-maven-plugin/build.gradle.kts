plugins {
    id("java")
    id("org.gradlex.maven-plugin-development") version "1.0.3"
}

// groupId is inherited from the root `allprojects { group = "com.axelixlabs" }` block.
// artifactId is derived from this project's name: `axelix-maven-plugin`.
// The maven-plugin-development plugin reads groupId/artifactId/version/description from these
// project properties to generate META-INF/maven/plugin.xml and wires it into processResources/jar.

val mavenApiVersion = "3.9.9"
val mavenAnnotationsVersion = "3.15.1"
val mavenPluginTestingVersion = "3.5.1"
val assertJVersion = "3.27.3"
val junitBomVersion = "5.12.2"
val junit4Version = "4.13.2"

dependencies {
    // Provided by the Maven runtime; must not be bundled into the plugin jar.
    compileOnly("org.apache.maven:maven-plugin-api:$mavenApiVersion")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:$mavenAnnotationsVersion")
    // MavenProject, Artifact, ResolutionScope (maven-core); ComparableVersion (maven-artifact).
    compileOnly("org.apache.maven:maven-core:$mavenApiVersion")
    compileOnly("org.apache.maven:maven-artifact:$mavenApiVersion")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
    options.compilerArgs.add("-parameters")
}

testing {
    suites {
        // The root build forces useJUnitJupiter() on every JvmTestSuite, so Jupiter is present here.
        val test by getting(JvmTestSuite::class) {
            dependencies {
                implementation("org.assertj:assertj-core:$assertJVersion")
                // The planner builds org.apache.maven.model.Dependency and reads Artifact/ComparableVersion.
                implementation("org.apache.maven:maven-core:$mavenApiVersion")
                implementation("org.apache.maven:maven-artifact:$mavenApiVersion")
            }
        }

        val integrationTestSuite = "integrationTest"

        register<JvmTestSuite>(integrationTestSuite) {

            sources {
                java {
                    setSrcDirs(listOf("src/$integrationTestSuite/java"))
                }
                resources {
                    setSrcDirs(listOf("src/$integrationTestSuite/resources"))
                }
            }

            dependencies {
                // Additional Test Suites do not inherit production dependencies automatically.
                // Depending on the module output pulls in the compiled Mojos AND the generated
                // META-INF/maven/plugin.xml that the testing harness uses to locate the Mojo.
                implementation(project())

                // The compile-only Maven API the Mojos were compiled against is not on the suite
                // classpath; the harness needs the plugin-api + annotations at runtime too.
                implementation("org.apache.maven:maven-plugin-api:$mavenApiVersion")
                implementation("org.apache.maven.plugin-tools:maven-plugin-annotations:$mavenAnnotationsVersion")

                // The harness; the Maven 3 Mojo path (MojoRule / AbstractMojoTestCase) is JUnit4.
                implementation("org.apache.maven.plugin-testing:maven-plugin-testing-harness:$mavenPluginTestingVersion")

                // The harness declares maven-core/-model/-artifact/-resolver at `provided` scope,
                // which Gradle does NOT bring transitively. Without an explicit Maven runtime the
                // Plexus container fails to boot. maven-compat supplies the legacy project-builder
                // components MojoRule relies on.
                implementation("org.apache.maven:maven-core:$mavenApiVersion")
                implementation("org.apache.maven:maven-compat:$mavenApiVersion")

                // MojoRule is a JUnit4 TestRule; run it via the vintage engine alongside Jupiter.
                implementation(platform("org.junit:junit-bom:$junitBomVersion"))
                implementation("junit:junit:$junit4Version")
                runtimeOnly("org.junit.vintage:junit-vintage-engine")

                implementation("org.assertj:assertj-core:$assertJVersion")
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.named("check") {
    dependsOn(testing.suites.named("integrationTest"))
}
