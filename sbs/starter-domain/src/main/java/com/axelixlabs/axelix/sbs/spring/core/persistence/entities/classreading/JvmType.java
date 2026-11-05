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
package com.axelixlabs.axelix.sbs.spring.core.persistence.entities.classreading;

/**
 * Utilities for converting Java classes to JVM internal names and descriptors.
 *
 * @author Nikita Kirilloff
 */
public final class JvmType {

    private JvmType() {}

    /**
     * Returns the JVM internal name for the given class.
     *
     * @param clazz the class to convert
     * @return the class name in JVM internal form
     */
    public static String getInternalName(final Class<?> clazz) {
        return clazz.getName().replace('.', '/');
    }

    /**
     * Returns the JVM type descriptor for the given class.
     *
     * @param clazz the class to convert
     * @return the JVM type descriptor
     */
    public static String getDescriptor(final Class<?> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        appendDescriptor(clazz, stringBuilder);
        return stringBuilder.toString();
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    private static void appendDescriptor(final Class<?> clazz, final StringBuilder stringBuilder) {
        Class<?> currentClass;
        for (currentClass = clazz; currentClass.isArray(); currentClass = currentClass.getComponentType()) {
            stringBuilder.append('[');
        }

        if (currentClass.isPrimitive()) {
            char descriptor;
            if (currentClass == Integer.TYPE) {
                descriptor = 'I';
            } else if (currentClass == Void.TYPE) {
                descriptor = 'V';
            } else if (currentClass == Boolean.TYPE) {
                descriptor = 'Z';
            } else if (currentClass == Byte.TYPE) {
                descriptor = 'B';
            } else if (currentClass == Character.TYPE) {
                descriptor = 'C';
            } else if (currentClass == Short.TYPE) {
                descriptor = 'S';
            } else if (currentClass == Double.TYPE) {
                descriptor = 'D';
            } else if (currentClass == Float.TYPE) {
                descriptor = 'F';
            } else {
                if (currentClass != Long.TYPE) {
                    throw new AssertionError();
                }

                descriptor = 'J';
            }

            stringBuilder.append(descriptor);
        } else {
            stringBuilder.append('L').append(getInternalName(currentClass)).append(';');
        }
    }
}
