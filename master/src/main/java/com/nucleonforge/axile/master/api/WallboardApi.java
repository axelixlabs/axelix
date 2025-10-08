package com.nucleonforge.axile.master.api;

import java.util.Set;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.common.domain.Instance;
import com.nucleonforge.axile.master.api.response.InstancesGridResponse;
import com.nucleonforge.axile.master.model.instance.Instance;
import com.nucleonforge.axile.master.service.convert.InstancesToShortProfileConverter;
import com.nucleonforge.axile.master.service.state.InstanceRegistry;

/**
 * The API for managing applications.
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
@RestController
@RequestMapping(path = ApiPaths.InstancesApi.MAIN)
public class WallboardApi {

    private final InstanceRegistry instanceRegistry;
    private final InstancesToShortProfileConverter instancesToShortProfileConverter;

    public WallboardApi(
            InstanceRegistry instanceRegistry, InstancesToShortProfileConverter instancesToShortProfileConverter) {
        this.instanceRegistry = instanceRegistry;
        this.instancesToShortProfileConverter = instancesToShortProfileConverter;
    }

    @GetMapping(path = ApiPaths.InstancesApi.GRID)
    @SuppressWarnings("NullAway")
    public InstancesGridResponse getInstancesGrid() {
        Set<Instance> all = instanceRegistry.getAll();
        return new InstancesGridResponse(instancesToShortProfileConverter.convertAll(all));
    }
}
