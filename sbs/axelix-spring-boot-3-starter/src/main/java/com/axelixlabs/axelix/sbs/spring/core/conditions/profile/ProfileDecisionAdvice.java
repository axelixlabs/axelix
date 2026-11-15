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
package com.axelixlabs.axelix.sbs.spring.core.conditions.profile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import net.bytebuddy.asm.Advice;

/**
 * Observes successful returns only: a throwing condition is not a negative match.
 * Reflection through public Spring interfaces keeps the agent independent of Spring's
 * application class loader (notably Boot's executable-JAR class loader).
 *
 * @author Axelix Labs
 */
public final class ProfileDecisionAdvice {

    private ProfileDecisionAdvice() {}

    @Advice.OnMethodExit(suppress = Throwable.class)
    public static void onExit(
            @Advice.Argument(0) Object context, @Advice.Argument(1) Object metadata, @Advice.Return boolean matched) {
        capture(context, metadata, matched);
    }

    /** Called from inlined advice, so it must be accessible to Spring's classes. */
    public static void capture(Object context, Object metadata, boolean matched) {
        try {
            ClassLoader loader = context.getClass().getClassLoader();
            Class<?> contextType =
                    Class.forName("org.springframework.context.annotation.ConditionContext", false, loader);
            Class<?> metadataType = Class.forName("org.springframework.core.type.AnnotatedTypeMetadata", false, loader);
            Class<?> classMetadataType = Class.forName("org.springframework.core.type.ClassMetadata", false, loader);
            Class<?> methodMetadataType = Class.forName("org.springframework.core.type.MethodMetadata", false, loader);
            Class<?> environmentType = Class.forName("org.springframework.core.env.Environment", false, loader);

            // Scanning may receive an ApplicationContext as registry while configuration parsing
            // receives its BeanFactory. Use the same owner for both paths.
            Object registry = contextType.getMethod("getBeanFactory").invoke(context);
            if (registry == null) {
                registry = contextType.getMethod("getRegistry").invoke(context);
            }
            Object environment = contextType.getMethod("getEnvironment").invoke(context);
            Map<?, ?> attributes = (Map<?, ?>) metadataType
                    .getMethod("getAllAnnotationAttributes", String.class)
                    .invoke(metadata, "org.springframework.context.annotation.Profile");
            List<String> expressions = new ArrayList<>();
            if (attributes != null && attributes.get("value") instanceof List<?> values) {
                for (Object value : values) {
                    expressions.addAll(Arrays.asList((String[]) value));
                }
            }
            boolean method = methodMetadataType.isInstance(metadata);
            String className = (String) (method ? methodMetadataType : classMetadataType)
                    .getMethod(method ? "getDeclaringClassName" : "getClassName")
                    .invoke(metadata);
            String methodName = method
                    ? (String) methodMetadataType.getMethod("getMethodName").invoke(metadata)
                    : "";
            ProfileDecisionRecorder.record(
                    registry,
                    new ProfileDecision(
                            className,
                            methodName,
                            expressions,
                            Arrays.asList((String[]) environmentType
                                    .getMethod("getActiveProfiles")
                                    .invoke(environment)),
                            Arrays.asList((String[]) environmentType
                                    .getMethod("getDefaultProfiles")
                                    .invoke(environment)),
                            matched));
        } catch (ReflectiveOperationException exception) {
            // Diagnostics must not alter Spring's result. Failed capture is visible rather than a fabricated decision.
            System.getLogger(ProfileDecisionAdvice.class.getName())
                    .log(System.Logger.Level.WARNING, "Could not record Spring profile decision", exception);
        }
    }
}
