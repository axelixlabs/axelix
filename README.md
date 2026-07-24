![Axelix Logo (Light)](docs/static/img/logo.svg#gh-light-mode-only)
![Axelix Logo (Dark)](docs/static/img/logo-dark.svg#gh-dark-mode-only)

[![License](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](LICENSE)
[![Nightly Heavy Tests](https://github.com/axelixlabs/axelix/actions/workflows/nightly.yaml/badge.svg)](https://github.com/axelixlabs/axelix/actions/workflows/nightly.yaml)
[![Security Check](https://github.com/axelixlabs/axelix/actions/workflows/nightly-security-check.yaml/badge.svg)](https://github.com/axelixlabs/axelix/actions/workflows/nightly-security-check.yaml)
[![Nightly Master E2E](https://github.com/axelixlabs/axelix/actions/workflows/nightly-master-e2e.yaml/badge.svg?branch=master)](https://github.com/axelixlabs/axelix/actions/workflows/nightly-master-e2e.yaml)

**Axelix is an open-source tool that surfaces the production problems your Spring Boot app hides from
you, then lets you inspect and change any running service from a web console or an AI agent.**

You know the drill. The app passes every test on your laptop and still falls over in staging. A
property resolves to a value nobody remembers setting. A `@ConditionalOnMissingBean` quietly wires in
the wrong implementation. A `Pageable` query with a `JOIN FETCH` pulls the whole table into heap.
`open-in-view` is still `true` in production. Axelix catches this class of problem before it pages
you, and when something is already broken it shows you the live beans, properties, transactions, and
logs of the running JVM, with no redeploy and no SSH.

## What Axelix does

Axelix reads a running Spring Boot service and flags the concrete anti-patterns and misconfigurations
that pass locally but cost you performance, stability, or security in production. It works on a plain
Spring Boot app, with no rewrite and no new framework: apply the fixes it points at and your memory 
footprint, throughput, GC pauses, and startup time will drastically improve.

Axelix analyses your app on every layer, from the JVM up through the framework and into the data layer:

- **JVM & deployment pitfalls.** Project Leyden not adopted, Compact Object Headers not enabled on
  Java 25, a Docker image that copies the fat jar instead of using Spring Boot layered images, GC
  logging not configured etc.
- **Enterprise anti-patterns.** Undetected N+1 queries, blocking network calls inside an active
  transaction, in-memory pagination performed by Hibernate, eager fetch strategies left implicit etc.
- **Configuration problems.** Open Session In View left enabled, virtual threads not adopted,
  `spring.jpa.show-sql=true` in production, actuator endpoints exposed via a `*` wildcard.

## Look inside a running application

Beyond detection, Axelix lets you inspect the live state of a running Spring Boot application, which
is what advanced runtime debugging really comes down to. Every capability below is available in the
web console and as an MCP tool, so the same information is avaialble to a human and an AI agent (according 
to configured RBAC policies, of course):

- **Configuration properties.** The effective value of every `@Value` and bound prefix, and the exact
  source it actually came from.
- **Beans & conditions.** The live bean graph plus the `@Conditional` verdicts that explain why each
  bean is here, or is not, including which auto-configurations matched.
- **Environment.** Every configuration source the JVM sees, ranked by precedence, with selective
  masking of sensitive values.
- **Loggers.** Flip log levels per package or class, live, no redeploy and no SSH, fully audited.
- **Transactional inspection.** See the profiles of your `@Transactional` methods and their potential problems.
- **Caches.** Cache managers, hit/miss rates, evict and clear.
- **Scheduled tasks.** Inspect cron and fixed-delay tasks; toggle, force-run, or change expressions
  live.
- **Thread dump, heap dump, garbage collector, metrics.** JVM diagnostics on demand plus live GC and
  Micrometer feeds.
- **Instance details.** Service metadata, build and git info (commit SHA, branch, build time,
  version), JVM, JDK vendor, GC, and Spring/Spring Boot/Spring Cloud versions actually running.

## How it works

Axelix has one central process, **Axelix Master**, that talks over HTTP to every Spring Boot service
you want to manage. Each service opts in by adding the **Axelix Starter** dependency, which exposes
the endpoints Master reads from and drives, and **Axelix Plugin** (Gradle or Maven build plugin).

The web UI and the MCP server both live inside Master, so the same live data is available both to humans 
and to AI agents.

```
                                         ┌───────────────────────┐
                                         │ Spring Boot Service   │
                         ┌──────────────▶│  + Axelix Starter     │
┌──────────┐         ┌───┴────────┐      └───────────────────────┘
│    UI    │─REST───▶│   Axelix   │      ┌───────────────────────┐
└──────────┘         │   Master   │─────▶│ Spring Boot Service   │
┌──────────┐         │ (UI + MCP) │      │  + Axelix Starter     │
│ AI Agent │─MCP────▶│            │      └───────────────────────┘
└──────────┘         └────────────┘                 ...
```

Master reads state from, and pushes mutations to, each managed service over HTTP. A service shows up
in Master either through **auto-discovery** (Master polls the Kubernetes API for services shipping
the Axelix Starter) or through **self-registration** (the starter registers with Master on startup
and heartbeats later to signal its alive). 

Master persists its own state in a database (SQLite by default; PostgreSQL or MySQL
for anything beyond a small deployment). Authentication for both the UI and MCP is available via
Basic auth or OAuth2/OIDC, with a single role model gating every human and agent identity.

See the [Architecture docs](docs/docs/product/architecture.mdx) for the full picture, including
multi-master deployments and the security model.

## Installation

Getting started is two steps: run **Axelix Master**, then add the **Axelix Starter** and build plugin
to each Spring Boot service you want to manage.

> The snippets below pin `1.0.0` for illustration. Check the
> [Releases page](https://github.com/axelixlabs/axelix/releases) for the latest published tag.

### 1. Run Axelix Master

Master listens on port `8080` and bundles the UI, so there is nothing extra to build for the web
interface. Pick whichever shape matches how you ship the rest of your services.

**As a JAR.** Download `axelix-1.0.0.jar` from the
[Releases page](https://github.com/axelixlabs/axelix/releases) and run:

```bash
java \
  -Daxelix.master.auth.jwt.algorithm=HMAC512 \
  -Daxelix.master.auth.jwt.signing-key=replace-with-a-long-random-secret \
  -jar axelix-1.0.0-M1.jar
```

**With Docker.** The release image is published to GitHub Container Registry:

```bash
docker run --rm -p 8080:8080 \
  -e JAVA_OTHER_ARGS="\
    -Daxelix.master.auth.jwt.algorithm=HMAC512 \
    -Daxelix.master.auth.jwt.signing-key=replace-with-a-long-random-secret \
    -Daxelix.master.auth.options.super-admin.credentials.password=replace-me" \
  ghcr.io/axelixlabs/axelix:1.0.0-M1
```

**On Kubernetes.** Install the first-party Helm chart, which also wires the RBAC needed for
in-cluster auto-discovery:

```bash
helm repo add axelix https://axelixlabs.github.io/helm-charts
helm repo update
helm install axelix axelix/axelix \
  --namespace axelix --create-namespace \
  --values values.yaml
```

Master then serves the UI at `http://localhost:8080`. It ships with a built-in super-admin account
(`admin / admin`) and an unset JWT signing key, so **change both before exposing Master to anyone
else.** A Docker Compose example and the full configuration reference (database, auth, discovery,
MCP) are in [Configuring Master](docs/docs/installation/configuring-master.mdx).

### 2. Add the Axelix Starter and build plugin to your Spring Boot service

Each service needs two things: the **runtime starter**, which exposes the endpoints Master reads and
drives, and the **build plugin**, which collects build, git, and project information at build time and
bundles it into the jar so features like Instance details work.

First, declare the starter coordinate matching your Spring Boot major version:

```kotlin
// Spring Boot 4.x
implementation("com.axelixlabs:axelix-spring-boot-4-starter:1.0.0")

// Spring Boot 3.x
implementation("com.axelixlabs:axelix-spring-boot-3-starter:1.0.0")

// Spring Boot 2.x
implementation("com.axelixlabs:axelix-spring-boot-2-starter:1.0.0")
```

Then apply the Axelix build plugin. With Gradle:

```kotlin
plugins {
    id("com.axelixlabs.axelix") version "1.0.0"
}
```

Or, with Maven:

```xml
<plugin>
  <groupId>com.axelixlabs</groupId>
  <artifactId>axelix-maven-plugin</artifactId>
  <version>1.0.0</version>
</plugin>
```

Finally, expose the Axelix actuator endpoints so Master can reach them:

```properties
management.endpoints.web.exposure.include=health,axelix-metadata,axelix-beans,axelix-caches,axelix-conditions,axelix-configprops,axelix-details,axelix-env,axelix-feign,axelix-gc,axelix-heap-dump,axelix-loggers,axelix-metrics,axelix-scheduled-tasks,axelix-thread-dump
```

The full setup (sharing the JWT signing key with Master, self-registration, sanitizing sensitive
property values) is documented in
[Configuring the Spring Boot Starter](docs/docs/setting-up-spring-boot-service/configuring-axelix-starter/configuring-axelix-starter.mdx).

## Documentation

Full documentation lives at [axelix.io](https://axelix.io/) and under [`docs/`](docs/docs):

- [Introduction](docs/docs/product/introduction.mdx) and [Motivation](docs/docs/product/motivation.mdx)
- [Architecture](docs/docs/product/architecture.mdx)
- [Installation](docs/docs/installation/configuring-master.mdx)
- [Features reference](docs/docs/features/details.mdx)
- [UI Guide](docs/docs/ui-guide/dashboard.mdx)

## Axelix OSS and Enterprise

This repository is the **Axelix OSS** open core, released under LGPL 3.0. Its job is to detect and
surface problems. The paid **Axelix Enterprise** edition (in development) builds on top of it to
enforce optimization policies across the whole ecosystem, at scale.

## Contributing

If you are interested in contributing to the Axelix project, please read our
[contribution guidelines](CONTRIBUTING.adoc).
