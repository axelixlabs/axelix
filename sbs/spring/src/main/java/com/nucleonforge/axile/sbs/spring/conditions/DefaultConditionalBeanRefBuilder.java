/*
 * Copyright 2025-present, Nucleon Forge Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.nucleonforge.axile.sbs.spring.conditions;

import java.lang.reflect.Proxy;

import org.jspecify.annotations.Nullable;

import org.springframework.util.ClassUtils;

/**
 * Default implementation {@link ConditionalBeanRefBuilder}.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public class DefaultConditionalBeanRefBuilder implements ConditionalBeanRefBuilder {

    public String buildBeanRefInternal(Class<?> beanClass, @Nullable String beanFactoryMethodName) {

        Class<?> userClass = resolveUserClass(beanClass);

        // By calling this method spring-boot replaces the nested class '$' with package separator - the dot.
        // https://github.com/spring-projects/spring-boot/blob/main/module/spring-boot-actuator-autoconfigure/src/main/java/org/springframework/boot/actuate/autoconfigure/condition/ConditionsReportEndpoint.java#L128
        var result = new StringBuilder(ClassUtils.getShortName(userClass));

        if (beanFactoryMethodName != null) {
            // We append the method via '#' sign in order to cope with the following behavior
            // https://github.com/spring-projects/spring-boot/blob/main/core/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/condition/SpringBootCondition.java#L79
            result.append("#").append(beanFactoryMethodName);
        }

        return result.toString();
    }

    // TODO:
    //  We have this logic  of determining the end user class spread
    //  across the project, especially in beans endpoint. Need to unify it.
    private static Class<?> resolveUserClass(Class<?> beanClass) {
        Class<?> userClass = ClassUtils.getUserClass(beanClass);

        if (userClass == beanClass && Proxy.isProxyClass(userClass)) {
            userClass = userClass.getInterfaces()[0];
        }
        return userClass;
    }
}
