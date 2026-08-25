# Instructions for AI Agents

## Overview 

This repository contains the source code of [Axelix](https://www.axelix.io/) project. More about the product can be read in README.md

### Architecture

All of the Axelix code can be found in this repository (the one you're currently in). Axelix is the monorepo.

Axelix as a product consists of:

#### 1. Axelix Master

Sometimes just called **'master'**. This is the standalone Spring Boot application distributed as the JAR/Docker Image/Helm Chart. It "manages" the regular, end-user's Java applications.

Axelix Master also has the UI (written in React & TypeScript) which is located in */master/front-end*. This front-end code eventually ends up in the Master's JAR file and server via Tomcat Web Server.

Axelix Master also has a built-in MCP server (backed by Spring AI) that works on the same port as the regular backend.

Source code of master (both Spring Boot backend and UI) can be found in the */master* dir.

#### 2. Axelix Spring Boot Starters

Sometimes abbreviated as 'sbs'. These are spring boot starters that end-user's are supposed to include in their runtime classpath. Axelix Master will not be able to detect Spring Boot microservice that does not include Spring Boot starter. These starters provide the core functionality that users see on the UI.

Their source code can be found in the */sbs/*** dir, e.g */sbs/axelix-spring-boot-4-starter*.

#### 3. Axelix Build Plugins

These are build system plugins (for Maven & Gradle) that generate a valuable information for the Axelix Spring Boot Starters. Without plugins, Axelix Master would also not be able to detect the managed services.

Their source code can be found in the */plugins/*** dir, e.g */sbs/axelix-gradle-plguin*.

### Versioning

Axelix as a project follows a lockstep versioning, i.e. all its components when released receive the same version, e.g. version 1.4.2.

### Building

The project has various build systems, but generally, you're supposed to build it with Make build system. You can find `Makefile` in the root (i.e. `./Makefile`).

In our case, make is just a "main" build system on top of (mostly) Gradle.

### CI/CD

The CI/CD that is used by this project is GitHub Actions. The source code for the GitHub Actions can be found in the `.github` directory.

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
