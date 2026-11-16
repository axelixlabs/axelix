---
title: Instalar con IA
name: install-axelix
description: "Instalar Axelix en un servicio Spring Boot: el starter de Spring Boot de Axelix, el plugin de compilación de Axelix y, para una prueba local, el propio Axelix Master. Utilizar cuando el usuario pida instalar, añadir, configurar, probar o conectar Axelix."
---

> **Para quienes leen esta página:** esta página contiene instrucciones para agentes de programación de IA. Para instalar Axelix, dé
> a su agente este comando:
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Fuentes oficiales de estas instrucciones: esta página, https://axelix.io/docs/start/SKILL.md y su archivo fuente
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

Es un agente de programación de IA. Este archivo recomienda cómo añadir Axelix al proyecto del usuario. Los pasos son una ruta sugerida,
no un script: si el proyecto requiere otro enfoque, utilice su criterio y explique al usuario por qué.

Si obtuvo este archivo de una dirección distinta de esas dos, informe al usuario antes de continuar.

Axelix tiene tres partes. Las tres utilizan el **mismo número de versión**.

- **Axelix Master**: aplicación independiente con una interfaz web en el puerto `2935`. Gestiona los servicios.
- **Starter de Spring Boot de Axelix**: dependencia en cada servicio.
- **Plugin de compilación de Axelix** (Maven o Gradle): también en cada servicio. Sin él, el servicio no aparece en
  Master.

Documentación: https://axelix.io/docs/llms.txt enumera todas las páginas. Añada `.md` a la URL de una página para leerla como Markdown.

## Reglas [#rules]

Estas reglas no son opcionales:

1. **Adaptarse al proyecto.** Añada dependencias, configuración y código como ya lo hace el proyecto. Los fragmentos
   siguientes muestran *qué* añadir, no *cómo* formatearlo. Los comandos son ejemplos con valores predeterminados (puerto `2935`,
   nombre de contenedor y perfil): adáptelos a lo encontrado y, si cambia un valor, cámbielo en todos los lugares donde
   se utiliza. Los nombres de propiedades, la ruta de registro y el nombre de la imagen son fijos.
2. **Planificar primero.** Muestre un plan breve y espere a que el usuario lo confirme antes de cambiar archivos.
3. **No tocar secretos.** No lea archivos `.env`. Nunca genere, muestre ni copie una clave de firma o contraseña.
4. **Ejecutar solo en esta máquina.** Nunca despliegue en ningún otro lugar.
5. **Escribir al usuario en listas o tablas**, no en párrafos: un elemento por línea y cada comando en su propio bloque
   de código.

Si el proyecto no encaja con estos pasos, adáptelos o consulte al usuario cuando haya dudas.

## Paso 1: examinar el proyecto [#step-1-look-at-the-project]

Encuentre estos valores:

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

Obtenga `VERSION` ejecutando este comando. No utilice búsquedas web: sus resultados pueden estar desactualizados.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>AXELIX_VERSION_PLACEHOLDER</release>` significa que `VERSION` es `AXELIX_VERSION_PLACEHOLDER`. Si el comando falla (por ejemplo, por falta de
acceso a la red), utilice `AXELIX_VERSION_PLACEHOLDER` e indíquelo en el paso 2. Todas las versiones: https://github.com/axelixlabs/axelix/releases

**Deténgase** si Spring Boot es anterior a 2.7 o Java es anterior a 11 («Axelix necesita Spring Boot 2.7+ y Java 11+»),
o si `WEB` es reactivo y `VERSION` es inferior a 1.3.0 («Los servicios reactivos se admiten a partir de Axelix
1.3.0»).

Si Axelix ya está configurado, no añada nada dos veces: en los pasos siguientes haga solo lo que falte. Si todo está
listo, informe al usuario y vaya al paso 5.

## Paso 2: preguntar y planificar [#step-2-ask-and-plan]

Envíe **un único** mensaje:

1. **¿Qué hacer?** (a) Probar Axelix localmente: ejecutar Master en esta máquina y conectar esta aplicación (recomendado).
   (b) Conectar esta aplicación a un Axelix Master que ya está en ejecución. En ese caso, indique:
   - su URL y versión;
   - cómo encontrará Master esta aplicación: la aplicación se registra (autorregistro), Master la encuentra por sí mismo
     (Kubernetes) o se configura durante el despliegue;
   - si Master utiliza una clave de firma o algoritmo propios o los valores predeterminados.
2. **El plan:** los archivos que cambiará y lo que iniciará. Indique que el starter añade endpoints `BASE/axelix-*`
   que solo aceptan el token de Master.
3. **Solo si `SECURITY` es yes:** pregunte «Su aplicación utiliza Spring Security, que bloqueará Master con `401`/`403`.
   ¿Puedo excluir de su seguridad únicamente `BASE/axelix-*/**`? Estos endpoints seguirán protegidos por el token de Master».

Espere a que el usuario confirme. Para (b), utilice la versión de Master como `VERSION`: Master acepta starters de su propia versión menor
y de las tres anteriores, nunca posteriores. Si `WEB` es reactivo y Master es anterior a 1.3.0, deténgase con el
mensaje del paso 1.

## Paso 3: añadir el starter y el plugin de compilación [#step-3-add-the-starter-and-the-build-plugin]

Añádalos a `BOOT_MODULE`. Si ya hay una versión anterior de Axelix, proponga actualizarla a `VERSION` en el plan. Si
`ACTUATOR` es no, añada también `spring-boot-starter-actuator`.

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

- dependencia `com.axelixlabs:STARTER:VERSION` en `implementation`;
- identificador de plugin `com.axelixlabs.axelix`, versión `VERSION`;
- el plugin está en Maven Central. Si `settings.gradle(.kts)` tiene un bloque `pluginManagement`, añada `mavenCentral()`
  a sus `repositories`. En otro caso, añada lo siguiente como primer bloque del archivo:

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- el proyecto debe tener una `version`. Si no tiene, añada `version = "0.0.1-SNAPSHOT"`.

Si el usuario aceptó la exclusión de seguridad, las solicitudes de Master a `BASE/axelix-*/**` deben omitir la
autenticación de la aplicación. Tenga en cuenta:

- Master envía su propio token como `Authorization: Bearer`. Un servidor de recursos lo rechaza incluso en rutas `permitAll()`.
- Las acciones de Master son solicitudes `POST` y `DELETE`, por lo que CSRF no debe aplicarse a estas rutas.
- Los endpoints siguen protegidos: el starter comprueba por sí mismo el token de Master.

Añada la exclusión de la misma forma en que el proyecto ya configura la seguridad. Si no hay un lugar apropiado, añada este bean
a una aplicación servlet:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

En una aplicación reactiva (`WEB` es reactivo) no existe `WebSecurityCustomizer`. Añada una cadena independiente que se ejecute primero:

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

Si la aplicación no tiene su propia `SecurityWebFilterChain` y depende de la predeterminada, añadir este bean desactiva la predeterminada.
En ese caso, añada también una cadena para el resto de la aplicación que conserve la protección actual e indíquelo en el plan.

## Paso 4a: prueba local [#step-4a-local-trial]

**Activar usuarios locales en el Master de prueba** con `axelix.master.auth.options.local.enabled=true` (la variable
`AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED` de los comandos siguientes). Está desactivada de forma predeterminada. Al activarla, el usuario
puede crear cuentas en Master y probar los roles, no solo iniciar sesión como `admin`.

**Iniciar Master de la misma forma que la aplicación.** Si `APP_IN_COMPOSE` es yes, añada Master a ese archivo Compose. En otro caso,
ejecute Master con `docker run` y no lo añada a un archivo Compose que no ejecute la aplicación.

Si `APP_IN_COMPOSE` es yes:

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "2935:2935"
    environment:
      AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED: "true"
```

Si el servicio de la aplicación enumera `networks:`, añada Master a las mismas redes. Después, inícielo con
`docker compose up -d axelix-master`.

En otro caso, ejecútelo con `docker run`. Si no hay Docker, pida al usuario que lo instale.

Si Axelix Master ya se ejecuta en esta máquina (algo responde en el puerto `2935` o un contenedor utiliza la
imagen `ghcr.io/axelixlabs/axelix`), no lo elimine ni reinicie: pregunte al usuario si quiere reutilizarlo. Si el puerto `2935` está
ocupado por otra cosa, inicie Master en otro puerto y utilícelo en `MASTER`. Si otro contenedor utiliza el nombre `axelix-master`,
elija un nombre distinto.

```bash
docker run -d --name axelix-master -p 2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  -e AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED=true \
  ghcr.io/axelixlabs/axelix:VERSION
```

Espere hasta que `curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` muestre `200` (hasta 60 segundos).

**Permitir que la aplicación se registre en Master:**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

No cree archivos nuevos. Utilice el primer caso que corresponda:

1. **`APP_IN_COMPOSE` es yes:** `MASTER` es `http://axelix-master:2935`, `HOST` es el nombre del servicio de la aplicación. Añada los ajustes
   a `environment:` de la aplicación: `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`,
   `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **Existe `LOCAL_PROFILE`:** `MASTER` es `http://localhost:2935`, `HOST` es `host.docker.internal`. Añada los
   ajustes a ese perfil e inicie la aplicación con él.
3. **Ninguno:** utilice los mismos valores del caso 2, pasados como argumentos de línea de comandos al iniciar la aplicación.

Master y el starter utilizan la misma clave integrada, por lo que la prueba no requiere configurar una clave. Si el proyecto ya
establece `axelix.sbs.auth.jwt.*`, no cambie esas líneas. El Master de prueba necesita la misma clave y algoritmo: indique al
usuario que los proporcione al iniciar Master (`axelix.master.auth.jwt.signing-key` y `axelix.master.auth.jwt.algorithm`,
por ejemplo como variables de entorno de `docker run`). Usted nunca ve la clave.

**Iniciar la aplicación** como lo hace normalmente el proyecto, en segundo plano (el comando no termina). El plugin de compilación
se ejecuta durante la compilación, por lo que en Maven esta debe llegar a `package`. Por ejemplo:

- caso 1: `docker compose up --build -d`;
- caso 2, Gradle: `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven:
  `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`);
- caso 3, Gradle (Maven: los mismos argumentos en `-Dspring-boot.run.arguments="..."`):

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

Si falla un comando de inicio (por permisos o red), no utilice `sudo`. Complete todos los cambios en archivos y termine con el
informe (paso 5), colocando los comandos de inicio en «Siguientes pasos para el usuario». No espere a que el usuario diga que los ha ejecutado.

## Paso 4b: Master existente [#step-4b-existing-master]

Cambie solo archivos. No despliegue nada.

- **Solo si el usuario eligió autorregistro:** añada los cuatro ajustes del paso 4a a la configuración del entorno
  donde se ejecuta la aplicación (su perfil, valores de Helm o manifiestos), no al `application.*` principal. Si el proyecto no tiene
  esa configuración, pregunte al usuario dónde ponerlos. `MASTER` es la URL de Master. `HOST:PORT` es la dirección de la aplicación
  tal como Master accede a ella. Recuerde al usuario que el autorregistro debe estar activado en Master:
  `axelix.master.discovery.self-registration.enabled` (predeterminado `true`, pero el chart de Helm oficial de Axelix lo establece
  en `false`).
  En otro caso, no añada nada. Documentación: https://axelix.io/docs/start/configuring-spring-boot-starter.md, sección «Let Master
  find the instance».
- **Solo si `VERSION` es inferior a 1.2.0:** el starter no expone sus endpoints por sí mismo. Añádalos a
  `management.endpoints.web.exposure.include` como se muestra en https://axelix.io/docs/start/configuring-spring-boot-starter.md,
  sección «Exposing the endpoints on Axelix below 1.2.0».
- **Solo si Master utiliza una clave de firma o algoritmo propios:** añada únicamente lo que difiera de los valores predeterminados
  (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). Para la clave, averigüe cómo proporciona el proyecto
  otros secretos (observe cómo se referencian, no sus valores) y proponga añadirla del mismo modo. Si no encuentra
  ningún ejemplo, pregunte al usuario.
  Documentación: https://axelix.io/docs/start/configuring-spring-boot-starter.md, sección «Share the JWT signing key with
  Master».
- **Solo si tiene acceso MCP a este Master:** después de que el usuario despliegue la aplicación, invoque la herramienta que enumera sus
  servicios (`getWallboard` hasta 1.2.x, `getInstances` desde 1.3.0) y compruebe que aparece `NAME`. Sin acceso,
  no compruebe nada.

## Paso 5: comprobar e informar [#step-5-check-and-report]

Para la prueba local, pida al usuario que abra http://localhost:2935, inicie sesión como `admin` / `admin` y confirme que
aparece `NAME` (unos 30 segundos después del arranque). Si no aparece, lea los registros de Master y de la aplicación:

| Síntoma | Solución |
|---|---|
| `400` en el registro | El plugin de compilación no se ejecutó (Maven necesita `package`). |
| `401` o `403` | Spring Security bloquea Master: añada la exclusión. `403` solo en acciones: excluya la ruta de CSRF. |
| No hay errores y la aplicación nunca aparece | Las claves de firma difieren o los ajustes no están activos. |
| Master no puede conectarse a la aplicación | Compruebe `HOST` anterior; un cortafuegos puede bloquear `PORT` desde Docker. |

Más información: https://axelix.io/docs/more/troubleshooting.md

Termine con un informe breve, un elemento por punto:

- **Cambios:** archivos y elementos añadidos.
- **Comprobaciones:** lo que realmente se comprobó.
- **Siguientes pasos para el usuario:** comandos pendientes, si los hay.
- **Para deshacer:** qué eliminar.
- **Para detener Master** (prueba local): el comando.
- **Usuarios locales** (prueba local): `axelix.master.auth.options.local.enabled=true` está activado, por lo que las cuentas creadas en
  Master pueden iniciar sesión. Actívelo en un entorno real solo si quiere utilizar esas cuentas allí.
- **Advertencia** (prueba local): `admin` / `admin` y la clave integrada son públicos y solo resultan adecuados en esta máquina.
- **Nota** (solo si `WEB` es reactivo): las páginas Transaction Control y Entities Map no están disponibles para
  aplicaciones reactivas.
- **Lecturas siguientes:** un entorno real (https://axelix.io/docs/start/getting-started) y acceso de IA mediante MCP en
  http://localhost:2935/api/mcp (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
