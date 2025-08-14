package com.nucleonforge.axile.master.api.response;

/**
 * Short profile of a given bean.
 *
 * @author Mikhail Polivakha
 */
public class BeanShortProfile {

    /**
     * The name of the bean.
     */
    private String beanName;

    /**
     * The fully qualified class named of the bean
     */
    private String className;

    /**
     * The short representation of the location from where the bean was loaded.
     * Might be the jar in the classpath, or might be the directory on the disk etc.
     */
    private String loadedFrom;
}
