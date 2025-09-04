package com.nucleonforge.axile.master.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nucleonforge.axile.master.api.response.software.DistributionSummaryResponse;

/**
 * The API related to software components used in the application.
 *
 * @author Mikhail Polivakha
 */
@RestController
@RequestMapping(path = ApiPaths.SoftwareApi.MAIN)
public class SoftwareApi {

    @GetMapping(path = ApiPaths.SoftwareApi.CORE_SUMMARY)
    public DistributionSummaryResponse getCoreDistributionGrid() {
        throw new UnsupportedOperationException();
    }
}
