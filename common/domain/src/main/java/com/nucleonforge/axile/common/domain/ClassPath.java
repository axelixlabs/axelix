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

import java.util.Iterator;
import java.util.Set;

/**
 * The class-path of the application.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
public class ClassPath implements Iterable<ClassPathEntry> {

    private final Set<ClassPathEntry> classPathEntries;

    public ClassPath(Set<ClassPathEntry> classPathEntries) {
        this.classPathEntries = classPathEntries;
    }

    public ClassPath addClassPathEntry(ClassPathEntry classPathEntry) {
        this.classPathEntries.add(classPathEntry);
        return this;
    }

    public Set<ClassPathEntry> getClassPathEntries() {
        return classPathEntries;
    }

    @Override
    public Iterator<ClassPathEntry> iterator() {
        return classPathEntries.iterator();
    }
}
