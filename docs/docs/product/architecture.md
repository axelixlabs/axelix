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

- [Master-side auto-discovery](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery): Master polls the platform's API to find services that ship the Axelix Starter. Automatic discovery is only possible in [Kubernetes](../setting-up-master-ui/configuring-master/configuring-master.mdx#discovery--auto-discovery) as of now, and it is enabled by default in the official Axelix helm-chart.
- [Starter-side self-registration](../setting-up-spring-boot-service/configuring-axelix-starter/configuring-axelix-starter.mdx#option-b--self-registration): the Axelix Starter sends a registration request to Master on startup and heartbeats its over certain interval.

## Deployment & scaling

- **Master is the central process.** It serves as the brain that accepts all incoming requests from the UI, and then routes them to the particular service.
- **Database is the durability boundary.** Axelix master needs to maintain certain state, and for that it needs a database. By default, SQLite is used. Although SQLite is quite enough for a small deployments, keep in mind that sqlite db file does not survive a container restart without a mounted volume. PostgreSQL or MySQL is the configuration for anything beyond that — see [Database](../setting-up-master-ui/configuring-master/configuring-master.mdx#database).
- **Network reachability is on the operator.** Master must be able to call every managed service over HTTP from its own network — that is the wire every read and mutation runs on. Self-registration heartbeats are the only call in the opposite direction.

## Multi-master deployments

It is possible to deploy multiple instances of Axelix Master. In this case, it is the responsibility of the end-users to arrange for the shared database (e.g. either the same mounted sqlite file or the common PostgreSQL database): 

```
              ┌──────────────┐                  ┌──────────────┐
              │      UI      │                  │  AI Agents   │
              └──────┬───────┘                  └──────┬───────┘
                     │REST                             │MCP
                     │                                 │
 ┌───────────────────┴─────────────────────────────────┴───────────────────┐
 │                         Load Balancer (K8S Service)                     │
 └──────────┬───────────────────────────────────────────────────┬──────────┘
            │                                                   │
            │                                                   │
 ┌──────────┴──────────┐                             ┌──────────┴──────────┐
 │    Axelix Master    │                             │    Axelix Master    │
 │       pod "A"       │                             │       pod "B"       │
 └──┬─────────────┬────┘                             └─┬─────────────┬─────┘
    │             │                                    │             │
    │             │                                    │             │
    │     ┌───────┴────────────────────────────────────┴────────┐    │
    │     │                      Shared DB                      │    │
    │     │                                                     │    │
    │     └─────────────────────────────────────────────────────┘    │
    │                                                                │
┌───┴──────────────────────┐                       ┌─────────────────┴─────┐
│ Spring Boot Services     │                       │ Spring Boot Services  │
│ (Spring Boot 2 - 3)      │                       │ (Spring Boot 2 - 3)   │
└──────────────────────────┘                       └───────────────────────┘
```

## Security model

Authentication for AI Agents when using MCP and for humans when using the UI is possible via either [Basic authentication](https://datatracker.ietf.org/doc/html/rfc7617), or the [OAuth2/OIDC](https://datatracker.ietf.org/doc/html/rfc7617). Below is only the small overview, see [Authentication](../setting-up-master-ui/authentication/authentication.mdx) for the full credential model.

### Basic Authentication

In this case, it is assumed that the users will be created in the database that Axelix Master is connected to. The user profile will be stored in the Axelix Master database, which is fine for small deployments.

:::note
As of now, users can be created only manually through the UI by human engineer. The Terraform Provider is under way.
:::

When logging-in, the user or the AI agent will be prompted to provide the credentials, which are then going to be checked against the database. In case of the login from the UI form, the backend will send a dedicated auth cookie that will contain the JWT access-token.

In case of AI Agents usage, these credentials are simply going to be provided by the custom <code>"Basic ..."</code> header as it is defined in [RFC 7617](https://datatracker.ietf.org/doc/html/rfc7617).

:::important
Since there is no standard around the <code>"Basic"</code> authentication scheme for the AI Agent via MCP server, the AI Agent must present the credentials in the <code>"Basic"</code> header **in every request**, in case of Basic authentication for the Agent is used.
:::

### OAuth2 and OIDC

In modern enterprises, the SSO (Single Sign-On) is a common IAM pattern. It is often archived via OAuth2 and OIDC.

Axelix Master can be configured to authenticate the user by the external OIDC Identity Provider. In this case, Axelix Master will trust the OIDC provider token, and it also _may_ request some additional information about the current user from OIDC provider (e.g. by querying the <code>/userinfo</code> endpoint, if available).

### IAM. Important Nuances

- **Shared JWT signing key.** As of now, both Axelix Master and the Axelix Starter will use **the symmetrically encrypted JWT**.  That implies that the same `signing-key` must be configured on Master and on every Axelix Starter. Master signs tokens to call starter endpoints, the starter signs heartbeats for self-registration. A key mismatch produces `401` and a rejected call rather than silent drift.
- **TLS is the operator's responsibility.** Master listens on plain HTTP by default. Put it behind a TLS-terminating proxy and set `axelix.master.auth.cookie.secure=true` for any non-local deployment.
