package com.nucleonforge.axile.master.api.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nucleonforge.axile.master.api.ApiPaths;
import com.nucleonforge.axile.master.api.app.response.ApplicationGridResponse;

/**
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
@RequestMapping(path = ApiPaths.ApplicationApi.MAIN)
public class ApplicationsApi {

    @GetMapping(path = ApiPaths.ApplicationApi.GRID)
    public ApplicationGridResponse getApplicationsGrid() {
        throw new UnsupportedOperationException();
    }

    @GetMapping(path = ApiPaths.ApplicationApi.SINGLE)
    public ApplicationGridResponse getApplication(@PathVariable("name") String applicationName) {
        throw new UnsupportedOperationException();
    }
}
