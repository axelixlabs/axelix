# Instructions for AI Agents

## Overview 

This repository contains the source code of [Axelix](https://www.rabbitmq.com/) project. Axelix is the software product that helps developers and Q/A engineers to debug/test/monitor Spring Boot Java/Kotlin applications.

### Architecture

The Axelix as a product consists of:

- **The UI/Front-end**. The source code of it is located under `front-end` directory. This is the React Application written in TypeScript, built using Vite.
- **Axelix Master** (or just **'master'**). The application that acts as the backend for the UI/Front-End. The Axelix Master is a Java 17 application. The source code of Axelix master is located inside the **master** directory.
- **Spring Boot Starter** (or just **'sbs'**) modules. Axelix Master communicates with the Java/Kotlin applications, that include Spring Boot Starter of various versions (like starter for Spring Boot 2, or starter for Spring Boot 3). The source code of all the starters is located inside the **sbs** directory.

### Repository Structure

The Gradle multi-project layout is defined in `settings.gradle.kts`. The included modules are: `:master`, `:sbs`, `:sbs:axelix-spring-boot-2`, `:sbs:axelix-spring-boot-3`, `:common`, `:common:api`, `:common:auth`, `:common:domain`, `:common:utils`.

Below is the full directory overview:

- `master/`: The Axelix Master backend application (Java 17, Spring Boot). Contains a single `src/` directory with its source code and a `build.gradle.kts`.
- `sbs/`: Parent directory for the Spring Boot Starter modules.
  - `sbs/axelix-spring-boot-2/`: Starter library for Spring Boot 2 applications.
  - `sbs/axelix-spring-boot-3/`: Starter library for Spring Boot 3 applications.
- `common/`: Shared modules used by both the Axelix Master and any of the Spring Boot Starters.
  - `common/api/`: Shared API definitions (DTOs, contracts) between master and starters.
  - `common/auth/`: Shared authentication-related code.
  - `common/domain/`: Shared domain model classes.
  - `common/utils/`: Shared utility classes.
- `front-end/`: The React/TypeScript UI application, built with Vite.
  - `front-end/src/`: Application source code organized into `api/`, `components/`, `helpers/`, `hooks/`, `i18n/`, `layout/`, `models/`, `pages/`, `routes/`, `services/`, `store/`, `tests/`, and `utils/` subdirectories.
  - `front-end/cypress/`: Cypress end-to-end tests (`e2e/` and `support/` subdirectories).
- `buildSrc/`: The Gradle `buildSrc` module containing convention plugins and shared build logic.
  - Convention plugins: `common.gradle.kts`, `sbs.gradle.kts`, `shared.gradle.kts`.
  - `Dependencies.kt`: Centralized dependency version declarations.
  - `binary/AxelixPropertiesPlugin.kt` and `binary/AxelixPropertiesGenerationTask.kt`: Custom Gradle plugin for generating Axelix properties at build time.
- `docs/`: Project documentation site built with Docusaurus. Contains `docs/` (user-facing documentation), `blog/`, `legacydocs/`, and `src/` subdirectories.
- `infra/`: Infrastructure-as-code configuration.
  - `infra/cloud/terraform/`: Terraform definitions for cloud deployments.
- `gradle/`: Contains the Gradle wrapper files.
- `.github/`: GitHub-specific configuration.
  - `.github/workflows/`: CI/CD workflow definitions (`backend_pull_requests.yaml`, `frontend_pull_requests.yaml`, `release.yaml`, `deploy-test.yaml`).
  - `.github/actions/`: Reusable composite actions (`build-backend/`, `build-frontend/`).
  - `.github/ISSUE_TEMPLATE/`: Issue templates.

Notable root-level files:

- `build.gradle.kts`: Root Gradle build script. Applies Spotless (code formatting), PMD (static analysis), Error Prone with NullAway (null-safety checks) to all subprojects. Also configures Maven publishing (Nexus and GitHub Packages) and PGP artifact signing.
- `settings.gradle.kts`: Gradle settings defining the multi-project structure.
- `gradle.properties`: Gradle properties (including `axelixVersion`).
- `pmd.ruleset.xml`: PMD static analysis ruleset.
- `LICENSE_HEADER`: License header text applied to all Java and TypeScript source files by Spotless.
- `Dockerfile`: Multi-stage Docker build that packages the master JAR and front-end dist into an Alpine JRE image.
- `CONTRIBUTING.adoc`: Contribution guidelines.

### Building

The backend modules are being built via Gradle build tool. The Axelix Project is a gradle multi-project project.

Therefore, for Java source code, you can simply run:

```
./gradlew clean build
```

That is going to also run the linting checks, like spotless and pmd. You can select the module to build, or run the build on the entire monorepo if you see fit.

For the front-end, please, inspect the `front-end/packages.json` in order to understand the possible commands that can be run (for testing, linting and building the dist).

### CI/CD

The CI/CD that is used by this project is GitHub Actions. The source code for the GitHub Actions can be found in the `.github` directory.

### Writing Tests

When you're writing tests, please, make sure to follow the principles:

1. Tests must be isolated, meaning, the execution or the outcome of execution of the given single test should NOT affect any other tests.
2. When writing individual tests, make sure that tests always follow the arrange/act/assert principle, insert the one-liner comments before each section:

- // given.
- // when.
- // then.

3. It is often the case that within the Junit test class some test methods are related, and some do not. For instance, some test methods test the same method of the public API just under different conditions. When you notice this pattern it is generally a good idea to organize those tests inside dedicated `@Nested` inner classes.

In a very rare occasions the principles above MAY be violated, but you should always ask before attempting to do so and explain your reasoning. 

### Overall Behavioral Guidelines

## 1. Think Before Coding

**Don't assume. Don't hide confusion. Surface tradeoffs.**

Before implementing:
- State your assumptions explicitly. If uncertain, ask.
- If multiple interpretations exist, present them - don't pick silently.
- If a simpler approach exists, say so. Push back when warranted.
- If something is unclear, stop. Name what's confusing. Ask.

## 2. Simplicity First

**Minimum code that solves the problem. Nothing speculative.**

- No features beyond what was asked.
- No abstractions for single-use code.
- No "flexibility" or "configurability" that wasn't requested.
- No error handling for impossible scenarios.
- If you write 200 lines, and it could be 50, rewrite it.

Ask yourself: "Would a senior engineer say this is overcomplicated?" If yes, simplify.

## 3. Surgical Changes

**Touch only what you must. Clean up only your own changes.**

When editing existing code:
- Don't "improve" adjacent code, comments, or formatting unless explicitly asked.
- Don't refactor things that aren't broken.
- Match existing style, even if you'd do it differently.
- If you notice unrelated dead code, mention it - don't delete it unless explicitly asked.

When your changes create orphans:
- Remove imports/variables/functions that YOUR changes made unused.
- Don't remove pre-existing dead code unless explicitly asked.

The test: Every changed line should trace directly to the user's request.

## 4. Goal-Driven Execution

**Define success criteria. Loop until verified.**

Transform tasks into verifiable goals:
- "Add validation" → "Write tests for invalid inputs, then make them pass"
- "Fix the bug" → "Write a test that reproduces it, then make it pass"
- "Refactor X" → "Ensure tests pass before and after"

For multi-step tasks, state a brief plan:
```
1. [Step] → verify: [check]
2. [Step] → verify: [check]
3. [Step] → verify: [check]
```

Strong success criteria let you loop independently. Weak criteria ("make it work") require constant clarification.
