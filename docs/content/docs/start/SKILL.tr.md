---
title: Yapay zekâ ile kurulum
name: install-axelix
description: "Axelix'i bir Spring Boot servisine kurun: Axelix Spring Boot starter, Axelix derleme eklentisi ve yerel deneme için Axelix Master. Kullanıcı Axelix'i kurmak, eklemek, ayarlamak, denemek veya bağlamak istediğinde kullanın."
---

> **Bu sayfayı okuyan kişiler için:** bu sayfa, yapay zekâ kodlama ajanlarına yönelik bir yönergedir. Axelix'i kurmak için
> ajanınıza şu komutu verin:
>
> ```
> Install Axelix in my app: run `curl -s https://axelix.io/docs/start/SKILL.md` and use it as a guide.
> ```
>
> Bu yönergenin resmî kaynakları: bu sayfa, https://axelix.io/docs/start/SKILL.md ve kaynak dosyası
> https://raw.githubusercontent.com/axelixlabs/axelix/master/docs/content/docs/start/SKILL.md.

Siz bir yapay zekâ kodlama ajanısınız. Bu dosya, Axelix'in kullanıcının projesine nasıl ekleneceğini önerir. Adımlar bir
komut dosyası değil, önerilen bir yoldur: proje farklı bir yaklaşım gerektiriyorsa kendi değerlendirmenizi yapın ve
nedenini kullanıcıya açıklayın.

Bu dosyayı bu iki adres dışında bir yerden aldıysanız devam etmeden önce kullanıcıya bildirin.

Axelix üç parçadan oluşur. Üçü de **aynı sürüm numarasını** kullanır.

- **Axelix Master**: `2935` portunda web arayüzü sunan bağımsız bir uygulama. Servisleri yönetir.
- **Axelix Spring Boot starter**: her serviste yer alan bir bağımlılık.
- **Axelix derleme eklentisi** (Maven veya Gradle): bu da her serviste bulunur. Olmadan servis Master'da görünmez.

Dokümantasyon: https://axelix.io/docs/llms.txt tüm sayfaları listeler. Bir sayfayı Markdown olarak okumak için URL'sine `.md` ekleyin.

## Kurallar [#rules]

Bu kurallar isteğe bağlı değildir:

1. **Projeye uyum sağlayın.** Bağımlılıkları, yapılandırmayı ve kodu projenin mevcut yöntemleriyle ekleyin. Aşağıdaki
   örnekler *neyin* ekleneceğini gösterir, *nasıl* biçimlendirileceğini değil. Komutlar varsayılan değerlerle verilmiş
   örneklerdir (port `2935`, konteyner adı, profil): bunları bulduklarınıza uyarlayın ve bir değeri değiştirirseniz
   kullanıldığı her yerde değiştirin. Özellik adları, kayıt yolu ve imaj adı sabittir.
2. **Önce planlayın.** Herhangi bir dosyayı değiştirmeden önce kısa bir plan gösterin ve kullanıcının onayını bekleyin.
3. **Gizli bilgilere dokunmayın.** `.env` dosyalarını okumayın. Asla imzalama anahtarı veya parola üretmeyin, yazdırmayın veya kopyalamayın.
4. **Yalnızca bu makinede çalıştırın.** Asla başka bir ortama dağıtım yapmayın.
5. **Kullanıcıya paragraflar yerine liste veya tablolarla yazın**: her öğe ayrı satırda, her komut kendi kod bloğunda olsun.

Proje bu adımlara uymuyorsa uyarlayın veya emin olmadığınızda kullanıcıya sorun.

## Adım 1: Projeyi inceleyin [#step-1-look-at-the-project]

Şu değerleri bulun:

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

Bu komutu çalıştırarak `VERSION` değerini alın. Web araması kullanmayın; sonuçları güncel olmayabilir.

```bash
curl -s https://repo1.maven.org/maven2/com/axelixlabs/axelix-spring-boot-3-starter/maven-metadata.xml | grep '<release>'
```

`<release>AXELIX_VERSION_PLACEHOLDER</release>`, `VERSION` değerinin `AXELIX_VERSION_PLACEHOLDER` olduğu anlamına gelir.
Komut başarısız olursa (örneğin ağ erişimi yoksa) `AXELIX_VERSION_PLACEHOLDER` kullanın ve bunu Adım 2'de belirtin.
Tüm sürümler: https://github.com/axelixlabs/axelix/releases

Spring Boot 2.7'den veya Java 11'den eskiyse **durun** ("Axelix, Spring Boot 2.7+ ve Java 11+ gerektirir");
ya da `WEB` reaktif ve `VERSION` 1.3.0'dan düşükse durun ("Reaktif servisler Axelix 1.3.0'dan itibaren desteklenir").

Axelix zaten kurulmuşsa hiçbir şeyi ikinci kez eklemeyin: sonraki adımlarda yalnızca eksikleri tamamlayın. Her şey
hazırsa kullanıcıya bildirin ve Adım 5'e geçin.

## Adım 2: Sorun ve planlayın [#step-2-ask-and-plan]

**Tek bir** ileti gönderin:

1. **Ne yapılacak?** (a) Axelix'i yerel olarak deneyin: Master'ı bu makinede çalıştırın ve bu uygulamayı bağlayın (önerilen).
   (b) Bu uygulamayı zaten çalışan bir Axelix Master'a bağlayın. Bu seçenek için kullanıcıdan şu bilgileri isteyin:
   - URL'si ve sürümü;
   - Master'ın bu uygulamayı nasıl bulacağı: uygulama kendisi kaydolur (kendi kendine kayıt), Master kendisi bulur
     (Kubernetes) veya dağıtım sırasında ayarlanır;
   - Master'ınızın kendi imzalama anahtarını veya algoritmasını mı yoksa varsayılanları mı kullandığı.
2. **Plan:** değiştireceğiniz dosyalar ve başlatacağınız bileşenler. Starter'ın yalnızca Master'ın belirtecini kabul eden
   `BASE/axelix-*` uç noktaları eklediğini söyleyin.
3. **Yalnızca `SECURITY` evetse:** şunu sorun: "Uygulamanız Spring Security kullanıyor; bu, Master'ı `401`/`403` ile
   engelleyecek. Yalnızca `BASE/axelix-*/**` yolunu bundan hariç tutabilir miyim? Bu uç noktalar Master'ın belirteciyle
   korunmaya devam eder."

Kullanıcının onayını bekleyin. (b) için `VERSION` olarak Master'ın sürümünü kullanın: Master, kendi minör sürümündeki ve
önceki üç minör sürümdeki starter'ları kabul eder, daha yenilerini asla kabul etmez. `WEB` reaktif ve Master 1.3.0'dan
eskiyse Adım 1'deki iletiyle durun.

## Adım 3: Starter'ı ve derleme eklentisini ekleyin [#step-3-add-the-starter-and-the-build-plugin]

`BOOT_MODULE` modülüne ekleyin. Eski bir Axelix sürümü zaten varsa planda bunu `VERSION` değerine yükseltmeyi önerin.
`ACTUATOR` hayırsa `spring-boot-starter-actuator` bağımlılığını da ekleyin.

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

- `implementation` içinde `com.axelixlabs:STARTER:VERSION` bağımlılığı;
- `com.axelixlabs.axelix` eklenti kimliği, `VERSION` sürümü;
- eklenti Maven Central'dadır. `settings.gradle(.kts)` dosyasında `pluginManagement` bloğu varsa `repositories` bölümüne
  `mavenCentral()` ekleyin. Yoksa bunu dosyanın ilk bloğu olarak ekleyin:

  ```kotlin
  pluginManagement {
      repositories {
          gradlePluginPortal()
          mavenCentral()
      }
  }
  ```

- projenin bir `version` değeri olmalıdır. Yoksa `version = "0.0.1-SNAPSHOT"` ekleyin.

Kullanıcı Security istisnasını kabul ettiyse Master'ın `BASE/axelix-*/**` istekleri, uygulamanın kimlik doğrulamasını
atlamalıdır. Bilmeniz gerekenler:

- Master kendi belirtecini `Authorization: Bearer` olarak gönderir. Kaynak sunucusu bunu `permitAll()` yollarında bile reddeder.
- Master'ın işlemleri `POST` ve `DELETE` istekleridir; bu nedenle bu yollara CSRF uygulanmamalıdır.
- Uç noktalar korunmaya devam eder: starter, Master'ın belirtecini kendisi denetler.

İstisnayı, projenin Security yapılandırmasını yaptığı mevcut yöntemle ekleyin. Uygun bir yer yoksa servlet uygulamasına
bu bean'i ekleyin:

Spring Boot 3/4 (Spring Security 6/7) için:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().requestMatchers("BASE/axelix-*/**");
}
```

Spring Boot 2.7 (Spring Security 5.7) için bunun yerine `antMatchers` kullanın:

```java
@Bean
public WebSecurityCustomizer axelixWebSecurityCustomizer() {
    return (web) -> web.ignoring().antMatchers("BASE/axelix-*/**");
}
```

Reaktif uygulamada (`WEB` reaktifken) `WebSecurityCustomizer` yoktur. İlk sırada çalışan ayrı bir zincir ekleyin:

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

Uygulamanın kendi `SecurityWebFilterChain` bileşeni yoksa ve varsayılanı kullanıyorsa bu bean'in eklenmesi varsayılanı
devre dışı bırakır. Bu durumda uygulamanın geri kalanı için mevcut korumayı sürdüren bir zincir de ekleyin ve planda bunu belirtin.

## Adım 4a: Yerel deneme [#step-4a-local-trial]

**Deneme Master'ında yerel kullanıcıları etkinleştirin:** `axelix.master.auth.options.local.enabled=true` kullanın
(aşağıdaki komutlarda `AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED` değişkeni). Varsayılan olarak kapalıdır. Bununla kullanıcı,
yalnızca `admin` olarak oturum açmak yerine Master'da kullanıcı hesapları oluşturabilir ve rolleri deneyebilir.

**Master'ı uygulamanın çalıştığı yöntemle başlatın.** `APP_IN_COMPOSE` evetse Master'ı aynı Compose dosyasına ekleyin.
Değilse Master'ı `docker run` ile çalıştırın; uygulamayı çalıştırmayan bir Compose dosyasına eklemeyin.

`APP_IN_COMPOSE` evetse:

```yaml
  axelix-master:
    image: ghcr.io/axelixlabs/axelix:VERSION
    ports:
      - "127.0.0.1:2935:2935"
    environment:
      AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED: "true"
```

Uygulamanın servisi `networks:` listeliyorsa Master'ı aynı ağlara ekleyin. Ardından `docker compose up -d axelix-master`
ile başlatın.

Diğer durumda `docker run` ile çalıştırın. Docker yoksa kullanıcıdan kurmasını isteyin.

Axelix Master bu makinede zaten çalışıyorsa (`2935` portunda yanıt veren bir şey varsa veya bir konteyner
`ghcr.io/axelixlabs/axelix` imajını kullanıyorsa) onu kaldırmayın veya yeniden başlatmayın: kullanıcıya yeniden kullanmak
isteyip istemediğini sorun. `2935` portunu başka bir şey kullanıyorsa Master'ı farklı bir portta başlatın ve `MASTER`
içinde bu portu kullanın. `axelix-master` adını başka bir konteyner kullanıyorsa farklı bir ad seçin.

```bash
docker run -d --name axelix-master -p 127.0.0.1:2935:2935 \
  --add-host=host.docker.internal:host-gateway \
  -e AXELIX_MASTER_AUTH_OPTIONS_LOCAL_ENABLED=true \
  ghcr.io/axelixlabs/axelix:VERSION
```

`curl -s -o /dev/null -w "%{http_code}" http://localhost:2935` komutu `200` yazdırana kadar bekleyin (en fazla 60 saniye).

**Uygulamanın Master'a kendi kendine kaydolmasını sağlayın:**

```properties
axelix.sbs.discovery.self-registration=true
axelix.sbs.discovery.master-url=MASTER/api/internal/service/register
axelix.sbs.discovery.instance-actuator-url=http://HOST:PORTPREFIXBASE
axelix.sbs.discovery.instance-name=NAME
```

Yeni dosya oluşturmayın. Uyan ilk durumu kullanın:

1. **`APP_IN_COMPOSE` evetse:** `MASTER`, `http://axelix-master:2935`; `HOST`, uygulamanın servis adıdır. Ayarları uygulamanın
   `environment:` bölümüne ekleyin: `AXELIX_SBS_DISCOVERY_SELFREGISTRATION`, `AXELIX_SBS_DISCOVERY_MASTERURL`,
   `AXELIX_SBS_DISCOVERY_INSTANCEACTUATORURL`, `AXELIX_SBS_DISCOVERY_INSTANCENAME`.
2. **`LOCAL_PROFILE` varsa:** `MASTER`, `http://localhost:2935`; `HOST`, `host.docker.internal` değerindedir. Ayarları bu
   profile ekleyin ve uygulamayı bu profille başlatın.
3. **İkisi de değilse:** durum 2'deki değerleri, uygulamayı başlatırken komut satırı argümanları olarak iletin.

Master ve starter aynı yerleşik imzalama anahtarını kullanır; bu nedenle deneme için anahtar ayarı gerekmez. Proje zaten
`axelix.sbs.auth.jwt.*` ayarlarını yapıyorsa bu satırları değiştirmeyin. Deneme Master'ı aynı anahtarı ve algoritmayı
gerektirir: kullanıcıya bunları Master başlarken iletmesini söyleyin (`axelix.master.auth.jwt.signing-key` ve
`axelix.master.auth.jwt.algorithm`; örneğin `docker run` ortam değişkenleri olarak). Anahtarı asla görmezsiniz.

**Uygulamayı**, projenin normal başlatma yöntemiyle arka planda başlatın (komut sonlanmaz). Derleme eklentisi derleme
sırasında çalıştığından Maven'da derlemenin `package` aşamasına ulaşması gerekir. Örneğin:

- durum 1: `docker compose up --build -d`;
- durum 2, Gradle: `./gradlew bootRun --args='--spring.profiles.include=LOCAL_PROFILE'` (Maven:
  `./mvnw package spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.include=LOCAL_PROFILE`);
- durum 3, Gradle (Maven: aynı argümanları `-Dspring-boot.run.arguments="..."` içinde kullanın):

  ```bash
  ./gradlew bootRun --args='--axelix.sbs.discovery.self-registration=true --axelix.sbs.discovery.master-url=http://localhost:2935/api/internal/service/register --axelix.sbs.discovery.instance-actuator-url=http://host.docker.internal:PORTPREFIXBASE --axelix.sbs.discovery.instance-name=NAME'
  ```

Başlatma komutu başarısız olursa (izin yok, ağ yok) `sudo` kullanmayın. Tüm dosya değişikliklerini tamamlayın, ardından
raporla (Adım 5) bitirin ve başlatma komutlarını "Kullanıcının sonraki adımları" altına koyun. Kullanıcının bunları
çalıştırdığını söylemesini beklemeyin.

## Adım 4b: Mevcut Master [#step-4b-existing-master]

Yalnızca dosyaları değiştirin. Dağıtım yapmayın.

- **Yalnızca kullanıcı kendi kendine kaydı seçtiyse:** Adım 4a'daki dört ayarı ana `application.*` dosyasına değil,
  uygulamanın çalıştığı ortamın yapılandırmasına (profil, Helm değerleri veya manifestler) ekleyin. Projede böyle bir
  yapılandırma yoksa kullanıcıya nereye konacağını sorun. `MASTER`, Master URL'sidir. `HOST:PORT`, uygulamanın Master'dan
  erişilen adresidir. Master'da kendi kendine kaydın açık olması gerektiğini hatırlatın:
  `axelix.master.discovery.self-registration.enabled` (varsayılan `true`, ancak resmî Axelix Helm chart'ı bunu `false` yapar).
  Diğer durumda hiçbir şey eklemeyin. Dokümantasyon: https://axelix.io/docs/start/configuring-spring-boot-starter.md,
  "Let Master find the instance" bölümü.
- **Yalnızca `VERSION` 1.2.0'dan düşükse:** starter uç noktalarını kendisi açmaz. Bunları
  https://axelix.io/docs/start/configuring-spring-boot-starter.md adresindeki "Exposing the endpoints on Axelix below 1.2.0"
  bölümünde gösterildiği gibi `management.endpoints.web.exposure.include` listesine ekleyin.
- **Yalnızca Master kendi imzalama anahtarını veya algoritmasını kullanıyorsa:** yalnızca varsayılanlardan farklı olanları
  ekleyin (`axelix.sbs.auth.jwt.signing-key`, `axelix.sbs.auth.jwt.algorithm`). Anahtar için projenin diğer gizli bilgileri
  nasıl sağladığını bulun (değerlerine değil, nasıl referans verildiğine bakın) ve aynı yöntemle eklemeyi önerin.
  Örnek bulamazsanız kullanıcıya sorun. Dokümantasyon: https://axelix.io/docs/start/configuring-spring-boot-starter.md,
  "Share the JWT signing key with Master" bölümü.
- **Yalnızca bu Master'a MCP erişiminiz varsa:** kullanıcı uygulamayı dağıttıktan sonra Master'ın servislerini listeleyen
  aracı çağırın (1.2.x'e kadar `getWallboard`, 1.3.0'dan itibaren `getInstances`) ve `NAME` değerinin listelendiğini
  denetleyin. Erişim yoksa hiçbir şeyi denetlemeyin.

## Adım 5: Denetleyin ve raporlayın [#step-5-check-and-report]

Yerel deneme için: kullanıcıdan http://localhost:2935 adresini açmasını, `admin` / `admin` ile oturum açmasını ve `NAME`
değerinin listelendiğini doğrulamasını isteyin (başlatmadan yaklaşık 30 saniye sonra). Listelenmiyorsa Master'ın ve
uygulamanın günlüklerini okuyun:

| Belirti | Çözüm |
|---|---|
| Kayıt sırasında `400` | Derleme eklentisi çalışmadı (Maven `package` gerektirir). |
| `401` veya `403` | Spring Security Master'ı engelliyor: istisnayı ekleyin. Yalnızca işlemlerde `403`: yolu CSRF'den hariç tutun. |
| Hata yok, uygulama hiç görünmüyor | İmzalama anahtarları farklı veya ayarlar etkin değil. |
| Master uygulamaya bağlanamıyor | Yukarıdaki `HOST` değerini denetleyin; güvenlik duvarı Docker'dan `PORT` erişimini engelliyor olabilir. |

Daha fazlası: https://axelix.io/docs/more/troubleshooting.md

Her biri bir liste öğesi olacak şekilde kısa bir raporla bitirin:

- **Değiştirilenler:** dosyalar ve ekledikleriniz.
- **Denetlenenler:** gerçekten denetledikleriniz.
- **Kullanıcının sonraki adımları:** varsa hâlâ çalıştırılması gereken komutlar.
- **Geri almak için:** kaldırılması gerekenler.
- **Master'ı durdurmak için** (yerel deneme): komut.
- **Yerel kullanıcılar** (yerel deneme): `axelix.master.auth.options.local.enabled=true` açıktır; dolayısıyla Master'da
  oluşturulan kullanıcı hesapları oturum açabilir. Gerçek bir ortamda yalnızca böyle hesaplar istiyorsanız etkinleştirin.
- **Uyarı** (yerel deneme): `admin` / `admin` ve yerleşik anahtar herkese açıktır ve yalnızca bu makinede kullanım için uygundur.
- **Not** (yalnızca `WEB` reaktifse): Transaction Control ve Entities Map sayfaları reaktif uygulamalarda kullanılamaz.
- **Sonraki okumalar:** gerçek ortam (https://axelix.io/docs/start/getting-started) ve
  http://localhost:2935/api/mcp adresinden MCP üzerinden yapay zekâ erişimi (https://axelix.io/docs/setting-up-master-ui/mcp/mcp-tools).
