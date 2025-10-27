package com.nucleonforge.axile.master.service.state.export;

import org.springframework.stereotype.Component;

import com.nucleonforge.axile.master.api.BeansApi;
import com.nucleonforge.axile.master.api.response.BeansFeedResponse;

/**
 * Collects Spring Beans information for application state export.
 *
 * @see BeansApi
 * @since 27.10.2025
 * @author Nikita Kirillov
 */
@Component
public class BeansDataCollector implements StateDataCollector {

    private final BeansApi beansApi;

    public BeansDataCollector(BeansApi beansApi) {
        this.beansApi = beansApi;
    }

    @Override
    public String getName() {
        return "beans";
    }

    @Override
    public BeansFeedResponse collectData(String instanceId) {
        return beansApi.getBeansProfile(instanceId);
    }
}
