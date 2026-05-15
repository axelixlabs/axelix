/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.axelixlabs.axelix.sbs.spring.core.shared;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.jspecify.annotations.NonNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import com.axelixlabs.axelix.common.auth.core.AuthenticationSchemes;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.service.JwtEncoderService;
import com.axelixlabs.axelix.common.domain.version.AxelixVersionDiscoverer;
import com.axelixlabs.axelix.sbs.spring.core.auth.JwtAuthTestConfiguration;
import com.axelixlabs.axelix.sbs.spring.core.beans.DefaultBeanMetaInfoExtractorTestFixtures;
import com.axelixlabs.axelix.sbs.spring.core.beans.QualifiersPersistenceTestFixtures;
import com.axelixlabs.axelix.sbs.spring.core.config.EndpointsConfigurationProperties;
import com.axelixlabs.axelix.sbs.spring.core.configprops.AxelixConfigurationPropertiesEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.configprops.ConfigurationPropertiesService;
import com.axelixlabs.axelix.sbs.spring.core.configprops.SmartSanitizingFunction;
import com.axelixlabs.axelix.sbs.spring.core.env.AxelixEnvironmentEndpoint;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentService;
import com.axelixlabs.axelix.sbs.spring.core.env.EnvironmentTestConfig;
import com.axelixlabs.axelix.sbs.spring.core.env.PropertyNameNormalizer;
import com.axelixlabs.axelix.sbs.spring.core.env.TestBeanWithCustomAnnotations;
import com.axelixlabs.axelix.sbs.spring.core.env.TestBeanWithSpEL;
import com.axelixlabs.axelix.sbs.spring.core.transactions.OwnerRepository;
import com.axelixlabs.axelix.sbs.spring.core.transactions.PropagationTestHelper;

/**
 * Single shared {@link TestConfiguration} that registers the union of beans
 * required across all endpoint integration tests.
 *
 * <p>By consolidating these beans into one configuration, all
 * {@code *EndpointTest} classes can share a single Spring
 * {@code ApplicationContext}, which dramatically speeds up the test suite
 * since the Spring TestContext framework caches contexts based on their
 * effective configuration.
 *
 * @since 14.05.2026
 * @author Artemiy Degtyarev
 */
@TestConfiguration
@Import({
    EnvironmentTestConfig.class,
    JwtAuthTestConfiguration.class,
    QualifiersPersistenceTestFixtures.class,
    DefaultBeanMetaInfoExtractorTestFixtures.class
})
@EnableConfigurationProperties(AxelixPropTest.class)
public class SharedEndpointTestConfiguration implements SchedulingConfigurer {

    // -----------------------------------------------------------------------------------------------------------------
    // TestRestTemplate authentication.
    //
    // With the shared context, the JWT authorization filter (from {@link JwtAuthTestConfiguration}) is registered for
    // every endpoint test. The default {@link TestRestTemplate} bean injected via {@code @SpringBootTest} has no JWT
    // token, so every actuator request would receive 401. The {@link BeanPostProcessor} below intercepts the default
    // {@code TestRestTemplate} and installs an admin-role JWT as the default {@code Authorization} header. Tests that
    // need a specific role can still use {@link com.axelixlabs.axelix.sbs.spring.core.utils.TestRestTemplateBuilder}.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public TestRestTemplateAuthInstaller testRestTemplateAuthInstaller(
            TestRestTemplate testRestTemplate, JwtEncoderService jwtEncoderService) {
        return new TestRestTemplateAuthInstaller(testRestTemplate, jwtEncoderService);
    }

    static class TestRestTemplateAuthInstaller {

        TestRestTemplateAuthInstaller(TestRestTemplate testRestTemplate, JwtEncoderService jwtEncoderService) {
            String token = jwtEncoderService.generateToken(
                    new DefaultUser("testUser", "testPassword", java.util.Set.of(DefaultRole.ADMIN)));
            testRestTemplate.getRestTemplate().getInterceptors().add((request, body, execution) -> {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    request.getHeaders().set(HttpHeaders.AUTHORIZATION, AuthenticationSchemes.BEARER.prefix() + token);
                }
                return execution.execute(request, body);
            });
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Scheduled task flags (set by @Scheduled methods, observed by AxelixScheduledTasksEndpointTest).
    // -----------------------------------------------------------------------------------------------------------------

    public static volatile boolean cronFlag = false;
    public static volatile boolean fixedDelayFlag = false;
    public static volatile boolean fixedRateFlag = false;
    public static volatile boolean customTaskFlag = false;

    public static final String CUSTOM_TRIGGER_NAME = "CustomTestTrigger";

    // -----------------------------------------------------------------------------------------------------------------
    // Cache test infrastructure.
    // -----------------------------------------------------------------------------------------------------------------

    public static final String TEST_CACHE_1 = "cache1";
    public static final String TEST_CACHE_2 = "cache2";

    public static final String MAIN_CACHE_MANAGER = "mainCacheManager";
    public static final String CLEAR_CACHE_MANAGER = "clearCacheManager";
    public static final String ENABLE_CACHE_MANAGER = "enableCacheManager";
    public static final String DISABLE_CACHE_MANAGER = "disableCacheManager";

    @Bean(name = MAIN_CACHE_MANAGER)
    @Primary
    public CacheManager mainCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = CLEAR_CACHE_MANAGER)
    public CacheManager clearCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = ENABLE_CACHE_MANAGER)
    public CacheManager enableCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    @Bean(name = DISABLE_CACHE_MANAGER)
    public CacheManager disableCacheManager() {
        return new ConcurrentMapCacheManager(TEST_CACHE_1, TEST_CACHE_2);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Beans endpoint test fixtures.
    // -----------------------------------------------------------------------------------------------------------------

    public static final String CUSTOM_SUPPLIER = "customSupplier";

    @Bean(CUSTOM_SUPPLIER)
    public Supplier<String> customSupplier() {
        return () -> "value";
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Conditions endpoint test fixtures.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "enabled")
    public String positiveConditionBean() {
        return "positive";
    }

    @Bean
    @ConditionalOnProperty(name = "axelix.conditions.test.flag", havingValue = "disabled")
    public String negativeConditionBean() {
        return "negative";
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Metrics endpoint test fixtures.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public MeterBinder groupingMetrics() {
        return registry -> {
            Counter.builder("axelixMetrics.test.metric1")
                    .description("Test metric belonging to the `axelixMetrics` group with a description")
                    .register(registry);

            Counter.builder("axelixMetrics.test.metric2")
                    .description("Test metric belonging to the `axelixMetrics` group with a description")
                    .register(registry);

            Counter.builder("axelixMetrics.test.metric3").register(registry);

            Counter.builder("testMetrics.axelix.metric1")
                    .description("Test metric belonging to the `testMetrics` group with a description")
                    .register(registry);

            Counter.builder("testMetrics.axelix.metric2").register(registry);

            Counter.builder("standalone")
                    .description("Test metric belonging to the 'Others' group without a prefix and with a description")
                    .register(registry);

            Gauge.builder("for.value.transformations", () -> 5480079 /* ~ 5.22 MB */)
                    .baseUnit("bytes")
                    .register(registry);
        };
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Metadata endpoint test fixtures.
    //
    // The metadata endpoint auto-configuration registers an {@link AxelixVersionDiscoverer} via
    // {@code @ConditionalOnMissingBean}. We expose our own bean here so that {@code AxelixMetadataEndpointTest}
    // can assert against a deterministic version string.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public AxelixVersionDiscoverer axelixVersionDiscoverer() {
        return () -> "1.1.3";
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Details endpoint test fixtures.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    @Primary
    public BuildProperties buildProperties() {
        Properties props = new Properties();
        props.setProperty("group", "com.axelixlabs.axelix");
        props.setProperty("artifact", "axelix-sbs");
        props.setProperty("version", "1.0.0-SNAPSHOT");
        props.setProperty("name", "test-application");
        props.setProperty("time", "2025-10-30T09:10:13.428Z");

        return new BuildProperties(props);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Sanitization function shared by the env and configprops endpoint tests.
    //
    // Both endpoint auto-configurations (env and configprops) are excluded from {@link
    // com.axelixlabs.axelix.sbs.spring.core.Main} since they register a custom
    // {@code RequiredAuthorityCheckService} that bypasses authentication. We therefore register a
    // single {@code SmartSanitizingFunction} covering the union of sanitized property names from
    // both tests.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    @Primary
    public SmartSanitizingFunction smartSanitizingFunction(PropertyNameNormalizer propertyNameNormalizer) {
        return new SmartSanitizingFunction(
                List.of(
                        "axelix.env.test.toBeSanitized",
                        "AXELIX_FOR_SANITIZATION",
                        "axelix.prop.test.tags.forSanitization",
                        "axelix.prop.test.tags.FOR_SANITIZATION"),
                propertyNameNormalizer);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Env / configprops endpoint beans (their auto-configurations are excluded).
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    @ConfigurationProperties(prefix = "axelix.sbs.endpoints.config")
    public EndpointsConfigurationProperties endpointsConfigurationProperties() {
        return new EndpointsConfigurationProperties();
    }

    @Bean
    public AxelixEnvironmentEndpoint axelixEnvironmentEndpoint(EnvironmentService environmentService) {
        return new AxelixEnvironmentEndpoint(environmentService);
    }

    @Bean
    public AxelixConfigurationPropertiesEndpoint axelixConfigurationPropertiesEndpoint(
            ConfigurationPropertiesService configurationPropertiesService) {
        return new AxelixConfigurationPropertiesEndpoint(configurationPropertiesService);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Transactions endpoint test fixture - the {@code PropagationTestHelper} drives the transaction monitoring
    // assertions. {@link com.axelixlabs.axelix.sbs.spring.core.transactions.Owner}/{@code Pet}/{@code OwnerRepository}
    // are top-level classes that are picked up automatically via {@code @SpringBootApplication} component scan.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public PropagationTestHelper propagationTestHelper(OwnerRepository ownerRepository) {
        return new PropagationTestHelper(ownerRepository);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ValueInjectionTrackerBeanPostProcessor test fixtures.
    //
    // The bean instances are constructed with placeholder values so Spring does not need to resolve the constructor
    // {@code @Value} placeholders. The {@code @Value}/{@link com.axelixlabs.axelix.sbs.spring.core.env.TimeoutValue}
    // annotations remain on the class itself, so the {@code ValueInjectionTrackerBeanPostProcessor} still discovers
    // every constructor / field / method / parameter injection point declared on the bean class.
    // The {@code @Autowired} setters on {@code TestBeanWithCustomAnnotations} still run, so {@code test.spring.profiles
    // .active} must be present in the environment - see {@link AbstractEndpointTest#registerDynamicProperties}.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public TestBeanWithCustomAnnotations testBeanWithCustomAnnotations() {
        return new TestBeanWithCustomAnnotations("TestApp", "5000");
    }

    @Bean
    public TestBeanWithSpEL testBeanWithSpEL() {
        return new TestBeanWithSpEL();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Scheduled tasks endpoint test fixtures.
    //
    // The scheduled task auto-configuration registers all the management beans for us, but it requires:
    //   - a {@link TaskScheduler} bean,
    //   - {@code @Scheduled} methods to inspect,
    //   - a {@link SchedulingConfigurer} that contributes a non-{@code @Scheduled} (i.e. "custom") task.
    // -----------------------------------------------------------------------------------------------------------------

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Scheduled(cron = "*/1 * * * * *")
    public void testCronTask() {
        cronFlag = true;
    }

    @Scheduled(cron = "*/2 * * * * *")
    public void testCronTaskForModify() {
        // intentionally empty
    }

    @Scheduled(fixedDelay = 100)
    public void testFixedDelayTask() {
        fixedDelayFlag = true;
    }

    @Scheduled(fixedDelay = 20000000)
    public void testFixedDelayTaskForModify() {
        // intentionally empty
    }

    @Scheduled(fixedDelay = 2000000000)
    public void testFixedDelayTaskForExecute() {
        fixedDelayFlag = true;
    }

    @Scheduled(fixedRate = 100, initialDelay = 50)
    public void testFixedRateTask() {
        fixedRateFlag = true;
    }

    @Scheduled(fixedRate = 20000000)
    public void testFixedRateTaskForModify() {
        // intentionally empty
    }

    @Scheduled(fixedRate = 2000000000)
    public void testFixedRateTaskForExecute() {
        fixedRateFlag = true;
    }

    @Override
    public void configureTasks(@NonNull ScheduledTaskRegistrar registrar) {
        registrar.addTriggerTask(new CustomTestTask(), new CustomTestTrigger());
    }

    public static class CustomTestTask implements Runnable {
        @Override
        public void run() {
            customTaskFlag = true;
        }

        @Override
        public String toString() {
            return CustomTestTask.class.getName();
        }
    }

    public static class CustomTestTrigger implements Trigger {
        @Override
        public Date nextExecutionTime(@NonNull TriggerContext triggerContext) {
            return Date.from(Instant.now().plusMillis(100));
        }

        @Override
        public String toString() {
            return CUSTOM_TRIGGER_NAME;
        }
    }
}
