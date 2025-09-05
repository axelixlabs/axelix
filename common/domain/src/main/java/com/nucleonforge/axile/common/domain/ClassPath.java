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
