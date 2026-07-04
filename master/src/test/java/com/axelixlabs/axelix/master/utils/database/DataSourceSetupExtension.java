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
package com.axelixlabs.axelix.master.utils.database;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.extension.AfterClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.BeforeClassTemplateInvocationCallback;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContext;
import org.junit.jupiter.api.extension.ClassTemplateInvocationContextProvider;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

import org.springframework.test.annotation.DirtiesContext.HierarchyMode;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.Assert;

import com.axelixlabs.axelix.master.domain.database.CommunityRDBMS;

/**
 * The specific context provider for {@link org.junit.jupiter.api.ClassTemplate} annotations, that is used
 * to wrap every class marked with {@link DatabaseMatrixTest}.
 * <p>
 * This extension know what databases Axelix Master supports, and it boots the appropriate {@link JdbcDatabaseContainer}
 * for every {@link org.junit.jupiter.api.ClassTemplate} invocation. This context provider is also responsible for
 * making sure the test container is torn down, and that Spring's {@link org.springframework.test.context.TestContext} is
 * aware of the booted container and uses it.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("rawtypes")
public class DataSourceSetupExtension implements ClassTemplateInvocationContextProvider {

    private static final Map<CommunityRDBMS, ContainerLifecycle> CONTAINERS = new HashMap<>();

    static {
        CONTAINERS.put(CommunityRDBMS.POSTGRES, new ContainerLifecycle(() -> new PostgreSQLContainer("postgres:18")));
        CONTAINERS.put(CommunityRDBMS.MYSQL, new ContainerLifecycle(() -> new MySQLContainer("mysql:8.4")));
    }

    @Override
    public boolean supportsClassTemplate(ExtensionContext context) {
        return true;
    }

    @Override
    public Stream<OverridingDataSourceInvocationContext> provideClassTemplateInvocationContexts(
            ExtensionContext context) {

        return Arrays.stream(CommunityRDBMS.values()).map(OverridingDataSourceInvocationContext::new);
    }

    public record OverridingDataSourceInvocationContext(CommunityRDBMS communityRDBMS)
            implements ClassTemplateInvocationContext {

        @Override
        public String getDisplayName(int invocationIndex) {
            return communityRDBMS.name();
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            return List.of(
                    new BootstrapContainerExtension(communityRDBMS), new TearDownContainerExtension(communityRDBMS));
        }
    }

    record BootstrapContainerExtension(CommunityRDBMS communityRDBMS) implements BeforeClassTemplateInvocationCallback {

        @Override
        public void beforeClassTemplateInvocation(ExtensionContext context) throws Exception {
            CONTAINERS.computeIfPresent(communityRDBMS, (_, lifecycle) -> lifecycle.boot());
        }
    }

    record TearDownContainerExtension(CommunityRDBMS communityRDBMS) implements AfterClassTemplateInvocationCallback {

        @Override
        public void afterClassTemplateInvocation(ExtensionContext context) throws Exception {
            CONTAINERS.computeIfPresent(communityRDBMS, (_, lifecycle) -> lifecycle.tearDown());
            evictSpringContext(context);
        }
    }

    /**
     * Evicts the currently cached Spring {@link org.springframework.context.ApplicationContext} for this test class.
     * <p>
     * This is the crux of the database matrix mechanism. {@link SpringExtension} boots and caches a single
     * {@link org.springframework.test.context.TestContext} for the whole {@link org.junit.jupiter.api.ClassTemplate}
     * (its {@link org.junit.jupiter.api.extension.BeforeAllCallback}/{@link org.junit.jupiter.api.extension.AfterAllCallback}
     * fire only once, wrapping
     * <em>all</em> invocations - not per invocation). Only {@link AfterClassTemplateInvocationCallback} runs per
     * invocation, so this is where we must evict the context: marking it dirty removes it from Spring's
     * context cache, forcing the next invocation to rebuild it against the new {@code spring.datasource.url} and
     * re-evaluate the {@code @ConditionalOnCommunityRdbms} conditions for the next database.
     * <p>
     * The {@link TestContextManager} is retrieved from the JUnit store using {@link SpringExtension}'s own storage
     * convention (namespace {@code SpringExtension.class}, keyed by the test class). This is an internal detail of
     * {@link SpringExtension}, but it is the only per-invocation, non per-method way to evict the context.
     */
    private static void evictSpringContext(ExtensionContext context) {
        TestContextManager testContextManager = context.getRoot()
                .getStore(ExtensionContext.Namespace.create(SpringExtension.class))
                .get(context.getRequiredTestClass(), TestContextManager.class);

        Assert.state(testContextManager != null, "TestContextManager must be present in the store at this point");

        testContextManager.getTestContext().markApplicationContextDirty(HierarchyMode.EXHAUSTIVE);
    }

    /**
     * Class that encapsulates the Lifecycle of the {@link JdbcDatabaseContainer}.
     *
     * @author Mikhail Polivakha
     */
    static class ContainerLifecycle {

        private final Supplier<JdbcDatabaseContainer> containerCreator;

        private @Nullable JdbcDatabaseContainer<?> bootedContainer;

        /**
         * @param containerCreator the {@link Supplier} for the container creator.
         */
        ContainerLifecycle(Supplier<JdbcDatabaseContainer> containerCreator) {
            this.containerCreator = containerCreator;
        }

        public ContainerLifecycle boot() {
            JdbcDatabaseContainer<?> container = containerCreator.get();

            container.start();

            System.setProperty("spring.datasource.url", container.getJdbcUrl());
            System.setProperty("spring.datasource.password", container.getPassword());
            System.setProperty("spring.datasource.username", container.getUsername());

            bootedContainer = container;

            return this;
        }

        public ContainerLifecycle tearDown() {
            Assert.state(bootedContainer != null, "The container must be booted at this point");

            System.clearProperty("spring.datasource.url");
            System.clearProperty("spring.datasource.password");
            System.clearProperty("spring.datasource.username");

            bootedContainer.stop();
            bootedContainer = null;

            return this;
        }
    }
}
