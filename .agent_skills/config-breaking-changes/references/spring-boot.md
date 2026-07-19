# Spring Boot properties — where to look and what changes mean

Use this when the diff touches the `axelix` Spring Boot application. It tells you where
properties are defined, how to read their type/default, and what "deprecate", "fallback",
and "type change" concretely look like in Spring Boot. The four rules live in `SKILL.md`;
this file only maps them onto Spring Boot mechanics.

## Where properties are defined

- **`@ConfigurationProperties` classes** — the primary source of truth. Each field (and its
  getter/setter, or the constructor parameter for records / `@ConstructorBinding`) is a
  property. The field **type** is the property's accepted type; the field initializer or
  `@DefaultValue` is the **default**.
- **`@Value("${some.property:default}")`** injections — the part after `:` is the default;
  the target field type is the accepted type.
- **`application.yml` / `application.properties`** (and profile variants like
  `application-prod.yml`) — declared keys and shipped default values.
- **`META-INF/additional-spring-configuration-metadata.json`** — hand-written metadata for
  defaults, descriptions, and **deprecations**. Spring also generates
  `spring-configuration-metadata.json` at build time.

When inventorying changes, map a renamed/removed Java field back to its **property name**
(relaxed binding: `instanceNamePrefix` ↔ `instance-name-prefix` ↔ `INSTANCE_NAME_PREFIX`).
Renaming the *Java field* but keeping the prefix/`@ConfigurationProperties name` so the
external key is unchanged is **not** a property rename — it's safe. The contract is the
external key, not the field identifier.

## Rule 1 (rename) in Spring Boot

The introducing PR adds the new property and keeps the old one with precedence + fallback.
Typical shapes:

```java
// Both fields exist; the getter encodes "new wins, fall back to old".
private String instanceName;        // deprecated
private String instanceNamePrefix;  // replacement

@DeprecatedConfigurationProperty(replacement = "axelix.instance-name-prefix",
                                 reason = "Renamed for clarity")
@Deprecated
public String getInstanceName() { return instanceName; }

public String getInstanceNamePrefix() {
    return instanceNamePrefix != null ? instanceNamePrefix : instanceName; // new wins, falls back
}
```

Check that the precedence is **new-wins-with-fallback**, not the reverse, and that an operator
who only set the old key still gets the value.

## Deprecation markers (Rules 1 & 2)

A property counts as properly deprecated when **both** are true where applicable:

- **`@DeprecatedConfigurationProperty(replacement = "...", reason = "...")`** on the getter —
  this is what surfaces the replacement to operators and tooling. The `replacement` (or a clear
  `reason` when there is no replacement) is how Rule 2's "communicate the alternative" is met.
- The deprecation is also reflected in metadata when present:
  ```json
  {"name": "axelix.instance-name", "deprecation": {
      "replacement": "axelix.instance-name-prefix",
      "reason": "Renamed for clarity", "level": "warning"}}
  ```
  `level: "error"` means the property is no longer bound — that is effectively a removal, so
  judge it under Rule 2 (only valid at a major bump, after a prior-release deprecation).

For **Rule 2 removal**, confirm via `git log`/`git blame` on the getter/field and metadata that
the deprecation landed in an **earlier released version**, and that this PR targets a **major**
bump (check the project version in `pom.xml` / `build.gradle`).

## Rule 3 (type/format) in Spring Boot

A type change is a violation when an existing property's binding target changes incompatibly:

- A field type changes (`Duration`/`int` → `String`, `int` → `long` semantics, etc.).
- An `enum` value is renamed, removed, or repurposed (operators may have written the old value).
- A custom `Converter`/`@ConfigurationPropertiesBinding` or parsing format is swapped for one
  that rejects or reinterprets previously-valid values.
- A collection/map shape changes (scalar → list, list element type changes).

The compliant path is a **new property** carrying the new type (e.g. `heartbeatIntervalText:
String` alongside `heartbeatInterval: Duration`), then the Rule 1 migration.

Note: *widening that is fully backward compatible and changes nothing for existing values* (e.g.
adding a new optional field) is not a Rule 3 violation. Focus on whether a previously-valid value
could now fail or behave differently.

## Rule 4 (default) in Spring Boot

The default is the field initializer, the `@DefaultValue(...)`, the `${prop:default}` fallback,
or the value shipped in `application.yml`. A change to any of these is a default change — allowed
only at a major bump, always a breaking change to be labelled.
