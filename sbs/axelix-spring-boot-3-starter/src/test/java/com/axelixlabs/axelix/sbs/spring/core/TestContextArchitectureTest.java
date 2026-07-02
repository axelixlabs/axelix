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
package com.axelixlabs.axelix.sbs.spring.core;

import java.lang.annotation.Annotation;
import java.util.List;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.data.cassandra.AutoConfigureDataCassandra;
import org.springframework.boot.test.autoconfigure.data.cassandra.DataCassandraTest;
import org.springframework.boot.test.autoconfigure.data.couchbase.AutoConfigureDataCouchbase;
import org.springframework.boot.test.autoconfigure.data.couchbase.DataCouchbaseTest;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.AutoConfigureDataElasticsearch;
import org.springframework.boot.test.autoconfigure.data.elasticsearch.DataElasticsearchTest;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.data.ldap.AutoConfigureDataLdap;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.data.neo4j.AutoConfigureDataNeo4j;
import org.springframework.boot.test.autoconfigure.data.neo4j.DataNeo4jTest;
import org.springframework.boot.test.autoconfigure.data.r2dbc.AutoConfigureDataR2dbc;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.autoconfigure.data.redis.AutoConfigureDataRedis;
import org.springframework.boot.test.autoconfigure.data.redis.DataRedisTest;
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureJdbc;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jooq.AutoConfigureJooq;
import org.springframework.boot.test.autoconfigure.jooq.JooqTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureMockRestServiceServer;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.webservices.client.WebServiceClientTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.web.WebAppConfiguration;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * Tests that check for context pollution in Spring Boot tests
 *
 * @author Artemiy Degtyarev
 */
public class TestContextArchitectureTest {

    /**
     * Annotations that influence the Spring test {@code ApplicationContext} — its
     * configuration, active profiles, property sources, web environment, or
     * autoconfiguration slices. Applying any of them to a test type tends to
     * create a new, distinct context cache key, which fragments the context cache
     * and slows the suite, or pollutes a shared context for other tests.
     *
     * <p>{@link TestContextArchitectureTest} verifies that these annotations are
     * not used in places where they would compromise context reuse or isolation.
     */
    public static final List<Class<? extends Annotation>> RESTRICTED_ANNOTATIONS = List.of(
            ContextConfiguration.class,
            ActiveProfiles.class,
            ContextHierarchy.class,
            TestPropertySource.class,
            WebAppConfiguration.class,
            SpringBootTest.class,
            Import.class,
            AutoConfigureMockMvc.class,
            WebMvcTest.class,
            AutoConfigureWebTestClient.class,
            DirtiesContext.class,
            AutoConfigureObservability.class,
            JsonTest.class,
            AutoConfigureJsonTesters.class,
            WebFluxTest.class,
            GraphQlTest.class,
            AutoConfigureGraphQlTester.class,
            DataCassandraTest.class,
            AutoConfigureDataCassandra.class,
            DataCouchbaseTest.class,
            AutoConfigureDataCouchbase.class,
            DataElasticsearchTest.class,
            AutoConfigureDataElasticsearch.class,
            DataJpaTest.class,
            AutoConfigureDataJpa.class,
            DataJdbcTest.class,
            AutoConfigureDataJdbc.class,
            JdbcTest.class,
            AutoConfigureJdbc.class,
            DataR2dbcTest.class,
            AutoConfigureDataR2dbc.class,
            JooqTest.class,
            AutoConfigureJooq.class,
            DataMongoTest.class,
            AutoConfigureDataMongo.class,
            DataNeo4jTest.class,
            AutoConfigureDataNeo4j.class,
            DataRedisTest.class,
            AutoConfigureDataRedis.class,
            DataLdapTest.class,
            AutoConfigureDataLdap.class,
            RestClientTest.class,
            AutoConfigureMockRestServiceServer.class,
            AutoConfigureRestDocs.class,
            WebServiceClientTest.class,
            ImportAutoConfiguration.class);

    public static final JavaClasses IMPORTED_CLASSES =
            new ClassFileImporter().importPackages("com.axelixlabs.axelix.sbs.spring.core");

    @Test
    void should_have_restricted_annotations_only_on_parent() {
        ClassesShouldConjunction rule = classes()
                .that()
                .areNotNestedClasses()
                .and()
                .areNotAnnotatedWith(TestConfiguration.class)
                .and()
                .areNotAnnotatedWith(SpringBootApplication.class)
                .and()
                .areMetaAnnotatedWith(areAnnotatedWithRestrictedAnnotations())
                .and()
                .areNotAnnotatedWith(IgnoreArchitectureTest.class)
                .should(beAbstract());

        rule.check(IMPORTED_CLASSES);
    }

    private DescribedPredicate<? super JavaAnnotation<?>> areAnnotatedWithRestrictedAnnotations() {
        return new DescribedPredicate<>("are annotated with restricted annotations") {
            @Override
            public boolean test(JavaAnnotation<?> javaAnnotation) {
                JavaClass rawType = javaAnnotation.getRawType();

                return RESTRICTED_ANNOTATIONS.stream().anyMatch(rawType::isEquivalentTo);
            }
        };
    }

    private ArchCondition<? super JavaClass> beAbstract() {
        return new ArchCondition<>("be abstract") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                boolean isAbstract = item.getModifiers().contains(JavaModifier.ABSTRACT);

                if (!isAbstract) {
                    String message = "%s should be parent test class or should be annotated with IgnoreArchitecutreTest annotation"
                        .formatted(item.getName());

                    events.add(new SimpleConditionEvent(item, false, message));
                }
            }
        };
    }
}
