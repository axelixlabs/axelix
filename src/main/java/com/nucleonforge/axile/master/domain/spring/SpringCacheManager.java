package com.nucleonforge.axile.master.domain.spring;

import java.util.Set;

import com.nucleonforge.axile.master.domain.LoadedClass;

public class SpringCacheManager {

    /**
     * The name of this Cache Manager
     */
    private String name;

    /**
     * Caches that this Spring's {@code CacheManager} manages
     */
    private Set<String> caches;

    /**
     * The information of the {@link java.lang.Class} from which the given CacheManager was created
     */
    private LoadedClass classInfo;
}
