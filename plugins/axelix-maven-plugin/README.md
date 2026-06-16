# axelix-maven-plugin

Wires the Axelix [spring-test-profiler](https://central.sonatype.com/artifact/digital.pragmatech.testing/spring-test-profiler)
diagnostics into a Maven-based Spring Boot project's **test** build. It is the Maven counterpart of
the Axelix Gradle plugin and supports Spring Boot 2 through Spring Boot 4.

For every reactor project that declares it, the plugin:

1. Adds `digital.pragmatech.testing:spring-test-profiler` (scope `test`) when it is absent. The
   version defaults to `0.1.2` and can be overridden with the `axelix.spring-test-profiler.version`
   property.
2. Ensures `META-INF/spring.factories` on the test classpath registers the Axelix
   `TestExecutionListener` and `ApplicationContextInitializer`. An existing test `spring.factories` is
   **merged** (the Axelix values are appended to those keys), not overwritten.
3. Ensures `org.thymeleaf:thymeleaf` is at least `3.1.5.RELEASE` on the test classpath (required by
   the profiler's HTML report renderer): it is added at the floor when absent, or its version is
   raised in place — preserving the existing scope — when it is declared below the floor.

## Usage

`<extensions>true</extensions>` is **required** — it activates the lifecycle participant that injects
the test dependencies before Maven resolves the test classpath. Without it only the `spring.factories`
file is written.

```xml
<build>
  <plugins>
    <plugin>
      <groupId>com.axelixlabs</groupId>
      <artifactId>axelix-maven-plugin</artifactId>
      <version>1.0.0-M2</version>
      <extensions>true</extensions>
      <executions>
        <execution>
          <id>axelix-test-profiler</id>
          <goals>
            <goal>prepare-test-profiler</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

The `prepare-test-profiler` goal binds to the `process-test-resources` phase by default.

## Tests

- Unit tests (`./gradlew :plugins:axelix-maven-plugin:test`) cover the `spring.factories` merge logic
  and the Thymeleaf version policy.
- Integration tests (`./gradlew :plugins:axelix-maven-plugin:integrationTest`) drive a real Maven
  build against the bundled Spring Boot 2 and Spring Boot 4 sample projects under
  `src/integrationTest/resources/it`, asserting the injected test classpath and the generated
  `spring.factories`. They publish the plugin to the local Maven repository first, require a Maven
  installation (discovered from `MAVEN_HOME`/`M2_HOME`, or SDKMAN), and are skipped when none is
  found. They are opt-in and not part of `check`.
