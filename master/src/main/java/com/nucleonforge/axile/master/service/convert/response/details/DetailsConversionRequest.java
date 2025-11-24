package com.nucleonforge.axile.master.service.convert.response.details;

import com.nucleonforge.axile.common.api.AxileDetails;
import com.nucleonforge.axile.master.model.instance.InstanceId;

/**
 * Request for details conversion. Required by {@link AxileDetailsConverter}.
 *
 * @author Mikhail Polivakha
 */
public record DetailsConversionRequest(AxileDetails axileDetails, InstanceId instanceId) {}
