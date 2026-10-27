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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.EndpointExtension;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.WebEndpointResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.metaAnnotatedWith;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

/**
 * Verifies that Axelix endpoint operations return non-generic payload types, since the isolated
 * Axelix Jackson converters recognize Axelix payloads by their top-level class only.
 *
 * @author Nikita Kirillov
 */
class EndpointReturnTypeArchitectureTest {

    private static final JavaClasses STARTER_CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.axelixlabs.axelix.sbs.spring");

    private static final Set<Class<?>> RESPONSE_WRAPPERS = Set.of(ResponseEntity.class, WebEndpointResponse.class);

    @Test
    void endpointOperationsShouldNotReturnGenericPayloads() {
        methods()
                .that()
                .areDeclaredInClassesThat(
                        metaAnnotatedWith(Endpoint.class).or(metaAnnotatedWith(EndpointExtension.class)))
                .and(metaAnnotatedWith(RequestMapping.class)
                        .or(metaAnnotatedWith(ReadOperation.class))
                        .or(metaAnnotatedWith(WriteOperation.class))
                        .or(metaAnnotatedWith(DeleteOperation.class)))
                .should(returnNonGenericPayload())
                .because(
                        "generic and array payloads (e.g. List<Dto>, Dto[]) bypass the isolated Axelix Jackson converters "
                                + "and get serialized with the host application's Jackson configuration")
                .allowEmptyShould(false)
                .check(STARTER_CLASSES);
    }

    private static ArchCondition<JavaMethod> returnNonGenericPayload() {
        return new ArchCondition<>("return a non-generic, non-array payload type") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                Type payload = method.reflect().getGenericReturnType();
                if (payload instanceof ParameterizedType
                        && RESPONSE_WRAPPERS.contains(((ParameterizedType) payload).getRawType())) {
                    payload = ((ParameterizedType) payload).getActualTypeArguments()[0];
                }
                if (!(payload instanceof Class) || isObjectArray((Class<?>) payload)) {
                    events.add(SimpleConditionEvent.violated(
                            method,
                            method.getFullName() + " returns generic or array payload " + payload.getTypeName()));
                }
            }
        };
    }

    private static boolean isObjectArray(Class<?> type) {
        return type.isArray() && !type.getComponentType().isPrimitive();
    }
}
