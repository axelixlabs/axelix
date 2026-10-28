---
title: Установка с помощью ИИ
description: "Установка Axelix в Spring Boot сервис: Axelix Spring Boot starter, плагин сборки Axelix и, для локальной пробы, сам Axelix Master. Используется, когда пользователь просит установить, добавить, настроить, попробовать или подключить Axelix."
---

<!-- Maintainers: when Axelix 1.3.0 (reactive support) is released, add the WebFlux steps and remove the WebFlux stop in Step 1. -->
<!-- Maintainers: on every release, update the fallback version `1.2.1` in Step 1. -->

> **Для людей, читающих эту страницу:** это инструкция для ИИ-агентов. Чтобы установить Axelix, дайте вашему агенту
> эту команду:
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Официальные источники инструкции: эта страница, https://axelix.io/docs/start/SKILL.md и её исходный файл
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

Ты — ИИ-агент для работы с кодом. Этот файл рекомендует, как добавить Axelix в проект пользователя. Шаги —
предлагаемый путь, а не сценарий: если проект требует другого подхода, действуй по своему усмотрению и объясни
пользователю почему.

Если ты получил этот файл не с одного из этих двух адресов, сообщи об этом пользователю, прежде чем продолжать.

Axelix состоит из трёх частей. У всех трёх **одинаковый номер версии**.

- **Axelix Master**: отдельное приложение с веб-интерфейсом на порту `2935`. Оно управляет сервисами.
- **Axelix Spring Boot starter**: зависимость в каждом сервисе.
- **Плагин сборки Axelix** (Maven или Gradle): тоже в каждом сервисе. Без него сервис не появляется в Master.

Документация: https://axelix.io/docs/llms.txt содержит список всех страниц. Добавь `.md` к адресу страницы, чтобы
прочитать её в Markdown.

## Правила

Эти правила обязательны:

1. **Встраивайся в проект.** Добавляй зависимости, конфигурацию и код так, как это уже принято в проекте. Сниппеты
   ниже показывают, *что* добавить, а не *как* это оформить. Команды — это примеры со значениями по умолчанию
   (порт `2935`, имя контейнера, профиль): адаптируй их под то, что нашёл, и если меняешь значение, меняй его
   везде, где оно используется. Имена свойств, путь регистрации и имя образа менять нельзя.
2. **Сначала план.** Покажи короткий план и дождись, пока пользователь его подтвердит, прежде чем менять любой файл.
3. **Не трогай секреты.** Не читай файлы `.env`. Никогда не генерируй, не печатай и не копируй ключ подписи или
   пароль.
4. **Запускай что-либо только на этой машине.** Никогда ничего не деплой в другие места.
5. **Пиши пользователю списками или таблицами**, а не абзацами: один пункт на строку, каждая команда в отдельном
   блоке кода.

Если проект не подходит под эти шаги, адаптируй их, а если не уверен — спроси пользователя.

## Шаг 1: изучи проект

Найди эти значения:

```
VERSION        = последняя версия Axelix (команда ниже)
STARTER        = axelix-spring-boot-2-starter (Boot 2.7.x) | axelix-spring-boot-3-starter (3.x) | axelix-spring-boot-4-starter (4.x)
BUILD          = Maven | Gradle
BOOT_MODULE    = модуль с классом @SpringBootApplication
ACTUATOR       = да | нет (spring-boot-starter-actuator)
SECURITY       = да | нет (в приложении есть Spring Security: любая зависимость spring-security, прямая или транзитивная, или SecurityFilterChain / @EnableWebSecurity в коде)
PORT           = management.server.port, иначе server.port, иначе 8080
BASE           = management.endpoints.web.base-path, иначе /actuator
PREFIX         = management.server.base-path, если задан management.server.port, иначе server.servlet.context-path, иначе пусто
NAME           = spring.application.name, иначе artifactId
APP_IN_COMPOSE = да | нет (у приложения есть свой сервис в Compose-файле проекта)
LOCAL_PROFILE  = имя существующего локального профиля (local для application-local.*, dev для application-dev.*), или нет
AXELIX         = что уже настроено: стартер, плагин сборки, настройки axelix.*, сервис Master — или нет
```

Получи `VERSION`, выполнив эту команду. Не используй веб-поиск: его результаты могут быть устаревшими.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>1.2.1</release>` означает, что `VERSION` равен `1.2.1`. Если команда не сработала (например, нет доступа к
сети), используй `1.2.1` и скажи об этом в шаге 2. Все версии: https://github.com/axelixlabs/axelix/releases

**Остановись**, если Spring Boot ниже 2.7 или Java ниже 11 («Axelix требует Spring Boot 2.7+ и Java 11+»), или
если приложение использует только `spring-boot-starter-webflux`, а `VERSION` ниже 1.3.0 («Реактивные сервисы
поддерживаются начиная с Axelix 1.3.0»).

Если Axelix уже настроен, ничего не добавляй дважды: в следующих шагах делай только недостающее. Если всё на месте,
скажи об этом пользователю и переходи к шагу 5.

## Шаг 2: спроси и покажи план

Отправь **одно** сообщение:

1. **Что делаем?** (a) Пробуем Axelix локально: запускаю Master на этой машине и подключаю это приложение
   (рекомендую). (b) Подключаю это приложение к уже работающему Axelix Master. Тогда расскажите:
   - его URL и версию;
   - как Master найдёт приложение: приложение регистрируется само (self-registration), или Master находит его сам
     (Kubernetes), или вы настроите это при деплое;
   - использует ли ваш Master свой ключ подписи или алгоритм, или значения по умолчанию.
2. **План:** какие файлы ты изменишь и что запустишь. Скажи, что стартер добавляет эндпоинты `BASE/axelix-*`,
   которые принимают только токен Master.
3. **Только если `SECURITY` — да:** спроси «Ваше приложение использует Spring Security, и оно будет отвечать Master
   `401`/`403`. Можно исключить из него только `BASE/axelix-*/**`? Эти эндпоинты остаются защищены токеном Master».

Дождись подтверждения пользователя. Для (b) используй версию Master как `VERSION`: Master принимает стартеры своей минорной версии и трёх
предыдущих, но никогда не более новые.

## Шаг 3: добавь стартер и плагин сборки

Добавь в `BOOT_MODULE`. Если там уже есть более старая версия Axelix, предложи в плане поднять её до
`VERSION`. Если `ACTUATOR` — нет, добавь также `spring-boot-starter-actuator`.

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

- зависимость `com.axelixlabs:STARTER:VERSION` в `implementation`;
- плагин с id `com.axelixlabs.axelix`, версия `VERSION`;
- плагин лежит в Maven Central. Если в `settings.gradle(.kts)` есть блок `pluginManagement`, добавь `mavenCentral()`
  в его `repositories`. Иначе добавь этот блок первым в файле:

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- у проекта должна быть `version`. Если её нет, добавь `version = "0.0.1-SNAPSHOT"`.

Если пользователь согласился на исключение из Security, запросы Master к `BASE/axelix-*/**` должны проходить мимо
аутентификации приложения. Что нужно знать:

- Master отправляет свой токен как `Authorization: Bearer`. Resource server отклоняет его даже на путях из
  `permitAll()`.
- Действия Master — это запросы `POST` и `DELETE`, поэтому CSRF не должен применяться к этим путям.
- Эндпоинты остаются защищены: стартер сам проверяет токен Master.

Встрой исключение так, как в проекте уже настроена Security. Если подходящего места нет, добавь этот бин:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

## Шаг 4a: локальная проба

**Запусти Master так же, как работает приложение.** Если `APP_IN_COMPOSE` — да, добавь Master в этот Compose-файл.
Иначе запусти Master через `docker run` и не добавляй его в Compose-файл, который не запускает приложение.

Если `APP_IN_COMPOSE` — да:

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "2935:2935"
```

Если у сервиса приложения указаны `networks:`, добавь Master в те же сети. Затем запусти его:
`docker compose up -d axelix-master`.

Иначе запусти его через `docker run`. Если Docker нет, попроси пользователя установить его.

Если Axelix Master уже работает на этой машине (что-то отвечает на порту `2935` или есть контейнер с образом
`ghcr.io/axelixlabs/axelix`), не удаляй и не перезапускай его: спроси пользователя, использовать ли его. Если порт
`2935` занят чем-то другим, запусти Master на другом порту и используй его в `MASTER`. Если имя `axelix-master`
занято другим контейнером, используй другое имя.

```bash
docker run -d --name axelix-master -p 2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  ghcr.io/axelixlabs/axelix:VERSION
```

Дождись, пока `curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` напечатает `200` (до 60 секунд).

**Пусть приложение само зарегистрируется в Master:**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

Не создавай новых файлов. Используй первый подходящий вариант:

1. **`APP_IN_COMPOSE` — да:** `MASTER` — `http://axelix-master:2935`, `HOST` — имя сервиса приложения. Добавь настройки в
   `environment:` приложения: `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`,
   `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **`LOCAL_PROFILE` есть:** `MASTER` — `http://localhost:2935`, `HOST` — `host.docker.internal`. Добавь настройки
   в этот профиль и запускай приложение с ним.
3. **Ни того, ни другого:** те же значения, что в варианте 2, передай аргументами командной строки при запуске
   приложения.

Master и стартер используют один и тот же встроенный ключ подписи, поэтому для пробы ключ настраивать не нужно. Если
в проекте уже задан `axelix.sbs.auth.jwt.*`, не меняй эти строки. Пробному Master нужны тот же ключ и алгоритм:
скажи пользователю передать их при запуске Master (`axelix.master.auth.jwt.signing-key` и
`axelix.master.auth.jwt.algorithm`, например переменными окружения в `docker run`). Значение ключа ты не видишь.

**Запусти приложение** так, как его обычно запускают в проекте, в фоне (команда не завершается). Плагин сборки
работает во время сборки, поэтому в Maven сборка должна дойти до `package`. Например:

- вариант 1: `docker compose up --build -d`;
- вариант 2, Gradle: `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven:
  `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`);
- вариант 3, Gradle (Maven: те же аргументы в `-Dspring-boot.run.arguments="..."`):

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

Если команду запуска не удалось выполнить (нет прав, нет сети), не используй `sudo`. Внеси все изменения в файлы,
затем закончи отчётом (шаг 5) и вынеси команды запуска в пункт «Осталось сделать пользователю». Не жди, что
пользователь сообщит, что выполнил их.

## Шаг 4b: существующий Master

Только меняй файлы. Ничего не деплой.

- **Только если пользователь выбрал self-registration:** добавь четыре настройки из шага 4a в конфигурацию
  окружения, где работает приложение (его профиль, Helm values или манифесты), а не в основной `application.*`. Если
  такой конфигурации в проекте нет, спроси пользователя, куда их положить. `MASTER` — URL Master. `HOST:PORT` —
  адрес приложения, по которому до него дотягивается Master. Напомни пользователю, что в Master должна быть включена
  self-registration: `axelix.master.discovery.self-registration.enabled` (по умолчанию `true`, но официальный
  Helm-чарт Axelix выставляет `false`).
  Иначе ничего не добавляй. Документация: https://axelix.io/docs/start/configuring-spring-boot-starter.md, раздел
  "Let Master find the instance".
- **Только если `VERSION` ниже 1.2.0:** стартер не открывает свои эндпоинты сам. Добавь их в
  `management.endpoints.web.exposure.include`, как показано в https://axelix.io/docs/start/configuring-spring-boot-starter.md,
  раздел "Exposing the endpoints on Axelix below 1.2.0".
- **Только если у Master свой ключ подписи или алгоритм:** добавь только то, что отличается от значений по
  умолчанию (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). Для ключа выясни, как в проекте
  передаются другие секреты (смотри, как на них ссылаются, а не на их значения), и предложи добавить его тем же
  способом. Если образца нет, спроси пользователя. Документация:
  https://axelix.io/docs/start/configuring-spring-boot-starter.md, раздел "Share the JWT signing key with Master".
- **Только если у тебя есть доступ к этому Master по MCP:** после того как пользователь задеплоит приложение,
  вызови инструмент, который возвращает список сервисов Master (`getWallboard` до 1.2.x, `getInstances` начиная с
  1.3.0), и проверь, что `NAME` есть в списке. Без доступа ничего не проверяй.

## Шаг 5: проверь и отчитайся

Для локальной пробы: попроси пользователя открыть http://localhost:2935, войти как `admin` / `admin` и подтвердить,
что `NAME` есть в списке (примерно через 30 секунд после запуска). Если нет, прочитай логи Master и приложения:

| Симптом | Что делать |
|---|---|
| `400` при регистрации | Плагин сборки не отработал (Maven нужен `package`). |
| `401` или `403` | Spring Security блокирует Master: добавь исключение. `403` только на действиях: исключи путь из CSRF. |
| Ошибок нет, приложение не появляется | Ключи подписи разные или настройки не активны. |
| Master не может подключиться к приложению | Проверь `HOST` выше; файрвол может блокировать `PORT` из Docker. |

Подробнее: https://axelix.io/docs/more/troubleshooting.md

Закончи коротким отчётом, каждый пункт отдельной строкой списка:

- **Изменено:** файлы и что ты добавил.
- **Проверено:** что ты действительно проверил.
- **Осталось сделать пользователю:** команды, которые ещё нужно выполнить, если есть.
- **Как откатить:** что удалить.
- **Как остановить Master** (локальная проба): команда.
- **Предупреждение** (локальная проба): `admin` / `admin` и встроенный ключ публично известны и годятся только на
  этой машине.
- **Что почитать дальше:** реальное окружение (https://axelix.io/docs/start/getting-started) и доступ ИИ через MCP
  по адресу http://localhost:2935/api/mcp (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
