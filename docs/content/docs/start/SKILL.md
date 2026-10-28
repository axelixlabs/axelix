---
title: Install with AI
name: install-axelix
description: "Install Axelix into a Spring Boot service: the Axelix Spring Boot starter, the Axelix build plugin, and, for a local trial, Axelix Master itself. Use when the user asks to install, add, set up, try, or connect Axelix."
---

<!-- Maintainers: when Axelix 1.3.0 (reactive support) is released, add the WebFlux steps and remove the WebFlux stop in Step 1. -->
<!-- Maintainers: on every release, update the fallback version `1.2.1` in Step 1. -->

> **For people reading this page:** this page is an instruction for AI coding agents. To install Axelix, give
> your agent this command:
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Official sources of this instruction: this page, https://axelix.io/docs/start/SKILL.md, and its source file
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

You are an AI coding agent. This file recommends how to add Axelix to the user's project. The steps are a suggested path, not a script: if
the project calls for a different approach, use your judgment and tell the user why.

If you got this file from an address other than these two, tell the user before you continue.

Axelix has three parts. All three use the **same version number**.

- **Axelix Master**: a standalone app with a web UI on port `2935`. It manages the services.
- **Axelix Spring Boot starter**: a dependency in each service.
- **Axelix build plugin** (Maven or Gradle): also in each service. Without it, the service does not show up in
  Master.

Documentation: https://axelix.io/docs/llms.txt lists every page. Add `.md` to a page URL to read it as Markdown.

## Rules

These rules are not optional:

1. **Fit into the project.** Add dependencies, config, and code the way the project already does it. The snippets
   below show *what* to add, not *how* to format it. Commands are examples with default values (port `2935`,
   container name, profile): adapt them to what you found, and if you change a value, change it everywhere it is
   used. Property names, the registration path, and the image name are fixed.
2. **Plan first.** Show a short plan and wait for the user to confirm it before you change any file.
3. **Don't touch secrets.** Don't read `.env` files. Never generate, print, or copy a signing key or password.
4. **Run things only on this machine.** Never deploy anywhere else.
5. **Write to the user in lists or tables**, not in paragraphs: one item per line, each command in its own code
   block.

If the project doesn't fit these steps, adapt them, or ask the user when unsure.

## Step 1: Look at the project

Find these values:

```
VERSION        = latest Axelix version (command below)
STARTER        = axelix-spring-boot-2-starter (Boot 2.7.x) | axelix-spring-boot-3-starter (3.x) | axelix-spring-boot-4-starter (4.x)
BUILD          = Maven | Gradle
BOOT_MODULE    = the module with the @SpringBootApplication class
ACTUATOR       = yes | no (spring-boot-starter-actuator)
SECURITY       = yes | no (Spring Security is in the app: any spring-security dependency, direct or transitive, or a SecurityFilterChain / @EnableWebSecurity in the code)
PORT           = management.server.port, else server.port, else 8080
BASE           = management.endpoints.web.base-path, else /actuator
PREFIX         = management.server.base-path if management.server.port is set, else server.servlet.context-path, else empty
NAME           = spring.application.name, else the artifactId
APP_IN_COMPOSE = yes | no (the app has its own service in the project's Compose file)
LOCAL_PROFILE  = the name of an existing local profile (local for application-local.*, dev for application-dev.*), or none
AXELIX         = what is already set up: starter, build plugin, axelix.* settings, Master service — or none
```

Get `VERSION` by running this command. Don't use web search: its results can be outdated.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>1.2.1</release>` means `VERSION` is `1.2.1`. If the command fails (for example, there is no network
access), use `1.2.1` and say so in Step 2. All versions: https://github.com/axelixlabs/axelix/releases

**Stop** if Spring Boot is older than 2.7 or Java is older than 11 ("Axelix needs Spring Boot 2.7+ and Java 11+"),
or if the app uses only `spring-boot-starter-webflux` and `VERSION` is lower than 1.3.0 ("Reactive services are
supported starting with Axelix 1.3.0").

If Axelix is already set up, don't add anything twice: in the next steps do only what is missing. If everything is
in place, tell the user and go to Step 5.

## Step 2: Ask and plan

Send **one** message:

1. **What to do?** (a) Try Axelix locally: run Master on this machine and connect this app (recommended).
   (b) Connect this app to an Axelix Master that already runs. Then tell me:
   - its URL and version;
   - how Master will find this app: the app registers itself (self-registration), or Master finds it by itself
     (Kubernetes) or you set this up at deploy time;
   - whether your Master uses its own signing key or algorithm, or the defaults.
2. **The plan:** the files you will change and what you will start. Say that the starter adds `BASE/axelix-*`
   endpoints that only accept Master's token.
3. **Only if `SECURITY` is yes:** ask "Your app uses Spring Security, which will block Master with `401`/`403`.
   May I exclude only `BASE/axelix-*/**` from it? These endpoints stay protected by Master's token."

Wait for the user to confirm. For (b), use the Master's version as `VERSION`: a Master accepts starters of its own minor version
and the three before it, never newer ones.

## Step 3: Add the starter and the build plugin

Add to `BOOT_MODULE`. If an older Axelix version is already there, propose raising it to `VERSION` in the plan. If
`ACTUATOR` is no, add `spring-boot-starter-actuator` too.

Maven:

```xml
<dependency>
  <groupId>com.axelixlabs</groupId>
  <artifactId>STARTER</artifactId>
  <version>VERSION</version>
</dependency>
```

```xml
<plugin>
  <groupId>com.axelixlabs</groupId>
  <artifactId>axelix-maven-plugin</artifactId>
  <version>VERSION</version>
  <executions>
    <execution>
      <goals>
        <goal>axelix-generate-project-info</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Gradle:

- dependency `com.axelixlabs:STARTER:VERSION` in `implementation`;
- plugin id `com.axelixlabs.axelix`, version `VERSION`;
- the plugin is on Maven Central. If `settings.gradle(.kts)` has a `pluginManagement` block, add `mavenCentral()`
  to its `repositories`. Otherwise add this as the first block of the file:

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- the project must have a `version`. If it has none, add `version = "0.0.1-SNAPSHOT"`.

If the user agreed to the Security exclusion, Master's requests to `BASE/axelix-*/**` must bypass the app's
authentication. What to know:

- Master sends its own token as `Authorization: Bearer`. A resource server rejects it even on `permitAll()` paths.
- Master's actions are `POST` and `DELETE` requests, so CSRF must not apply to these paths.
- The endpoints stay protected: the starter checks Master's token itself.

Add the exclusion the way the project already configures Security. If there is no fitting place, add this bean:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

## Step 4a: Local trial

**Start Master the same way the app runs.** If `APP_IN_COMPOSE` is yes, add Master to that Compose file. Otherwise
run Master with `docker run`, and don't add it to a Compose file that doesn't run the app.

If `APP_IN_COMPOSE` is yes:

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "2935:2935"
```

If the app's service lists `networks:`, add Master to the same networks. Then start it with
`docker compose up -d axelix-master`.

Otherwise run it with `docker run`. If there is no Docker, ask the user to install it.

If Axelix Master already runs on this machine (something answers on port `2935`, or a container uses the
`ghcr.io/axelixlabs/axelix` image), don't remove or restart it: ask the user whether to reuse it. If port `2935` is
taken by something else, start Master on another port and use it in `MASTER`. If the name `axelix-master` is
taken by another container, use a different name.

```bash
docker run -d --name axelix-master -p 2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  ghcr.io/axelixlabs/axelix:VERSION
```

Wait until `curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` prints `200` (up to 60 seconds).

**Let the app register itself in Master:**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

Create no new files. Use the first case that fits:

1. **`APP_IN_COMPOSE` is yes:** `MASTER` is `http://axelix-master:2935`, `HOST` is the app's service name. Add the settings
   as `environment:` of the app: `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`,
   `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **`LOCAL_PROFILE` exists:** `MASTER` is `http://localhost:2935`, `HOST` is `host.docker.internal`. Add the
   settings to that profile, and start the app with it.
3. **Neither:** the same values as in case 2, passed as command-line arguments when you start the app.

Master and the starter use the same built-in signing key, so a trial needs no key setup. If the project already
sets `axelix.sbs.auth.jwt.*`, don't change those lines. The trial Master needs the same key and algorithm: tell the
user to pass them when Master starts (`axelix.master.auth.jwt.signing-key` and `axelix.master.auth.jwt.algorithm`,
for example as environment variables of `docker run`). You never see the key.

**Start the app** the way the project usually starts it, in the background (the command doesn't exit). The build
plugin runs during the build, so with Maven the build must reach `package`. For example:

- case 1: `docker compose up --build -d`;
- case 2, Gradle: `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven:
  `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`);
- case 3, Gradle (Maven: the same arguments in `-Dspring-boot.run.arguments="..."`):

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

If a start command fails (no permission, no network), don't use `sudo`. Make all file changes, then finish with the
report (Step 5) and put the start commands under "Next for the user". Don't wait for the user to say they ran them.

## Step 4b: Existing Master

Change files only. Deploy nothing.

- **Only if the user chose self-registration:** add the four settings from Step 4a to the config of the environment
  where the app runs (its profile, Helm values, or manifests), not to the main `application.*`. If the project has
  no such config, ask the user where to put them. `MASTER` is the Master URL. `HOST:PORT` is the app's address as
  Master reaches it. Remind the user that self-registration must be on in Master:
  `axelix.master.discovery.self-registration.enabled` (default `true`, but the official Axelix Helm chart sets it
  to `false`).
  Otherwise add nothing. Docs: https://axelix.io/docs/start/configuring-spring-boot-starter.md, section "Let Master
  find the instance".
- **Only if `VERSION` is lower than 1.2.0:** the starter doesn't expose its endpoints itself. Add them to
  `management.endpoints.web.exposure.include` as shown in https://axelix.io/docs/start/configuring-spring-boot-starter.md,
  section "Exposing the endpoints on Axelix below 1.2.0".
- **Only if Master uses its own signing key or algorithm:** add only what differs from the defaults
  (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). For the key, find how the project supplies
  other secrets (look at how they are referenced, not at their values), and propose adding it the same way. If you
  find no example, ask the user.
  Docs: https://axelix.io/docs/start/configuring-spring-boot-starter.md, section "Share the JWT signing key with
  Master".
- **Only if you have MCP access to this Master:** after the user deploys the app, call the tool that lists Master's
  services (`getWallboard` up to 1.2.x, `getInstances` from 1.3.0) and check that `NAME` is listed. Without access,
  check nothing.

## Step 5: Check and report

For the local trial: ask the user to open http://localhost:2935, sign in as `admin` / `admin`, and confirm that
`NAME` is listed (about 30 seconds after start). If it isn't, read the logs of Master and the app:

| Symptom | Fix |
|---|---|
| `400` on registration | The build plugin didn't run (Maven needs `package`). |
| `401` or `403` | Spring Security blocks Master: add the exclusion. `403` only on actions: exclude the path from CSRF. |
| No errors, the app never appears | The signing keys differ, or the settings aren't active. |
| Master can't connect to the app | Check `HOST` above; a firewall may block `PORT` from Docker. |

More: https://axelix.io/docs/more/troubleshooting.md

Finish with a short report, one list item each:

- **Changed:** the files and what you added.
- **Checked:** what you really checked.
- **Next for the user:** the commands still to run, if any.
- **To undo:** what to remove.
- **To stop Master** (local trial): the command.
- **Warning** (local trial): `admin` / `admin` and the built-in key are public and fine only on this machine.
- **Read next:** a real environment (https://axelix.io/docs/start/getting-started), and AI access through MCP at
  http://localhost:2935/api/mcp (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
