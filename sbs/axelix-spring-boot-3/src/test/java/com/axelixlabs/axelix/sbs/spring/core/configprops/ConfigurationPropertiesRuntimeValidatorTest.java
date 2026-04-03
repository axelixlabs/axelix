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

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for {@link ConfigurationPropertiesRuntimeValidator}.
 *
 * @author Nikita Kirillov
 */
@SpringBootTest
@Import(ConfigurationPropertiesRuntimeValidatorTest.CurrentConfig.class)
class ConfigurationPropertiesRuntimeValidatorTest {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    private BasicTypesProperties basicTypesProperties;

    private ConfigurationPropertiesRuntimeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ConfigurationPropertiesRuntimeValidator();
    }

    @Nested
    @DisplayName("Basic Type Validations")
    class BasicTypeValidations {

        private ConfigurationPropertiesBean bean;

        @BeforeEach
        void setUp() {
            bean = ConfigurationPropertiesBean.get(applicationContext, basicTypesProperties, "basicTypesProperties");
        }

        @ParameterizedTest
        @CsvSource({"basic.integerValue, 42", "basic.integerValue, 0", "basic.integerValue, -10"})
        void shouldValidateIntegerValues(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @CsvSource({"basic.integerValue, not-a-number", "basic.integerValue, 12.34", "basic.integerValue, true"})
        void shouldRejectInvalidIntegerValues(String propertyName, String value) {
            assertThatThrownBy(() -> validator.validate(bean, propertyName, value))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"true", "false", "TRUE", "FALSE", "yes", "no", "on", "off"})
        void shouldValidateBooleanValues(String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, "basic.booleanValue", value));
        }

        @ParameterizedTest
        @ValueSource(strings = {"not-a-boolean", "maybe", "123"})
        void shouldRejectInvalidBooleanValues(String value) {
            assertThatThrownBy(() -> validator.validate(bean, "basic.booleanValue", value))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @ParameterizedTest
        @CsvSource({
            "basic.doubleValue, 3.14",
            "basic.doubleValue, -2.5",
            "basic.doubleValue, 0.0",
            "basic.doubleValue, 1e-10"
        })
        void shouldValidateDoubleValues(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @CsvSource({"basic.doubleValue, abc", "basic.doubleValue, 1.2.3"})
        void shouldRejectInvalidDoubleValues(String propertyName, String value) {
            assertThatThrownBy(() -> validator.validate(bean, propertyName, value))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "any-string", "123", "true", "special chars !@#$"})
        void shouldValidateStringValues(String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, "basic.stringValue", value));
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        private ConfigurationPropertiesBean bean;

        @BeforeEach
        void setUp() {
            bean = ConfigurationPropertiesBean.getAll(applicationContext).get("namingProperties");
        }

        @ParameterizedTest
        @CsvSource({"naming.maxRetries, 5", "naming.defaultTimeout, 30", "naming.enableCache, true"})
        void shouldHandleCamelCase(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @CsvSource({"naming.max-retries, 5", "naming.default-timeout, 30", "naming.enable-cache, true"})
        void shouldHandleKebabCase(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @CsvSource({"naming.max_retries, 5", "naming.default_timeout, 30", "naming.enable_cache, true"})
        void shouldHandleSnakeCase(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @CsvSource({"NAMING.MAX_RETRIES, 5", "NAMING.DEFAULT_TIMEOUT, 30", "NAMING.ENABLE_CACHE, true"})
        void shouldHandleUpperCase(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }
    }

    @Nested
    @DisplayName("Collection Types")
    class CollectionTypes {

        private ConfigurationPropertiesBean bean;

        @BeforeEach
        void setUp() {
            bean = ConfigurationPropertiesBean.getAll(applicationContext).get("collectionProperties");
        }

        @Test
        void shouldValidateListOfStrings() {
            assertThatNoException()
                    .isThrownBy(() -> validator.validate(bean, "collection.string-list", "item1,item2,item3"));
        }

        @Test
        void shouldValidateListOfIntegers() {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, "collection.integer-list", "1,2,3,4,5"));
        }

        @Test
        void shouldRejectListWithInvalidInteger() {
            assertThatThrownBy(() -> validator.validate(bean, "collection.integer-list", "1,not-a-number,3"))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @Test
        void shouldValidateMapEntries() {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, "collection.simple-map.key1", "value1"));

            assertThatNoException().isThrownBy(() -> validator.validate(bean, "collection.simple-map.key2", "value2"));
        }

        @Test
        void shouldValidateStringMapEntryValue() {
            assertThatNoException()
                    .isThrownBy(() -> validator.validate(
                            bean, "collection.simple-map.key1", "any-string-is-fine-for-string-map"));
        }
    }

    @Nested
    @DisplayName("Special Types")
    class SpecialTypes {

        private ConfigurationPropertiesBean bean;

        @BeforeEach
        void setUp() {
            bean = ConfigurationPropertiesBean.getAll(applicationContext).get("specialTypesProperties");
        }

        @ParameterizedTest
        @DisplayName("Should validate Duration format")
        @CsvSource({"special.duration, PT30S", "special.duration, 5m", "special.duration, 2h", "special.duration, 1d"})
        void shouldValidateDuration(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid Duration")
        @ValueSource(strings = {"invalid", "10x", "forever"})
        void shouldRejectInvalidDuration(String value) {
            assertThatThrownBy(() -> validator.validate(bean, "special.duration", value))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @ParameterizedTest
        @DisplayName("Should validate LocalDate format")
        @CsvSource({"special.local-date, 2024-01-15", "special.local-date, 2023-12-31"})
        void shouldValidateLocalDate(String propertyName, String value) {
            assertThatNoException().isThrownBy(() -> validator.validate(bean, propertyName, value));
        }

        @ParameterizedTest
        @DisplayName("Should reject invalid LocalDate")
        @ValueSource(strings = {"01/15/2024", "2024-13-01", "invalid"})
        void shouldRejectInvalidLocalDate(String value) {
            assertThatThrownBy(() -> validator.validate(bean, "special.local-date", value))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Scenarios")
    class EdgeCases {

        @Test
        void shouldThrowExceptionWhenPropertyNotFound() {
            ConfigurationPropertiesBean bean =
                    ConfigurationPropertiesBean.getAll(applicationContext).get("basicTypesProperties");

            assertThatThrownBy(() -> validator.validate(bean, "basic.non-existent-property", "value"))
                    .isInstanceOf(ConfigurationPropertyValidationException.class);
        }

        @Test
        void shouldHandleEmptyString() {
            ConfigurationPropertiesBean ownerBean =
                    ConfigurationPropertiesBean.getAll(applicationContext).get("basicTypesProperties");

            assertThatNoException().isThrownBy(() -> validator.validate(ownerBean, "basic.stringValue", ""));
        }

        @Test
        void shouldHandleWhitespace() {
            ConfigurationPropertiesBean ownerBean =
                    ConfigurationPropertiesBean.getAll(applicationContext).get("basicTypesProperties");

            assertThatNoException().isThrownBy(() -> validator.validate(ownerBean, "basic.stringValue", "   trim   "));
        }
    }

    @ConfigurationProperties(prefix = "basic")
    public static class BasicTypesProperties {
        private int integerValue;
        private boolean booleanValue;
        private double doubleValue;
        private String stringValue;

        public int getIntegerValue() {
            return integerValue;
        }

        public void setIntegerValue(int integerValue) {
            this.integerValue = integerValue;
        }

        public boolean isBooleanValue() {
            return booleanValue;
        }

        public void setBooleanValue(boolean booleanValue) {
            this.booleanValue = booleanValue;
        }

        public double getDoubleValue() {
            return doubleValue;
        }

        public void setDoubleValue(double doubleValue) {
            this.doubleValue = doubleValue;
        }

        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }
    }

    @ConfigurationProperties(prefix = "naming")
    public static class NamingProperties {
        private int maxRetries;
        private int defaultTimeout;
        private boolean enableCache;

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getDefaultTimeout() {
            return defaultTimeout;
        }

        public void setDefaultTimeout(int defaultTimeout) {
            this.defaultTimeout = defaultTimeout;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public void setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
        }
    }

    @ConfigurationProperties(prefix = "collection")
    public static class CollectionProperties {
        private List<String> stringList;
        private List<Integer> integerList;
        private Map<String, String> simpleMap;

        public List<String> getStringList() {
            return stringList;
        }

        public void setStringList(List<String> stringList) {
            this.stringList = stringList;
        }

        public List<Integer> getIntegerList() {
            return integerList;
        }

        public void setIntegerList(List<Integer> integerList) {
            this.integerList = integerList;
        }

        public Map<String, String> getSimpleMap() {
            return simpleMap;
        }

        public void setSimpleMap(Map<String, String> simpleMap) {
            this.simpleMap = simpleMap;
        }
    }

    @ConfigurationProperties(prefix = "special")
    public static class SpecialTypesProperties {
        private Duration duration;
        private LocalDate localDate;

        public Duration getDuration() {
            return duration;
        }

        public void setDuration(Duration duration) {
            this.duration = duration;
        }

        public LocalDate getLocalDate() {
            return localDate;
        }

        public void setLocalDate(LocalDate localDate) {
            this.localDate = localDate;
        }
    }

    @Configuration
    @EnableConfigurationProperties({
        BasicTypesProperties.class,
        NamingProperties.class,
        CollectionProperties.class,
        SpecialTypesProperties.class
    })
    static class CurrentConfig {

        @Bean
        public BasicTypesProperties basicTypesProperties() {
            return new BasicTypesProperties();
        }

        @Bean
        public NamingProperties namingProperties() {
            return new NamingProperties();
        }

        @Bean
        public CollectionProperties collectionProperties() {
            return new CollectionProperties();
        }

        @Bean
        public SpecialTypesProperties specialTypesProperties() {
            return new SpecialTypesProperties();
        }
    }
}
