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
package com.nucleonforge.axile.common.domain;

/**
 * The link to the class that is loaded by the application.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class LoadedClass {

    /**
     * The link to class loader that loaded that particular class.
     */
    private ClassLoader classLoader;

    /**
     * Fully qualified class name.
     */
    private String fqcn;

    /**
     * The class-path-entry from which the given class is loaded.
     */
    private ClassPathEntry classPathEntry;

    public LoadedClass(ClassLoader classLoader, String fqcn, ClassPathEntry classPathEntry) {
        this.classLoader = classLoader;
        this.fqcn = fqcn;
        this.classPathEntry = classPathEntry;
    }
}
