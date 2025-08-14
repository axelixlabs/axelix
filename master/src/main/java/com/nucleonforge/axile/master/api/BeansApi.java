package com.nucleonforge.axile.master.api;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.response.BeansFeedResponse;

/**
 * The API for managing beans.
 *
 * @author Mikhail Polivakha
 */
@RestController
@RequestMapping(path = ApiPaths.BeansApi.MAIN)
public class BeansApi {

    @RequestMapping(path = ApiPaths.BeansApi.FEED)
    public BeansFeedResponse getBeansProfile(@PathVariable("instanceId") String instanceId) {
        throw new UnsupportedOperationException();
    }
}
