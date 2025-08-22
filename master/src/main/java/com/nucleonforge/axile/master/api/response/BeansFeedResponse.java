package com.nucleonforge.axile.master.api.response;

import java.util.ArrayList;
import java.util.List;

/**
 * The feed of the beans used in the application.
 *
 * @author Mikhail Polivakha
 */
public class BeansFeedResponse {

    private final List<BeanShortProfile> beans;

    public BeansFeedResponse() {
        this.beans = new ArrayList<>();
    }

    public List<BeanShortProfile> getBeans() {
        return beans;
    }

    public BeansFeedResponse addBean(BeanShortProfile beanShortProfile) {
        this.beans.add(beanShortProfile);
        return this;
    }
}
