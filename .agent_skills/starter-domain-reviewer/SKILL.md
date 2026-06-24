---
name: starter-domain-reviewer
description: Reviews changes in sbs/starter-domain for technology-agnostic domain logic, forbidden production dependencies, and Java 11+ compatibility. Use whenever a PR or diff touches sbs/starter-domain/, when the user asks to review starter-domain code, when evaluating starter-domain build.gradle.kts dependency changes, or when reviewing shared starter logic that must work across axelix-spring-boot-2/3/4 starters — even if the user only says "review this PR" and starter-domain files are in the diff.
---

# Starter-Domain Reviewer

Guide for AI agents reviewing changes in **`sbs/starter-domain`** — the shared, framework-neutral domain layer used by every Axelix Spring Boot starter (`axelix-spring-boot-2-starter`, `axelix-spring-boot-3-starter`, `axelix-spring-boot-4-starter`).

Apply this skill whenever a PR **creates, modifies, or deletes** files under `sbs/starter-domain/`, including `build.gradle.kts`.

## Module purpose

starter-domain is the module that is supposed to represent the domain logic of our starters that is independent from Spring, Hibernate, Junit or any technology that may differ in various different spring boot versions.

As a result, starter-domain must NEVER include the dependencies like:

- spring-boot
- hibernate
- jackson
- thymeleaf

and so on - anything that is subject to change in between major spring boot versions - cannot be used in starter-domain. This is an absolute must.

Note that however for the dependencies that are not ending up in the end-user's classpath - we're free to choose whatever we want. But again, since we must complie for java 11, we cannot use e.g. Spring Boot Test version 3, since it requires java 17. So for "test" dependencies this rule is generally eased, but again, we must consider the fact that we compile to java 11 bytecode.

starter-domain is supposed to be used by all spring boot starters that we have. As a result, the code that is written in the starter-domain module is supposed to be used in the java verisons from 11 up to the very latest. We do not support java 8.

## When to apply

- PR diff touches `sbs/starter-domain/**` (main, test, `concurrencyTest`, or `build.gradle.kts`).
- User asks to review starter-domain, starter shared logic, or framework-neutral starter code.
- User asks whether starter-domain changes are merge-ready.

If starter-domain is untouched, do not apply unless the user explicitly asks about starter-domain impact of changes elsewhere (e.g. a new type referenced from starter-domain).

## Review workflow

1. **Identify scope** — List changed files under `sbs/starter-domain/` (`git diff` / PR files API). Include `build.gradle.kts` and both `src/main` and `src/test` (plus `src/concurrencyTest` if present).
2. **Check production classpath** — Inspect `sbs/starter-domain/build.gradle.kts` for any new/changed `api`, `implementation`, `compileOnly`, or `runtimeOnly` dependencies. Flag anything version-sensitive or framework-specific.
3. **Scan main sources** — In `src/main/**`, search imports and public API signatures for forbidden framework types (see [Forbidden in production](#forbidden-in-production)).
4. **Check Java baseline** — Confirm `options.release = 11` / `JvmTarget.JVM_11` remain set; scan for Java 17+ language APIs or bytecode-only features.
5. **Review design fit** — Does new logic belong in starter-domain, or in a version-specific starter module? Prefer abstractions over framework types.
6. **Review tests separately** — Apply [test-reviewer](../test-reviewer/SKILL.md) for test quality; apply this skill's test-dependency rules below.
7. **Report** using the output format at the end. **Block merge** on any 🔴 finding.

## Standards

### 1. Production dependencies — merge-blocking

**Rule:** Nothing that varies across Spring Boot major versions may appear on the **production** classpath of `starter-domain`.

**Forbidden dependency groups** (non-exhaustive — block anything in the same category):

| Category | Examples |
|----------|----------|
| Spring | `spring-boot-*`, `spring-*`, `spring-cloud-*`, `spring-kafka`, `spring-data-*` |
| Persistence | `hibernate-*`, `spring-boot-starter-data-jpa`, JDBC drivers as `implementation` |
| Serialization / templating | `jackson-*`, `thymeleaf`, `freemarker`, `gson` |
| Web stacks | `spring-boot-starter-web`, `spring-boot-starter-webflux`, `jakarta.servlet-*`, `javax.servlet-*` |
| Test-only libs on main | `junit-*`, `mockito-*`, `assertj-*`, `spring-boot-starter-test` |

**Allowed production dependencies** (typical):

- `project(":common:auth")`, `project(":common:api")`, `project(":common:domain")`, `project(":common:utils")` — already declared as `api`
- JDK / `java.*`, `javax.sql.*` (e.g. `DataSource`)
- `org.jspecify:jspecify` (via `shared` plugin)
- Other small, version-stable libraries only if they do **not** pull Spring/Jackson/Hibernate onto the end-user classpath

**Flag as 🔴 when:**

- `build.gradle.kts` adds `implementation` / `api` / `runtimeOnly` for any forbidden group above.

**Flag as 🟡 when:**

- `compileOnly` is used for a framework type (discouraged in starter-domain; prefer string-based detection or an interface in starter-domain with impl in the starter module). 

### 2. No framework types in main code — merge-blocking

Production code (`src/main/**`) must not **import** or **expose** types from version-sensitive frameworks.

**Scan commands** (run on changed main files or whole module when in doubt):

```bash
rg 'import (org\.springframework|org\.hibernate|com\.fasterxml|org\.thymeleaf|jakarta\.servlet|javax\.servlet)' sbs/starter-domain/src/main
```

**Acceptable patterns already in the codebase:**

- **Abstraction interfaces** with framework-specific implementations in starter modules — e.g. `EndpointPathMatcher` in starter-domain; Spring `PathPattern` adapter lives in `axelix-spring-boot-*-starter`.
- **String-based class presence checks** — e.g. `SpringCloudVersionResolver` referencing `"org.springframework.cloud.client.CommonsClientAutoConfiguration"` without importing Spring.
- **JDK and `common/*` types** — e.g. `com.axelixlabs.axelix.common.domain.http.HttpMethod` instead of `org.springframework.http.HttpMethod`.
- **Javadoc references** to Spring class names (documentation only, not compile-time deps).

**Flag as 🔴 when:**

- `src/main` imports `org.springframework.*`, `org.hibernate.*`, `com.fasterxml.jackson.*`, `org.thymeleaf.*`, etc.
- Public method signatures, fields, or return types use framework classes (`ApplicationContext`, `BeanFactory`, `PathPattern`, `ObjectMapper`, `EntityManager`, …).
- Spring/Jakarta annotations on production types (`@Component`, `@Configuration`, `@Autowired`, `@Transactional`, …).
- Production code uses Spring-specific utilities when a JDK or starter-domain utility exists (`ClassUtils` / `ProxyUtils` in this module — not Spring's).

**Flag as 🟡 when:**

- Logic duplicates Spring behavior inline with a comment linking to Spring source — acceptable if unavoidable, but suggest extracting a named utility or documenting why an interface boundary is not possible.

### 3. Java 11+ compatibility — merge-blocking

starter-domain compiles to **Java 11 bytecode** (`options.release = 11`, Kotlin `JvmTarget.JVM_11`). It must run on **Java 11 through the latest supported Java**. Java 8 is not supported.

**Flag as 🔴 when:**

- `build.gradle.kts` raises `release` / `jvmTarget` above 11 for this module.
- Main or test code uses Java **17+** language features: `record`, `sealed` types/interfaces, pattern-matching `switch`, multi-line string blocks requiring 15+ if SB2 consumers compile against this API surface.
- Main or test code calls APIs added **after Java 11** without guards — e.g. `List.of` / `Map.of` (OK, Java 9+), but not `Stream.toList()` (Java 16+) in code that must stay 11-clean; prefer `collect(Collectors.toList())`.
- Test dependencies require Java 17+ (e.g. Spring Boot 3.x `spring-boot-starter-test` as the test platform for this module).

**Flag as 🟡 when:**

- `String.isBlank()` / `String.strip()` (Java 11) — fine.
- Kotlin used only in `concurrencyTest` with JVM 11 target — fine.

### 4. Test and concurrencyTest dependencies — relaxed but bounded

Test-scoped dependencies (`testImplementation`, `testRuntimeOnly`, `concurrencyTest` suite deps) **do not** ship to end users, so Spring Boot Test and similar are allowed **only in test sources**.

Current baseline in `build.gradle.kts`: Spring Boot **2.7.18** BOM for tests (Java 11 compatible).

**Flag as 🔴 when:**

- Test dependency upgrade forces Java 17+ for compiling or running `starter-domain` tests.
- Framework types leak from tests into `src/main` (e.g. copying a Spring helper into production to support a test).

**Flag as 🟡 when:**

- New tests use `@SpringBootTest` where a plain unit test with mocks would suffice — suggest simplification, not a merge block.

### 5. Placement and API design

**Ask:** Does this code belong in starter-domain, or in a version-specific `axelix-spring-boot-*-starter` module?

| Belongs in starter-domain | Belongs in a starter module |
|---------------------------|-----------------------------|
| Pure domain logic, algorithms, value types | Spring `@Configuration`, `@Bean`, auto-configuration |
| Interfaces implemented differently per SB version | `EnvironmentPostProcessor`, `BeanPostProcessor`, Actuator endpoint wiring |
| Registration/metadata assembly without Spring types | Code that imports `ApplicationContext` or actuator infrastructure |

**Flag as 🔴 when:**

- Version-specific branching for Spring Boot 2 vs 3 vs 4 is added to starter-domain — belongs in the respective starter with shared contracts in starter-domain.

**Flag as 🟡 when:**

- New public API is overly broad for all starters; suggest narrowing or package-private default with public interface.

## Severity

| Level | Meaning | Merge |
|-------|---------|-------|
| 🔴 **Blocking** | Forbidden production dep, framework import/signature in main, Java baseline violation, version-specific logic in starter-domain | **Do not approve** until fixed |
| 🟡 **Suggestion** | Test simplification, `compileOnly` smell, API shape, duplication | Approve with comments optional |
| 🟢 **Note** | Style, naming, unrelated polish | Informational |

Any 🔴 finding means: **we cannot merge such PR** (for starter-domain defects).

## Blocking a PR

When the review has **one or more 🔴 blocking findings**:

1. **Add the label** `blocked-by-ai-reviewer` (if reviewing on GitHub).
2. **Post one PR comment** — single top-level summary, brief bullets.

Example:

```markdown
## starter-domain review — blocked

This PR cannot be merged until these starter-domain issues are fixed:

- [production dep] `build.gradle.kts` — added `implementation("org.springframework.boot:spring-boot-starter-web")`
- [framework type] `FooService.java` — public method returns `org.springframework.context.ApplicationContext`
- [java baseline] `Bar.java` — uses `record` (requires Java 16+)
```

Do **not** add the label when there are only 🟡 / 🟢 items.

## Review output format

```markdown
## starter-domain review

**Scope:** [files / areas reviewed]

### Dependency check
| Dependency / import | Scope | Status |
|---------------------|-------|--------|
| [coordinate or import] | main / test | ✅ OK / ❌ forbidden |

### Findings

#### 🔴 Blocking
- **[file:line]** — [production dep | framework type | java baseline | misplaced logic] — [what's wrong] — [what to do]

#### 🟡 Suggestions
- **[file:line]** — [brief suggestion]

### Verdict
**[Approve / Request changes — N blocking issue(s)]**
```

When the verdict is **Request changes**, apply [Blocking a PR](#blocking-a-pr).

For PRs that also change tests, cross-reference [test-reviewer](../test-reviewer/SKILL.md) findings in a separate **Test review** section.

## Quick checklist

```
- [ ] No new version-sensitive deps on api/implementation/runtimeOnly in build.gradle.kts
- [ ] src/main has no imports from Spring, Hibernate, Jackson, Thymeleaf, servlet APIs
- [ ] Public API uses JDK + common/* + starter-domain types only
- [ ] No Spring/Jakarta annotations on production types
- [ ] Module still targets Java 11 bytecode; no Java 17+ language features
- [ ] Test deps remain Java 11-compatible (SB 2.7.x test BOM or equivalent)
- [ ] New logic is framework-neutral; version-specific code stays in axelix-spring-boot-*-starter
- [ ] Framework integration uses interfaces or string-based detection, not direct coupling
```

## Reference: module layout

```
sbs/starter-domain/
├── build.gradle.kts          # release 11; test BOM spring-boot 2.7.18
├── src/main/java/...         # production — strict rules apply
├── src/test/java/...         # tests — Spring allowed; Java 11 compile
└── src/concurrencyTest/kotlin/  # Lincheck suite; JVM 11 target
```

Consumers: `axelix-spring-boot-2-starter` (`compileOnly` + tests), `axelix-spring-boot-3-starter`, `axelix-spring-boot-4-starter` (`api` — starter-domain on user classpath).
