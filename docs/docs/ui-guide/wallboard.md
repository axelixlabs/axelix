# Wallboard

The Wallboard is the default landing page in Axelix. It shows every Spring Boot instance discovered by Axelix Master as a grid of compact cards, each with the instance's live health status and a quick summary of what it runs. Clicking a card opens the [Service Profile](./service-profile.md) for that instance.

## Service cards

Each card represents one discovered instance. The card header carries the service name and a status indicator on the right:

- A solid **green** dot — the instance is **UP**.
- A pulsing **red** dot — the instance is **DOWN**.
- A **question icon** — Axelix Master could not determine the instance's status (**UNKNOWN**).

Cards that are not **UP** are rendered with a status-coloured background so they stand out in the grid.

The card body lists the runtime details Axelix collected when the instance registered:

- **Version**: the service's own version (the Maven/Gradle artifact version).
- **Spring Boot**: the Spring Boot version the instance was built against.
- **Java**: the major Java version of the running JVM.
- **Commit**: short Git SHA of the build (from `git-commit-id`-style metadata, when the application exposes it).
- **Deployed for**: how long the instance has been registered with Master.

Clicking anywhere on the card navigates to the **Details** screen of that instance's [Service Profile](./service-profile.md).

## Search and filters

The search field above the grid filters cards by service name (case-insensitive substring match). The counter on the right shows `<matches> / <total>`.

The **+ Add filter** button next to the search opens a popover with three fields:

- **Field** — one of **Spring Boot**, **Java**, **Spring Framework**, **Kotlin**.
- **Comparison** — **Equal**, **Less than or equal**, or **Greater than or equal**. The comparison is semver-aware, so `<= 3.2` matches `3.1.x` and `3.2.x` but not `3.3.x`.
- **Value** — a version pulled from the instances currently in the fleet. Only versions that exist on at least one card are offered, so you cannot build a filter that matches nothing.

Saving the filter adds a removable tag between the search and the grid, narrows the displayed cards, and writes the filter into the URL as a `?f=Field:Operator:Value` query parameter. Filtered Wallboard URLs can be bookmarked and shared, and the [Dashboard](./dashboard.md) generates them automatically when you click a slice of a System's Map chart. Add more filters by repeating the action — every active filter has to match for a card to be shown.

## MCP access

The Wallboard data is exposed as an MCP tool, so an AI agent connected to the bundled Axelix Master MCP server can list every managed instance with the same fields shown on the cards. [See the MCP catalog.](../setting-up-master-ui/mcp/mcp-tools.mdx#instances)

## Related

- [Service Profile](./service-profile.md)
- [Dashboard](./dashboard.md)
- [MCP](./mcp.md)
