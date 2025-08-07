package com.nucleonforge.axile.master.api.app.response;

import java.util.Set;

import com.nucleonforge.axile.master.domain.Application;

/**
 * Abstract application deployment
 *
 * @see Application
 * @author Mikhail Polivakha
 */
public class ApplicationResponse {

    /**
     * The name of the application, e.g. K8S deployment name
     */
    private String name;

    /**
     * Instances of the given application, e.g. K8S pods.
     */
    private Set<InstanceResponse> instances;
}
