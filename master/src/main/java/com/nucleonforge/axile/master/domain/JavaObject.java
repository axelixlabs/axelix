package com.nucleonforge.axile.master.domain;

import java.util.List;

/**
 * The object inside Java heap.
 *
 * @author Mikhail Polivakha
 */
public class JavaObject {

    /**
     * The class of the object.
     */
    private LoadedClass clazz;

    private List<SpringBean> dependencies;
}
