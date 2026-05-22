# Service Profile

The Service Profile is the per-instance area of Axelix. It groups every diagnostic and inspection screen for a single Spring Boot instance behind one sidebar, so you can move between Beans, Loggers, Thread Dumps, and the rest without losing the context of the instance you are looking at.

You enter a Service Profile by clicking a service card on the [Wallboard](./wallboard.md). The first screen you land on is always **Details**.

## Layout

A Service Profile has two regions:

- The **left sidebar** lists every feature page available for the instance, organised into three collapsible groups.
- The **main content area** shows the currently selected feature page.

## Sidebar groups

The sidebar is split into three groups. Each group can be expanded or collapsed independently, and the currently open page is highlighted.

### Insights

General-purpose runtime information about the instance.

- [Details](../features/details.md): the identity card of the instance. Shows build coordinates, Git revision, JDK vendor, and runtime metadata, plus a **Download state** action that exports a ZIP bundle of selected diagnostics (heap dump, thread dump, logs, beans, caches, and more) for offline analysis.
- [Metrics](../features/metrics.md): live Micrometer metrics exposed by the instance, automatically grouped by prefix into logical categories such as JVM, HTTP, and JDBC. Each metric can be drilled into to see its current value and tag dimensions.
- [Loggers](../features/loggers/loggers.mdx): lists every configured logger and logger group in the application with its current level. You can change a level on the fly (for example, push a package to DEBUG while reproducing a bug) and reset it back to the original value when you are done. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#loggers)

### Spring Framework

Spring-specific introspection.

- [Properties](../features/properties.md): the resolved Spring `Environment` of the instance. Every active property is grouped by its source (system properties, `application.yaml`, command-line arguments, and so on), so you can see not just the effective value but also which property source supplied it. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection)
- [Beans](../features/beans.md): every bean in the application context with its scope (Singleton, Prototype, Request, Session, and others), dependencies, aliases, qualifiers, and origin (`@Component`, `@Bean` method, `FactoryBean`, synthetic). Beans backed by `@ConfigurationProperties` link directly to the matching entry on the Configuration Properties page. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection)
- [Configuration Properties](../features/configuration-properties.md): all beans annotated with `@ConfigurationProperties` and the current value of every field they hold. Lets you see what your typed configuration objects actually contain at runtime instead of inferring it from YAML. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection)
- [Scheduled Tasks](../features/scheduled-tasks.md): every `@Scheduled` task in the application, grouped by trigger type (**cron**, **fixed delay**, **fixed rate**). You can pause or resume a task, edit its schedule, and trigger a one-off run by hand. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection)
- [Conditions](../features/conditions.md): the outcome of Spring's `@Conditional` evaluation for every candidate bean, split into **positive** and **negative** matches. The page is the fastest way to find out which condition stopped an auto-configuration from contributing a bean you expected. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#instance-introspection)
- [Caches](../features/caches.md): Spring caches registered in the instance, grouped by their `CacheManager`. Shows hit and miss rates, estimated entry counts, and lets you clear individual caches or every cache at once. [Also available as an MCP tool.](../setting-up-master-ui/mcp/mcp-tools.mdx#caches)
- [Transaction Control](../features/transaction-control.md): live view of every transaction the application is running, with the originating class, method, and duration metrics. Each execution opens a timeline of the SQL queries that ran inside the transaction — you see the exact SQL, its start and end time, and its duration — which is how you spot N+1 access patterns and other query-level inefficiencies.

### JVM

JVM-level diagnostics.

- [Thread Dump](../features/thread-dump.md): snapshot of every live JVM thread with its state, stack trace, blocked/waited counters, and lock owner information. The page also lets you toggle thread contention monitoring on the running JVM.
- [Garbage Collector](../features/garbage-collector.md): garbage collector logs streamed from the instance. GC logging is usually disabled by default in production JVMs, so the page exposes an **Enable GC Logging** action that switches it on without restarting the application.

## MCP access

Several of the features above are also exposed as tools by the MCP server bundled with Axelix Master, so an AI agent connected over MCP can read the same data. The full catalog of registered tools is browsable on the [MCP screen](./mcp.md), and the up-to-date reference list with names, tags, and role gating lives at [MCP Tools](../setting-up-master-ui/mcp/mcp-tools.mdx).

## Related

- [Wallboard](./wallboard.md)
- [Dashboard](./dashboard.md)
