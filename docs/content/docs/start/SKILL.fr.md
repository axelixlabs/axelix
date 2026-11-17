---
title: Installer avec l’IA
name: install-axelix
description: "Installer Axelix dans un service Spring Boot : le starter Spring Boot Axelix, le plugin de build Axelix et, pour un essai local, Axelix Master lui-même. À utiliser lorsque l’utilisateur demande d’installer, d’ajouter, de configurer, d’essayer ou de connecter Axelix."
---

> **Pour les personnes qui lisent cette page :** cette page contient des instructions destinées aux agents de programmation IA. Pour installer Axelix, donnez cette commande à votre agent :
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Sources officielles de ces instructions : cette page, https://axelix.io/docs/start/SKILL.md et son fichier source
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

Vous êtes un agent de programmation IA. Ce fichier recommande une méthode pour ajouter Axelix au projet de l’utilisateur. Les étapes constituent un parcours suggéré, pas un script : si le projet nécessite une autre approche, faites preuve de discernement et expliquez pourquoi à l’utilisateur.

Si vous avez obtenu ce fichier à une autre adresse que ces deux-là, informez-en l’utilisateur avant de poursuivre.

Axelix comprend trois parties. Elles utilisent toutes le **même numéro de version**.

- **Axelix Master** : une application autonome avec une interface web sur le port `2935`. Elle gère les services.
- **Le starter Spring Boot Axelix** : une dépendance dans chaque service.
- **Le plugin de build Axelix** (Maven ou Gradle) : également présent dans chaque service. Sans lui, le service n’apparaît pas dans Master.

Documentation : https://axelix.io/docs/llms.txt répertorie toutes les pages. Ajoutez `.md` à l’URL d’une page pour la lire au format Markdown.

## Règles

Ces règles sont obligatoires :

1. **Respectez les pratiques du projet.** Ajoutez les dépendances, la configuration et le code comme le projet le fait déjà. Les extraits ci-dessous indiquent *quoi* ajouter, pas *comment* le mettre en forme. Les commandes sont des exemples avec des valeurs par défaut (port `2935`, nom du conteneur, profil) : adaptez-les à ce que vous avez trouvé et, si vous changez une valeur, modifiez-la partout où elle est utilisée. Les noms des propriétés, le chemin d’enregistrement et le nom de l’image sont fixes.
2. **Commencez par un plan.** Présentez un plan court et attendez sa confirmation par l’utilisateur avant de modifier un fichier.
3. **Ne touchez pas aux secrets.** Ne lisez pas les fichiers `.env`. Ne générez, n’affichez et ne copiez jamais de clé de signature ni de mot de passe.
4. **Exécutez uniquement sur cette machine.** Ne déployez jamais ailleurs.
5. **Écrivez à l’utilisateur sous forme de listes ou de tableaux**, pas de paragraphes : un élément par ligne, chaque commande dans son propre bloc de code.

Si le projet ne correspond pas à ces étapes, adaptez-les ou interrogez l’utilisateur en cas de doute.

## Étape 1 : examiner le projet

Recherchez ces valeurs :

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

Obtenez `VERSION` en exécutant cette commande. N’utilisez pas la recherche web : ses résultats peuvent être obsolètes.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>AXELIX_VERSION_PLACEHOLDER</release>` signifie que `VERSION` vaut `AXELIX_VERSION_PLACEHOLDER`. Si la commande échoue (par exemple, en l’absence d’accès réseau), utilisez `AXELIX_VERSION_PLACEHOLDER` et signalez-le à l’étape 2. Toutes les versions : https://github.com/axelixlabs/axelix/releases

**Arrêtez-vous** si la version de Spring Boot est antérieure à 2.7 ou celle de Java à 11 (« Axelix nécessite Spring Boot 2.7+ et Java 11+ »), ou si `WEB` vaut reactive et que `VERSION` est inférieure à 1.3.0 (« Les services réactifs sont pris en charge à partir d’Axelix 1.3.0 »).

Si Axelix est déjà configuré, n’ajoutez rien deux fois : dans les étapes suivantes, effectuez uniquement ce qui manque. Si tout est en place, informez-en l’utilisateur et passez à l’étape 5.

## Étape 2 : poser les questions et présenter le plan

Envoyez **un seul** message :

1. **Que faire ?** (a) Essayer Axelix localement : exécuter Master sur cette machine et connecter cette application (recommandé). (b) Connecter cette application à un Axelix Master déjà en cours d’exécution. Dans ce cas, demandez :
   - son URL et sa version ;
   - comment Master trouvera cette application : elle s’enregistre elle-même (auto-enregistrement), Master la trouve seul (Kubernetes), ou cela sera configuré au déploiement ;
   - si Master utilise une clé de signature ou un algorithme propres, ou les valeurs par défaut.
2. **Le plan :** les fichiers que vous allez modifier et ce que vous allez démarrer. Indiquez que le starter ajoute des endpoints `BASE/axelix-*` qui n’acceptent que le jeton de Master.
3. **Uniquement si `SECURITY` vaut yes :** demandez « Votre application utilise Spring Security, qui bloquera Master avec `401`/`403`. Puis-je en exclure uniquement `BASE/axelix-*/**` ? Ces endpoints restent protégés par le jeton de Master. »

Attendez la confirmation de l’utilisateur. Pour (b), utilisez la version de Master comme `VERSION` : un Master accepte les starters de sa propre version mineure et des trois précédentes, jamais des versions plus récentes. Si `WEB` vaut reactive et que Master est antérieur à 1.3.0, arrêtez-vous avec le message de l’étape 1.

## Étape 3 : ajouter le starter et le plugin de build

Ajoutez-les dans `BOOT_MODULE`. Si une ancienne version d’Axelix y est déjà présente, proposez dans le plan de la passer à `VERSION`. Si `ACTUATOR` vaut no, ajoutez également `spring-boot-starter-actuator`.

Maven :

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

Gradle :

- dépendance `com.axelixlabs:STARTER:VERSION` dans `implementation` ;
- identifiant du plugin `com.axelixlabs.axelix`, version `VERSION` ;
- le plugin est sur Maven Central. Si `settings.gradle(.kts)` contient un bloc `pluginManagement`, ajoutez `mavenCentral()` à ses `repositories`. Sinon, ajoutez ceci comme premier bloc du fichier :

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- le projet doit avoir une `version`. S’il n’en a pas, ajoutez `version = "0.0.1-SNAPSHOT"`.

Si l’utilisateur a accepté l’exclusion de Security, les requêtes de Master vers `BASE/axelix-*/**` doivent contourner l’authentification de l’application. À savoir :

- Master envoie son propre jeton dans `Authorization: Bearer`. Un serveur de ressources le rejette même sur les chemins configurés avec `permitAll()`.
- Les actions de Master sont des requêtes `POST` et `DELETE` ; CSRF ne doit donc pas s’appliquer à ces chemins.
- Les endpoints restent protégés : le starter vérifie lui-même le jeton de Master.

Ajoutez l’exclusion en respectant la configuration Security existante du projet. S’il n’y a pas d’emplacement approprié, ajoutez ce bean dans une application servlet :

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

Dans une application réactive (`WEB` vaut reactive), `WebSecurityCustomizer` n’existe pas. Ajoutez une chaîne distincte qui s’exécute en premier :

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

Si l’application ne possède pas de `SecurityWebFilterChain` propre et utilise celle par défaut, l’ajout de ce bean désactive cette dernière. Ajoutez alors aussi une chaîne pour le reste de l’application qui conserve sa protection actuelle, et indiquez-le dans le plan.

## Étape 4a : essai local

**Activez les utilisateurs locaux dans le Master d’essai** avec `axelix.master.auth.options.local.enabled=true` (la variable `AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED` dans les commandes ci-dessous). Cette option est désactivée par défaut. Elle permet à l’utilisateur de créer des comptes dans Master et d’essayer les rôles, au lieu de se connecter uniquement en tant qu’`admin`.

**Démarrez Master de la même manière que l’application.** Si `APP_IN_COMPOSE` vaut yes, ajoutez Master à ce fichier Compose. Sinon, exécutez Master avec `docker run` et ne l’ajoutez pas à un fichier Compose qui n’exécute pas l’application.

Si `APP_IN_COMPOSE` vaut yes :

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "127.0.0.1:2935:2935"
    environment:
      AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED: "true"
```

Si le service de l’application déclare `networks:`, ajoutez Master aux mêmes réseaux. Démarrez-le ensuite avec `docker compose up -d axelix-master`.

Sinon, exécutez-le avec `docker run`. Si Docker n’est pas installé, demandez à l’utilisateur de l’installer.

Si Axelix Master s’exécute déjà sur cette machine (un service répond sur le port `2935`, ou un conteneur utilise l’image `ghcr.io/axelixlabs/axelix`), ne le supprimez pas et ne le redémarrez pas : demandez à l’utilisateur s’il souhaite le réutiliser. Si le port `2935` est occupé par autre chose, démarrez Master sur un autre port et utilisez celui-ci dans `MASTER`. Si le nom `axelix-master` est pris par un autre conteneur, utilisez un autre nom.

```bash
docker run -d --name axelix-master -p 127.0.0.1:2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  -e AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED=true \
  ghcr.io/axelixlabs/axelix:VERSION
```

Attendez que `curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` affiche `200` (jusqu’à 60 secondes).

**Laissez l’application s’enregistrer elle-même dans Master :**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

Ne créez aucun nouveau fichier. Utilisez le premier cas qui convient :

1. **`APP_IN_COMPOSE` vaut yes :** `MASTER` vaut `http://axelix-master:2935`, `HOST` est le nom du service de l’application. Ajoutez les paramètres à la section `environment:` de l’application : `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`, `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **`LOCAL_PROFILE` existe :** `MASTER` vaut `http://localhost:2935`, `HOST` vaut `host.docker.internal`. Ajoutez les paramètres à ce profil et démarrez l’application avec celui-ci.
3. **Aucun des deux :** les mêmes valeurs que dans le cas 2, transmises comme arguments de ligne de commande au démarrage de l’application.

Master et le starter utilisent la même clé de signature intégrée ; un essai ne nécessite donc aucune configuration de clé. Si le projet définit déjà `axelix.sbs.auth.jwt.*`, ne modifiez pas ces lignes. Le Master d’essai a besoin de la même clé et du même algorithme : demandez à l’utilisateur de les transmettre au démarrage de Master (`axelix.master.auth.jwt.signing-key` et `axelix.master.auth.jwt.algorithm`, par exemple comme variables d’environnement de `docker run`). Vous ne voyez jamais la clé.

**Démarrez l’application** comme le projet le fait habituellement, en arrière-plan (la commande ne se termine pas). Le plugin de build s’exécute pendant le build ; avec Maven, celui-ci doit donc atteindre `package`. Par exemple :

- cas 1 : `docker compose up --build -d` ;
- cas 2, Gradle : `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven : `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`) ;
- cas 3, Gradle (Maven : les mêmes arguments dans `-Dspring-boot.run.arguments="..."`) :

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

Si une commande de démarrage échoue (droits insuffisants, absence de réseau), n’utilisez pas `sudo`. Effectuez toutes les modifications de fichiers, puis terminez par le compte rendu (étape 5) et placez les commandes de démarrage sous « À faire par l’utilisateur ». N’attendez pas que l’utilisateur indique les avoir exécutées.

## Étape 4b : Master existant

Modifiez uniquement les fichiers. Ne déployez rien.

- **Uniquement si l’utilisateur a choisi l’auto-enregistrement :** ajoutez les quatre paramètres de l’étape 4a à la configuration de l’environnement où l’application s’exécute (son profil, ses valeurs Helm ou ses manifestes), pas au fichier principal `application.*`. Si le projet n’a pas de configuration de ce type, demandez à l’utilisateur où les placer. `MASTER` est l’URL de Master. `HOST:PORT` est l’adresse de l’application telle que Master peut l’atteindre. Rappelez à l’utilisateur que l’auto-enregistrement doit être activé dans Master : `axelix.master.discovery.self-registration.enabled` (`true` par défaut, mais le chart Helm officiel d’Axelix le définit à `false`). Sinon, n’ajoutez rien. Documentation : https://axelix.io/docs/start/configuring-spring-boot-starter.md, section « Let Master find the instance ».
- **Uniquement si `VERSION` est inférieure à 1.2.0 :** le starter n’expose pas lui-même ses endpoints. Ajoutez-les à `management.endpoints.web.exposure.include` comme indiqué dans https://axelix.io/docs/start/configuring-spring-boot-starter.md, section « Exposing the endpoints on Axelix below 1.2.0 ».
- **Uniquement si Master utilise sa propre clé de signature ou son propre algorithme :** ajoutez seulement ce qui diffère des valeurs par défaut (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). Pour la clé, déterminez comment le projet fournit les autres secrets (examinez leurs références, pas leurs valeurs) et proposez de l’ajouter de la même manière. Si vous ne trouvez aucun exemple, interrogez l’utilisateur. Documentation : https://axelix.io/docs/start/configuring-spring-boot-starter.md, section « Share the JWT signing key with Master ».
- **Uniquement si vous avez accès à ce Master via MCP :** après le déploiement de l’application par l’utilisateur, appelez l’outil qui répertorie les services de Master (`getWallboard` jusqu’à 1.2.x, `getInstances` à partir de 1.3.0) et vérifiez que `NAME` figure dans la liste. Sans accès, ne vérifiez rien.

## Étape 5 : vérifier et rendre compte

Pour l’essai local : demandez à l’utilisateur d’ouvrir http://localhost:2935, de se connecter avec `admin` / `admin` et de confirmer que `NAME` figure dans la liste (environ 30 secondes après le démarrage). Si ce n’est pas le cas, lisez les journaux de Master et de l’application :

| Symptôme | Correction |
|---|---|
| `400` lors de l’enregistrement | Le plugin de build ne s’est pas exécuté (Maven nécessite `package`). |
| `401` ou `403` | Spring Security bloque Master : ajoutez l’exclusion. `403` uniquement sur les actions : excluez le chemin de CSRF. |
| Aucune erreur, l’application n’apparaît jamais | Les clés de signature diffèrent ou les paramètres ne sont pas actifs. |
| Master ne peut pas se connecter à l’application | Vérifiez `HOST` ci-dessus ; un pare-feu peut bloquer l’accès à `PORT` depuis Docker. |

Pour en savoir plus : https://axelix.io/docs/more/troubleshooting.md

Terminez par un bref compte rendu, un élément de liste pour chaque point :

- **Modifications :** les fichiers et ce que vous avez ajouté.
- **Vérifications :** ce que vous avez réellement vérifié.
- **À faire par l’utilisateur :** les commandes restant à exécuter, le cas échéant.
- **Pour annuler :** ce qu’il faut supprimer.
- **Pour arrêter Master** (essai local) : la commande.
- **Utilisateurs locaux** (essai local) : `axelix.master.auth.options.local.enabled=true` est activé ; les comptes créés dans Master peuvent donc se connecter. Dans un environnement réel, activez cette option uniquement si vous souhaitez y utiliser de tels comptes.
- **Avertissement** (essai local) : `admin` / `admin` et la clé intégrée sont publics et ne conviennent que sur cette machine.
- **Remarque** (uniquement si `WEB` vaut reactive) : les pages Transaction Control et Entities Map ne sont pas disponibles pour les applications réactives.
- **Pour poursuivre la lecture :** un environnement réel (https://axelix.io/docs/start/getting-started) et l’accès IA via MCP à l’adresse http://localhost:2935/api/mcp (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
