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

import org.springframework.boot.context.properties.ConfigurationPropertiesBean;
import org.springframework.boot.context.properties.bind.BindException;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

import com.axelixlabs.axelix.sbs.spring.core.utils.MockEnvironment;

/**
 * Validates configuration property values without modifying the actual configuration bean.
 *
 * @author Nikita Kirillov
 */
public class ConfigurationPropertiesRuntimeValidator {

    /**
     * Validates a property value against a configuration bean.
     *
     * <p>Uses an isolated {@link MockEnvironment}.
     *
     * @param ownerBean the configuration properties bean (for metadata only, not modified)
     * @param propertyName the property name
     * @param newValue the value to validate
     * @throws ConfigurationPropertyValidationException if validation fails (property not found,
     *         type mismatch, or constraint violation)
     */
    public void validate(ConfigurationPropertiesBean ownerBean, String propertyName, String newValue) {

        MockEnvironment tempEnvironment = new MockEnvironment();
        tempEnvironment.addProperty(propertyName, newValue);

        Bindable<?> bindable = Bindable.of(ownerBean.asBindTarget().getType());

        try {
            Binder binder = Binder.get(tempEnvironment);
            BindResult<?> bindResult = binder.bind(ownerBean.getAnnotation().prefix(), bindable);

            // If binding failed (isBound() is false), we assume the property was not found.
            // If validation fails or there's a type mismatch, a BindException will be thrown.
            if (!bindResult.isBound()) {
                throw new ConfigurationPropertyValidationException(
                        String.format("Property '%s' does not exist in configuration", propertyName));
            }
        } catch (BindException e) {
            throw new ConfigurationPropertyValidationException(String.format(
                    "Validation failed for property '%s' with value '%s': %s", propertyName, newValue, e.getMessage()));
        }
    }
}
