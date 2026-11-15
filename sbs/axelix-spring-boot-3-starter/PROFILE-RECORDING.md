# Local profile decision recorder

Build the opt-in startup agent:

```sh
./gradlew :sbs:axelix-spring-boot-3-starter:profileAgentJar
```

Start a Spring Boot 3 application with the generated agent JAR on the filesystem:

```sh
java -javaagent:/path/to/axelix-spring-boot-3-starter-1.4.0-SNAPSHOT-profile-agent.jar -jar application.jar
```

The agent observes normal returns from Spring Framework 6's `ProfileCondition.matches`.
It preserves Spring's result and never re-evaluates a condition. Failed instrumentation
or metadata capture is reported to stderr; absence of a record is not evidence of a match.
No runtime attachment or JVM attachment permission is required.

`ProfileDecisionRecorder` retains immutable observations in an internal, synchronized
list per bean factory (or per registry when no bean factory is available). Keys are weak
so the recorder does not keep application contexts alive. For internal inspection:

```java
ProfileDecisionRecorder.snapshot(context.getBeanFactory());
```

Each observation contains the declaration's class and method name (empty for a class),
profile expressions, active and default profiles at evaluation time, and the actual
boolean result. Multiple expressions are alternatives, as in Spring's `@Profile`.
Repeated checks are retained. There is no endpoint, master integration, or change to
the existing conditions feed. Without `-javaagent`, nothing is recorded.

A negative observation means that Spring's profile check rejected that declaration in
that evaluation. It does not establish final bean absence: another declaration may
register the same bean name. Bean names and overloaded method signatures are not
resolved by this prototype. A rejected configuration is recorded as a configuration;
its unexamined bean methods get no fabricated observations. Conditions bypassed by
short-circuiting and conditions that throw also get no observations.

Run the instrumented integration tests separately from ordinary starter tests:

```sh
./gradlew :sbs:axelix-spring-boot-3-starter:profileAgentTest
```

The agent relies on Spring internals. Its interception point must be checked when
upgrading Spring; the integration tests verify actual registration outcomes.
