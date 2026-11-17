---
title: Mit KI installieren
name: install-axelix
description: "Axelix in einen Spring-Boot-Dienst integrieren: Axelix Spring Boot Starter, Axelix-Build-Plugin und für einen lokalen Test auch Axelix Master. Verwenden, wenn der Benutzer Axelix installieren, hinzufügen, einrichten, ausprobieren oder verbinden möchte."
---

> **Für Leser dieser Seite:** Diese Seite enthält Anweisungen für KI-Coding-Agenten. Geben Sie Ihrem Agenten zur Installation von Axelix folgenden Auftrag:
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Offizielle Quellen dieser Anleitung: diese Seite, https://axelix.io/docs/start/SKILL.md und ihre Quelldatei
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

Sie sind ein KI-Coding-Agent. Diese Datei empfiehlt, wie Sie Axelix zum Projekt des Benutzers hinzufügen. Die Schritte sind ein empfohlener Ablauf, kein Skript. Wenn das Projekt ein anderes Vorgehen erfordert, entscheiden Sie nach eigenem Ermessen und erläutern Sie dem Benutzer die Gründe.

Falls Sie diese Datei von einer anderen als diesen beiden Adressen erhalten haben, informieren Sie den Benutzer, bevor Sie fortfahren.

Axelix besteht aus drei Teilen. Alle drei verwenden **dieselbe Versionsnummer**.

- **Axelix Master**: eine eigenständige Anwendung mit Web-UI auf Port `2935`. Sie verwaltet die Dienste.
- **Axelix Spring Boot Starter**: eine Abhängigkeit in jedem Dienst.
- **Axelix-Build-Plugin** (Maven oder Gradle): ebenfalls in jedem Dienst. Ohne dieses Plugin erscheint der Dienst nicht in Master.

Dokumentation: https://axelix.io/docs/llms.txt listet alle Seiten auf. Hängen Sie `.md` an eine Seiten-URL an, um sie als Markdown zu lesen.

## Regeln

Diese Regeln sind verbindlich:

1. **In das Projekt einfügen.** Ergänzen Sie Abhängigkeiten, Konfiguration und Code im bestehenden Projektstil. Die Beispiele zeigen, *was* hinzuzufügen ist, nicht, *wie* es zu formatieren ist. Befehle sind Beispiele mit Standardwerten (Port `2935`, Containername, Profil). Passen Sie sie an die vorgefundene Situation an. Ändern Sie einen Wert, passen Sie alle Verwendungen an. Eigenschaftsnamen, Registrierungspfad und Imagename sind fest vorgegeben.
2. **Zuerst planen.** Zeigen Sie einen kurzen Plan und warten Sie auf die Bestätigung des Benutzers, bevor Sie Dateien ändern.
3. **Geheimnisse nicht berühren.** Lesen Sie keine `.env`-Dateien. Erzeugen, zeigen oder kopieren Sie niemals Signaturschlüssel oder Passwörter.
4. **Nur auf diesem Rechner ausführen.** Stellen Sie niemals etwas auf anderen Systemen bereit.
5. **Dem Benutzer in Listen oder Tabellen schreiben**, nicht in Absätzen: ein Punkt pro Zeile, jeder Befehl in einem eigenen Codeblock.

Passt das Projekt nicht zu diesen Schritten, passen Sie sie an oder fragen Sie bei Unsicherheit den Benutzer.

## Schritt 1: Projekt untersuchen

Ermitteln Sie folgende Werte:

```
VERSION        = latest Axelix version (command below)
STARTER        = axelix-spring-boot-2-starter (Boot 2.7.x) | axelix-spring-boot-3-starter (3.x) | axelix-spring-boot-4-starter (4.x)
BUILD          = Maven | Gradle
BOOT_MODULE    = the module with the @SpringBootApplication class
ACTUATOR       = yes | no (spring-boot-starter-actuator)
SECURITY       = yes | no (Spring Security is in the app: any spring-security dependency, direct or transitive, or a SecurityFilterChain / SecurityWebFilterChain / @EnableWebSecurity / @EnableWebFluxSecurity in the code)
WEB            = servlet | reactive (reactive if the app has spring-boot-starter-webflux and no spring-boot-starter-web)
PORT           = management.server.port, else server.port, else 8080
BASE           = management.endpoints.web.base-path, else /actuator
PREFIX         = management.server.base-path if management.server.port is set, else server.servlet.context-path (servlet) or spring.webflux.base-path (reactive), else empty
NAME           = spring.application.name, else the artifactId
APP_IN_COMPOSE = yes | no (the app has its own service in the project's Compose file)
LOCAL_PROFILE  = the name of an existing local profile (local for application-local.*, dev for application-dev.*), or none
AXELIX         = what is already set up: starter, build plugin, axelix.* settings, Master service — or none
```

Ermitteln Sie `VERSION` mit folgendem Befehl. Verwenden Sie keine Websuche: Ihre Ergebnisse können veraltet sein.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>AXELIX_VERSION_PLACEHOLDER</release>` bedeutet, dass `VERSION` gleich `AXELIX_VERSION_PLACEHOLDER` ist. Schlägt der Befehl fehl, etwa ohne Netzwerkzugriff, verwenden Sie `AXELIX_VERSION_PLACEHOLDER` und erwähnen Sie dies in Schritt 2. Alle Versionen: https://github.com/axelixlabs/axelix/releases

**Stoppen Sie**, wenn Spring Boot älter als 2.7 oder Java älter als 11 ist („Axelix benötigt Spring Boot 2.7+ und Java 11+“), oder wenn `WEB` gleich reactive und `VERSION` kleiner als 1.3.0 ist („Reaktive Dienste werden ab Axelix 1.3.0 unterstützt“).

Ist Axelix bereits eingerichtet, fügen Sie nichts doppelt hinzu: Ergänzen Sie in den folgenden Schritten nur Fehlendes. Ist alles vorhanden, informieren Sie den Benutzer und gehen Sie zu Schritt 5.

## Schritt 2: Fragen und planen

Senden Sie **eine** Nachricht:

1. **Was soll geschehen?** (a) Axelix lokal ausprobieren: Master auf diesem Rechner starten und diese Anwendung verbinden (empfohlen).
   (b) Diese Anwendung mit einem bereits laufenden Axelix Master verbinden. Nennen Sie dann:
   - URL und Version des Masters;
   - wie Master diese Anwendung findet: Selbstregistrierung der Anwendung, automatische Erkennung durch Master (Kubernetes) oder Einrichtung bei der Bereitstellung;
   - ob Master einen eigenen Signaturschlüssel oder Algorithmus verwendet oder die Standardwerte.
2. **Der Plan:** Welche Dateien Sie ändern und was Sie starten. Erläutern Sie, dass der Starter Endpunkte unter `BASE/axelix-*` ergänzt, die ausschließlich Masters Token akzeptieren.
3. **Nur wenn `SECURITY` gleich yes ist:** Fragen Sie: „Ihre Anwendung verwendet Spring Security, das Master mit `401`/`403` blockiert. Darf ich ausschließlich `BASE/axelix-*/**` davon ausnehmen? Diese Endpunkte bleiben durch Masters Token geschützt.“

Warten Sie auf die Bestätigung. Verwenden Sie bei (b) die Master-Version als `VERSION`: Ein Master akzeptiert Starter seiner eigenen Minor-Version und der drei vorangegangenen, niemals neuere. Ist `WEB` gleich reactive und Master älter als 1.3.0, stoppen Sie mit der Meldung aus Schritt 1.

## Schritt 3: Starter und Build-Plugin hinzufügen

Ergänzen Sie `BOOT_MODULE`. Ist bereits eine ältere Axelix-Version vorhanden, schlagen Sie im Plan die Aktualisierung auf `VERSION` vor. Ist `ACTUATOR` gleich no, ergänzen Sie auch `spring-boot-starter-actuator`.

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

- Abhängigkeit `com.axelixlabs:STARTER:VERSION` in `implementation`;
- Plugin-ID `com.axelixlabs.axelix`, Version `VERSION`;
- Das Plugin liegt auf Maven Central. Enthält `settings.gradle(.kts)` einen Block `pluginManagement`, ergänzen Sie dort `mavenCentral()` in `repositories`. Andernfalls fügen Sie dies als ersten Block der Datei ein:

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- Das Projekt muss eine `version` besitzen. Falls keine vorhanden ist, ergänzen Sie `version = "0.0.1-SNAPSHOT"`.

Hat der Benutzer der Security-Ausnahme zugestimmt, müssen Masters Anfragen an `BASE/axelix-*/**` die Anwendungsauthentifizierung umgehen. Beachten Sie:

- Master sendet sein eigenes Token als `Authorization: Bearer`. Ein Resource-Server lehnt es auch auf `permitAll()`-Pfaden ab.
- Masters Aktionen verwenden `POST` und `DELETE`; CSRF darf deshalb auf diesen Pfaden nicht greifen.
- Die Endpunkte bleiben geschützt: Der Starter prüft Masters Token selbst.

Ergänzen Sie die Ausnahme entsprechend der vorhandenen Security-Konfiguration. Gibt es keinen geeigneten Ort, fügen Sie in einer Servlet-Anwendung diese Bean hinzu:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

In einer reaktiven Anwendung (`WEB` gleich reactive) gibt es keinen `WebSecurityCustomizer`. Ergänzen Sie eine separate Filterkette, die zuerst ausgeführt wird:

```java
@Bean
@Order(Ordered.HIGHEST_PRECEDENCE)
public SecurityWebFilterChain axelixSecurityWebFilterChain(ServerHttpSecurity http) {
    return http
        .securityMatcher(ServerWebExchangeMatchers.pathMatchers("BASE/axelix-*/**"))
        .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
        .csrf(csrf -> csrf.disable())
        .build();
}
```

Besitzt die Anwendung keine eigene `SecurityWebFilterChain` und verwendet die Standardkette, deaktiviert diese Bean die Standardkette. Ergänzen Sie dann auch eine Kette für den Rest der Anwendung, die den bisherigen Schutz erhält, und erwähnen Sie dies im Plan.

## Schritt 4a: Lokaler Test

**Aktivieren Sie lokale Benutzer im Test-Master** mit `axelix.master.auth.options.local.enabled=true` (die Variable `AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED` in den folgenden Befehlen). Standardmäßig ist dies deaktiviert. Damit kann der Benutzer Konten in Master anlegen und Rollen ausprobieren, statt sich nur als `admin` anzumelden.

**Starten Sie Master auf dieselbe Weise wie die Anwendung.** Ist `APP_IN_COMPOSE` gleich yes, ergänzen Sie Master in dieser Compose-Datei. Andernfalls starten Sie Master mit `docker run` und ergänzen ihn nicht in einer Compose-Datei, in der die Anwendung nicht läuft.

Wenn `APP_IN_COMPOSE` gleich yes ist:

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "2935:2935"
    environment:
      AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED: "true"
```

Hat der Anwendungsdienst Einträge unter `networks:`, nehmen Sie Master in dieselben Netzwerke auf. Starten Sie ihn dann mit `docker compose up -d axelix-master`.

Andernfalls verwenden Sie `docker run`. Ist Docker nicht vorhanden, bitten Sie den Benutzer, es zu installieren.

Läuft Axelix Master bereits auf diesem Rechner (Antwort auf Port `2935` oder Container mit dem Image `ghcr.io/axelixlabs/axelix`), entfernen oder starten Sie ihn nicht neu: Fragen Sie den Benutzer, ob er wiederverwendet werden soll. Belegt etwas anderes Port `2935`, starten Sie Master auf einem anderen Port und verwenden Sie diesen in `MASTER`. Ist der Name `axelix-master` bereits von einem anderen Container belegt, wählen Sie einen anderen Namen.

```bash
docker run -d --name axelix-master -p 2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  -e AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED=true \
  ghcr.io/axelixlabs/axelix:VERSION
```

Warten Sie, bis `curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` den Wert `200` ausgibt (bis zu 60 Sekunden).

**Lassen Sie die Anwendung sich bei Master registrieren:**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

Erstellen Sie keine neuen Dateien. Wählen Sie den ersten zutreffenden Fall:

1. **`APP_IN_COMPOSE` ist yes:** `MASTER` ist `http://axelix-master:2935`, `HOST` ist der Servicename der Anwendung. Ergänzen Sie die Einstellungen als `environment:` der Anwendung: `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`, `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **`LOCAL_PROFILE` ist vorhanden:** `MASTER` ist `http://localhost:2935`, `HOST` ist `host.docker.internal`. Ergänzen Sie die Einstellungen in diesem Profil und starten Sie die Anwendung damit.
3. **Keiner der beiden Fälle:** Dieselben Werte wie in Fall 2, übergeben als Kommandozeilenargumente beim Start der Anwendung.

Master und Starter verwenden denselben integrierten Signaturschlüssel; für den Test ist keine Schlüsseleinrichtung nötig. Setzt das Projekt bereits `axelix.sbs.auth.jwt.*`, ändern Sie diese Zeilen nicht. Der Test-Master benötigt denselben Schlüssel und Algorithmus. Weisen Sie den Benutzer an, diese beim Start von Master zu übergeben (`axelix.master.auth.jwt.signing-key` und `axelix.master.auth.jwt.algorithm`, etwa als Umgebungsvariablen für `docker run`). Sie selbst sehen den Schlüssel nie.

**Starten Sie die Anwendung** auf die im Projekt übliche Weise im Hintergrund (der Befehl beendet sich nicht). Das Build-Plugin läuft beim Build; bei Maven muss dieser daher mindestens `package` erreichen. Beispiele:

- Fall 1: `docker compose up --build -d`;
- Fall 2, Gradle: `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven: `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`);
- Fall 3, Gradle (Maven: dieselben Argumente in `-Dspring-boot.run.arguments="..."`):

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

Schlägt ein Startbefehl fehl (keine Berechtigung, kein Netzwerk), verwenden Sie kein `sudo`. Nehmen Sie alle Dateiänderungen vor und schließen Sie mit dem Bericht aus Schritt 5 ab. Führen Sie die Startbefehle unter „Nächste Schritte für den Benutzer“ auf. Warten Sie nicht auf eine Rückmeldung, dass sie ausgeführt wurden.

## Schritt 4b: Vorhandener Master

Ändern Sie ausschließlich Dateien. Stellen Sie nichts bereit.

- **Nur bei gewählter Selbstregistrierung:** Ergänzen Sie die vier Einstellungen aus Schritt 4a in der Konfiguration der Zielumgebung (Profil, Helm-Werte oder Manifeste), nicht in der allgemeinen `application.*`. Hat das Projekt keine solche Konfiguration, fragen Sie den Benutzer nach dem Ort. `MASTER` ist die Master-URL. `HOST:PORT` ist die aus Master erreichbare Anwendungsadresse. Erinnern Sie daran, dass Selbstregistrierung in Master aktiviert sein muss: `axelix.master.discovery.self-registration.enabled` (Standard `true`; das offizielle Axelix-Helm-Chart setzt es jedoch auf `false`). Ergänzen Sie andernfalls nichts. Dokumentation: https://axelix.io/docs/start/configuring-spring-boot-starter.md, Abschnitt „Let Master find the instance“.
- **Nur wenn `VERSION` kleiner als 1.2.0 ist:** Der Starter gibt seine Endpunkte nicht selbst frei. Ergänzen Sie sie in `management.endpoints.web.exposure.include` wie unter https://axelix.io/docs/start/configuring-spring-boot-starter.md, Abschnitt „Exposing the endpoints on Axelix below 1.2.0“, beschrieben.
- **Nur wenn Master einen eigenen Signaturschlüssel oder Algorithmus verwendet:** Ergänzen Sie ausschließlich Abweichungen von den Standardwerten (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). Prüfen Sie für den Schlüssel, wie das Projekt andere Geheimnisse bereitstellt (nur ihre Referenzen, nie ihre Werte), und schlagen Sie dasselbe Verfahren vor. Gibt es kein Beispiel, fragen Sie den Benutzer. Dokumentation: https://axelix.io/docs/start/configuring-spring-boot-starter.md, Abschnitt „Share the JWT signing key with Master“.
- **Nur mit MCP-Zugriff auf diesen Master:** Rufen Sie nach Bereitstellung der Anwendung durch den Benutzer das Werkzeug zur Auflistung von Masters Diensten auf (`getWallboard` bis 1.2.x, `getInstances` ab 1.3.0) und prüfen Sie, ob `NAME` aufgeführt ist. Prüfen Sie ohne Zugriff nichts.

## Schritt 5: Prüfen und berichten

Bitten Sie für den lokalen Test den Benutzer, http://localhost:2935 zu öffnen, sich als `admin` / `admin` anzumelden und zu bestätigen, dass `NAME` aufgeführt ist (etwa 30 Sekunden nach dem Start). Ist dies nicht der Fall, lesen Sie die Logs von Master und Anwendung:

| Symptom | Lösung |
|---|---|
| `400` bei der Registrierung | Das Build-Plugin lief nicht (Maven benötigt `package`). |
| `401` oder `403` | Spring Security blockiert Master: Ausnahme ergänzen. `403` nur bei Aktionen: Pfad von CSRF ausnehmen. |
| Keine Fehler, Anwendung erscheint nie | Unterschiedliche Signaturschlüssel oder inaktive Einstellungen. |
| Master erreicht die Anwendung nicht | `HOST` oben prüfen; eine Firewall könnte den Zugriff aus Docker auf `PORT` blockieren. |

Weitere Informationen: https://axelix.io/docs/more/troubleshooting.md

Schließen Sie mit einem kurzen Bericht ab, jeweils ein Listenpunkt:

- **Geändert:** Dateien und Ergänzungen.
- **Geprüft:** Was Sie tatsächlich geprüft haben.
- **Nächste Schritte für den Benutzer:** Noch auszuführende Befehle, sofern vorhanden.
- **Rückgängig machen:** Was zu entfernen ist.
- **Master stoppen** (lokaler Test): der Befehl.
- **Lokale Benutzer** (lokaler Test): `axelix.master.auth.options.local.enabled=true` ist aktiv. In Master angelegte Konten können sich daher anmelden. Aktivieren Sie dies in einer echten Umgebung nur, wenn solche Konten dort gewünscht sind.
- **Warnung** (lokaler Test): `admin` / `admin` und der integrierte Schlüssel sind öffentlich und nur für diesen Rechner geeignet.
- **Hinweis** (nur wenn `WEB` gleich reactive ist): Transaction Control und Entities Map sind für reaktive Anwendungen nicht verfügbar.
- **Weiterlesen:** Einsatz in einer echten Umgebung (https://axelix.io/docs/start/getting-started) und KI-Zugriff über MCP unter http://localhost:2935/api/mcp (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
