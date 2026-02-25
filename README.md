# Axelix

[![Axelix Logo (Light)](docs/static/img/logo.svg)](https://www.axelix.io)
[![Axelix Logo (Dark)](docs/static/img/logo-dark.svg)](https://www.axelix.io)

[![License](https://img.shields.io/badge/License-LGPL_v3-blue.svg)](LICENSE)

Axelix is an open-source solution for debugging, testing, and monitoring Java/Kotlin Spring Boot applications in real time. It is designed to empower teams that develop and operate Spring Boot microservices.

Axelix helps developers and Q/A engineers in:

- **Debugging** — Inspect beans, configuration properties, and environment at runtime; change log levels on the fly; capture thread dumps and heap exports; toggle scheduled tasks and caches for troubleshooting without redeploys.
- **Testing** — Validate configuration and property resolution across environments; test scheduling and cache behavior; exercise different scenarios by enabling or disabling features at runtime.
- **Monitoring** — View real-time metrics and health status of registered applications; monitor multiple instances and environments from a single dashboard.

## Architecture overview

![Architecture overview](docs/static/img/architecture.svg)

The **Axelix Master** (backend) discovers and registers Spring Boot applications that include the **Axelix Spring Boot Starter**. Users interact with the **UI/Front-end**, which talks to the Master; the Master proxies requests to the managed applications (e.g. in Kubernetes or Docker Compose).

## Contributing

If you are interested in contributing to the Axelix project, please read our [contribution guidelines](CONTRIBUTING.adoc).
