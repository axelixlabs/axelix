package com.nucleonforge.axile.master.api.response.software;

import java.util.List;

/**
 * The summary of all software distributions used in hte ecosystem.
 *
 * @author Mikhail Polivakha
 */
public record DistributionSummaryResponse(List<DistributionResponse> distributions) {}
