package com.nucleonforge.axile.master.api.app;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nucleonforge.axile.master.api.app.response.ApplicationGridResponse;

/**
 *
 * @since 19.07.2025
 * @author Mikhail Polivakha
 */
@RequestMapping(path = "/applications/v1")
public class ApplicationsApi {

    @GetMapping(path = "/grid")
    public ApplicationGridResponse getApplicationsGrid() {
        throw new UnsupportedOperationException();
    }

    @GetMapping(path = "/single/{name}")
    public ApplicationGridResponse getApplication(String applicationName) {
        throw new UnsupportedOperationException();
    }
}
