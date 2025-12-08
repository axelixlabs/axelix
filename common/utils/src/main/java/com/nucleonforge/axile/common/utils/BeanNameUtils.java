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
package com.nucleonforge.axile.common.utils;

/**
 * Utilities to work with bean names.
 *
 * @author Mikhail Polivakha
 */
public class BeanNameUtils {

    /**
     * Strips the configprops prefix from the bean name.
     * <p>
     * The problem is that the bean name of the configprops bean as returned by the actuator, for some reason, contains
     * the dash at the very beginning. I do not know why. We do not want to show it in the bean name.
     */
    public static String stripConfigPropsPrefix(String beanName) {
        int indexOfDash = beanName.indexOf("-");

        if (indexOfDash != -1 && indexOfDash < beanName.length() - 1) {
            return beanName.substring(indexOfDash + 1);
        } else {
            return beanName;
        }
    }
}
