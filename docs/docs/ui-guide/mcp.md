# MCP

The **MCP** screen lists every tool that Axelix Master exposes through its bundled Model Context Protocol server. Each card is one tool an AI agent connected over MCP can invoke against any registered Spring Boot instance.

## When the MCP tab appears

The **MCP** link in the navigation bar is rendered only when the MCP server is enabled in Master. The gate is the `axelix.master.mcp-server.enabled` property, which defaults to `true`. In deployments that disable the server, the link is hidden and `/mcp-server` is unreachable. See [MCP server configuration](../setting-up-master-ui/configuring-master/configuring-master.mdx#mcp-server) for the property.

The MCP server itself authenticates AI agents separately from the browser session — credentials, the supported `Basic` and `Bearer` schemes, and per-tool authority gating are covered in [MCP server authentication](../setting-up-master-ui/authentication/authentication.mdx#mcp-server-authentication). This page describes only the read-only catalog UI; it does not change which tools an agent can call.

## The tool catalog

The catalog is a grid of cards. The search field at the top filters cards by title, and the counter on the right shows `<matches> / <total>`. The search matches anywhere in the tool title and is case-insensitive.

Every card has the same three parts:

- **Header** with the tool title and a coloured status dot — green when the tool is registered with the MCP server, red when it is currently unavailable.
- **Description** — the short blurb the tool advertises to MCP clients. Long descriptions are clipped to a fixed height; hover the description to see the full text in a tooltip.
- **Footer** with annotation tags — see [Tool annotations](#tool-annotations) below.

## Tool annotations

Each tool declares four boolean hints that tell MCP clients what kind of side effects they should expect when calling it. The card footer surfaces two of them inline and exposes all four through a popover.

**Inline tags** (always visible):

- **Read-only** — the tool does not change any state in the target Spring Boot instance.
- **Idempotent** — invoking the tool more than once with the same arguments has the same effect as invoking it once.

Each tag is coloured green when the hint is <code style={{color: 'green'}}>true</code> and orange when it is <code style={{color: 'orange'}}>false</code>. The visible value is the tool's own claim — not a guarantee about what the underlying endpoint actually does.

**Full annotations popover** — click the burger button on the right of the footer to open a panel showing all four hints with their boolean values:

- **Read-only** — the tool does not change any state in the target Spring Boot instance.
- **Idempotent** — invoking the tool more than once with the same arguments has the same effect as invoking it once.
- **Destructive** — the tool may delete data or otherwise apply changes that cannot be undone from the UI.
- **Open-world** — the tool reaches outside Master and the registered Spring Boot instances (for example, an external HTTP call).

## What is missing from this screen

The MCP page is a discovery surface, not a runner. From here you cannot:

- Invoke a tool against a Spring Boot instance — that happens from your MCP client (Claude Desktop, Cursor, a custom agent, etc.).
- Enable or disable individual tools — the catalog reflects what the master registered at startup.
- See past invocations or per-tool metrics.

To use the catalog, point your MCP client at `<master-base-url>/api/mcp` and authenticate as described in [MCP server authentication](../setting-up-master-ui/authentication/authentication.mdx#mcp-server-authentication).

:::info
An **Access Log** for MCP tool invocations is on the roadmap and will ship with Axelix Enterprise. Both the Access Log and Axelix Enterprise itself are still in development, so for now the MCP screen remains a read-only catalog without history or per-tool metrics.
:::
