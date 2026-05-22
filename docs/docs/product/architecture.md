# Architecture

Axelix has one central application — **Axelix Master** — that talks to every Spring Boot service you want to manage through an **Axelix Starter** dependency on the service side. The web UI and the MCP server both live inside Master, so the same data is served to a browser and to an AI agent from a single process.

```
                                         ┌───────────────────────┐
                                         │ Spring Boot 2 Service │
                         ┌──────────────▶│  + Axelix Starter     │
                         │               └───────────────────────┘
┌──────────┐         ┌───┴────────┐      ┌───────────────────────┐
│    UI    │─REST───▶│            │─────▶│ Spring Boot 3 Service │
└──────────┘         │   Axelix   │      │  + Axelix Starter     │
                     │   Master   │      └───────────────────────┘
                     │ (UI + MCP) │      ┌───────────────────────┐
┌──────────┐         │            │─────▶│ Spring Boot 2 Service │
│ AI Agent │─MCP────▶│            │      │  + Axelix Starter     │
└──────────┘         └───┬────────┘      └───────────────────────┘
                         │               ┌───────────────────────┐
                         └──────────────▶│ Spring Boot 3 Service │
                                         │  + Axelix Starter     │
                                         └───────────────────────┘
```

## Components

- **[Axelix Master](../setting-up-master-ui/what-is-master.md)**: Serves the UI, exposes the REST API the UI consumes, runs the bundled MCP server, talks to every managed service over HTTP, and holds the instance registry.
- **[Axelix Spring Boot Starter](../setting-up-spring-boot-service/what-is-axelix-starter.md)**: Separate starter modules exist for [Spring Boot 2](../setting-up-spring-boot-service/configuring-axelix-starter/configuring-axelix-starter.mdx#add-the-dependency), [Spring Boot 3](../setting-up-spring-boot-service/configuring-axelix-starter/configuring-axelix-starter.mdx#add-the-dependency), and Spring Boot 4 (Spring Boot 4 is still in development — stay tuned for news) — pick the one that matches your application. The starter exposes the endpoints Master needs (beans, environment, caches, scheduled tasks, and the rest) and can optionally self-register with Master on startup.

## Communication

- **Browser ⇄ Master**: REST/JSON over the same origin Master serves the static front-end from.
- **AI agent ⇄ Master**: [Model Context Protocol](https://modelcontextprotocol.io/docs/getting-started/intro) over HTTP. See [MCP server authentication](../setting-up-master-ui/authentication/authentication.mdx#mcp-server-authentication) for the credential schemes.
- **Master → managed service**: HTTP to the Axelix Starter's endpoints on the service. Every read (beans, environment, scheduled tasks, and the rest) and every mutating action (set a logger level, clear a cache, trigger a task) is a synchronous HTTP call.
- **Managed service → Master**: used only when self-registration is on. The starter posts a registration on startup and heartbeats it; the payload carries the URL Master should reach the service at.

## Service discovery

A service shows up in Master's instance registry through one of two paths:

- [Master-side auto-discovery](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery): Master polls the platform's API to find services that ship the Axelix Starter. [Kubernetes](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery), [Docker](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery) and [Docker Compose](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery) are supported today.
- [Starter-side self-registration](../setting-up-spring-boot-service/configuring-axelix-starter/configuring-axelix-starter.mdx#option-b--self-registration): the Axelix Starter posts a registration to Master on startup and renews it on a heartbeat interval.

Both paths feed the same instance registry inside Master, and Master evicts an instance after a configurable period of heartbeat silence, so unhealthy or removed instances drop off the [Wallboard](../ui-guide/wallboard.md) on their own.

## Deployment & scaling

- **Master is the central process.** All managed services route through it; nothing else needs to be deployed alongside (no broker, no separate collector). Managed services scale independently — Master just adds or evicts registry entries.
- **Database is the durability boundary.** SQLite is enough for a single-node trial and is the bundled default, but the file does not survive a container restart without a mounted volume and cannot back more than one Master at a time. PostgreSQL or MySQL is the configuration for anything beyond that — see [Database](../setting-up-master-ui/configuring-master/configuring-master.mdx#database).
- **Network reachability is on the operator.** Master must be able to call every managed service over HTTP from its own network — that is the wire every read and mutation runs on. Self-registration heartbeats are the only call in the opposite direction.

## Multi-master deployments

The two registration paths can be split across separate Master processes that share one database, so each Master owns a clear role:

```
              ┌──────────────┐                  ┌──────────────┐
              │      UI      │                  │  AI Agents   │
              └──────┬───────┘                  └──────┬───────┘
                     │REST                             │MCP
                     │                                 │
 ┌───────────────────┴─────────────────────────────────┴───────────────────┐
 │                              Load Balancer                              │
 └──────────┬───────────────────────────────────────────────────┬──────────┘
            │                                                   │
            │                                                   │
 ┌──────────┴──────────┐                             ┌──────────┴──────────┐
 │      Master A       │                             │      Master B       │
 │  auto-disco only    │                             │  self-registration  │
 └──┬─────────────┬────┘                             └─┬─────────────┬─────┘
    │             │                                    │             │
    │             │                                    │             │
    │     ┌───────┴────────────────────────────────────┴────────┐    │
    │     │                    Shared DB                        │    │
    │     │                                                     │    │
    │     └─────────────────────────────────────────────────────┘    │
    │                                                                │
┌───┴──────────────────────┐                       ┌─────────────────┴─────┐
│ Spring Boot Services     │                       │ Spring Boot Services  │
│ (Spring Boot 2 - 3)      │                       │ (Spring Boot 2 - 3)   │
└──────────────────────────┘                       └───────────────────────┘
```

| Property                                            | Master A | Master B |
|-----------------------------------------------------|----------|----------|
| `axelix.master.discovery.auto.enabled`              | `true`   | `false`  |
| `axelix.master.discovery.self-registration.enabled` | `false`  | `true`   |

In this example both Masters share one registry, Master A polls Kubernetes for its services, and Master B accepts self-registration from its own services.

## Security model

- **Shared JWT signing key.** The same `signing-key` is configured on Master and on every Axelix Starter. Master signs tokens to call starter endpoints, the starter signs heartbeats for self-registration. A key mismatch produces `401` and a rejected call rather than silent drift.
- **End-user authentication is separate from service-to-service.** Browser users sign in via the built-in super-admin account, local accounts, or OIDC, AI agents authenticate to the MCP server through its own credential schemes. See [Authentication](../setting-up-master-ui/authentication/authentication.mdx) for the full credential model.
- **TLS is the operator's responsibility.** Master listens on plain HTTP by default. Put it behind a TLS-terminating proxy and set `axelix.master.auth.cookie.secure=true` for any non-local deployment.
