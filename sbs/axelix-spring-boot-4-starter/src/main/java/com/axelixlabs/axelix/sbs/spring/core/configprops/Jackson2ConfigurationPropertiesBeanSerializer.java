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
package com.axelixlabs.axelix.sbs.spring.core.configprops;

/*
 * Copyright 2012-present the original author or authors.
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.BeanSerializerFactory;
import com.fasterxml.jackson.databind.ser.BeanSerializerModifier;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jspecify.annotations.Nullable;

import org.springframework.boot.context.properties.bind.BindConstructorProvider;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.unit.DataSize;

/**
 * Similar to Spring Boot {@code Jackson2BeanSerializer}, but uses {@link Include#ALWAYS}
 * instead of {@link Include#NON_NULL} to retain null values.
 *
 * <p>Original author: Phillip Webb.</p>
 *
 * @author Nikita Kirillov
 */
final class Jackson2ConfigurationPropertiesBeanSerializer implements ConfigurationPropertiesBeanSerializer {

    private static final String CONFIGURATION_PROPERTIES_FILTER_ID = "configurationPropertiesFilter";

    private final ObjectMapper mapper;

    Jackson2ConfigurationPropertiesBeanSerializer() {
        JsonMapper.Builder builder = JsonMapper.builder();
        configureMapper(builder);
        this.mapper = builder.build();
    }

    private void configureMapper(JsonMapper.Builder builder) {
        builder.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        builder.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        builder.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);
        builder.configure(MapperFeature.USE_STD_BEAN_NAMING, true);
        builder.serializationInclusion(Include.ALWAYS);
        applyConfigurationPropertiesFilter(builder);
        applySerializationModifier(builder);
        if (ClassUtils.isPresent(
                "com.fasterxml.jackson.datatype.jsr310.JavaTimeModule",
                builder.getClass().getClassLoader())) {
            builder.addModule(new JavaTimeModule());
        }
        builder.addModule(new ConfigurationPropertiesModule());
    }

    private void applyConfigurationPropertiesFilter(JsonMapper.Builder builder) {
        builder.annotationIntrospector(new ConfigurationPropertiesAnnotationIntrospector());
        ConfigurationPropertiesPropertyFilter filter = new ConfigurationPropertiesPropertyFilter();
        builder.filterProvider(new SimpleFilterProvider().setDefaultFilter(filter));
    }

    private void applySerializationModifier(JsonMapper.Builder builder) {
        SerializerFactory factory =
                BeanSerializerFactory.instance.withSerializerModifier(new GenericSerializerModifier());
        builder.serializerFactory(factory);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, @Nullable Object> serialize(@Nullable Object bean) {
        return this.mapper.convertValue(bean, Map.class);
    }

    private static final class ConfigurationPropertiesAnnotationIntrospector extends JacksonAnnotationIntrospector {

        @Override
        public Object findFilterId(Annotated a) {
            Object id = super.findFilterId(a);
            return (id != null) ? id : CONFIGURATION_PROPERTIES_FILTER_ID;
        }
    }

    private static final class ConfigurationPropertiesPropertyFilter extends SimpleBeanPropertyFilter {

        private static final Log logger = LogFactory.getLog(ConfigurationPropertiesPropertyFilter.class);

        @Override
        protected boolean include(BeanPropertyWriter writer) {
            return include(writer.getFullName().getSimpleName());
        }

        @Override
        protected boolean include(PropertyWriter writer) {
            return include(writer.getFullName().getSimpleName());
        }

        private boolean include(String name) {
            return !name.startsWith("$$");
        }

        @Override
        public void serializeAsField(
                Object pojo, JsonGenerator jgen, SerializerProvider provider, PropertyWriter writer) throws Exception {
            if (writer instanceof BeanPropertyWriter beanPropertyWriter) {
                try {
                    if (pojo == beanPropertyWriter.get(pojo)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Skipping '" + writer.getFullName() + "' on '"
                                    + pojo.getClass().getName() + "' as it is self-referential");
                        }
                        return;
                    }
                } catch (Exception ex) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                                "Skipping '" + writer.getFullName() + "' on '"
                                        + pojo.getClass().getName()
                                        + "' as an exception was thrown when retrieving its value",
                                ex);
                    }
                    return;
                }
            }
            super.serializeAsField(pojo, jgen, provider, writer);
        }
    }

    private static final class ConfigurationPropertiesModule extends SimpleModule {

        private ConfigurationPropertiesModule() {
            addSerializer(DataSize.class, ToStringSerializer.instance);
        }
    }

    protected static class GenericSerializerModifier extends BeanSerializerModifier {

        private static final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

        @Override
        public List<BeanPropertyWriter> changeProperties(
                SerializationConfig config, BeanDescription beanDesc, List<BeanPropertyWriter> beanProperties) {
            List<BeanPropertyWriter> result = new ArrayList<>();
            Class<?> beanClass = beanDesc.getType().getRawClass();
            Bindable<?> bindable = Bindable.of(ClassUtils.getUserClass(beanClass));
            Constructor<?> bindConstructor = BindConstructorProvider.DEFAULT.getBindConstructor(bindable, false);
            for (BeanPropertyWriter writer : beanProperties) {
                if (isCandidate(beanDesc, writer, bindConstructor)) {
                    result.add(writer);
                }
            }
            return result;
        }

        private boolean isCandidate(
                BeanDescription beanDesc, BeanPropertyWriter writer, @Nullable Constructor<?> constructor) {
            if (constructor != null) {
                Parameter[] parameters = constructor.getParameters();
                @Nullable String @Nullable [] names = parameterNameDiscoverer.getParameterNames(constructor);
                if (names == null) {
                    names = new String[parameters.length];
                }
                for (int i = 0; i < parameters.length; i++) {
                    String name = MergedAnnotations.from(parameters[i])
                            .get(Name.class)
                            .getValue(MergedAnnotation.VALUE, String.class)
                            .orElse((names[i] != null) ? names[i] : parameters[i].getName());
                    if (name.equals(writer.getName())) {
                        return true;
                    }
                }
            }
            return isReadable(beanDesc, writer);
        }

        private boolean isReadable(BeanDescription beanDesc, BeanPropertyWriter writer) {
            Class<?> parentType = beanDesc.getType().getRawClass();
            Class<?> type = writer.getType().getRawClass();
            AnnotatedMethod setter = findSetter(beanDesc, writer);
            return (setter != null)
                    || ClassUtils.getPackageName(parentType).equals(ClassUtils.getPackageName(type))
                    || Map.class.isAssignableFrom(type)
                    || Collection.class.isAssignableFrom(type);
        }

        private @Nullable AnnotatedMethod findSetter(BeanDescription beanDesc, BeanPropertyWriter writer) {
            String name = "set" + determineAccessorSuffix(writer.getName());
            Class<?> type = writer.getType().getRawClass();
            AnnotatedMethod setter = beanDesc.findMethod(name, new Class<?>[] {type});
            if (setter == null && type.equals(Boolean.TYPE)) {
                setter = beanDesc.findMethod(name, new Class<?>[] {Boolean.class});
            }
            return setter;
        }

        private String determineAccessorSuffix(String propertyName) {
            if (propertyName.length() > 1 && Character.isUpperCase(propertyName.charAt(1))) {
                return propertyName;
            }
            return StringUtils.capitalize(propertyName);
        }
    }
}
