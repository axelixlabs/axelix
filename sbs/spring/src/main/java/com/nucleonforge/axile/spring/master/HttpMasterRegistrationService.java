package com.nucleonforge.axile.spring.master;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * The {@link MasterRegistrationService} that performs registration via sending an HTTP request.
 *
 * @author Mikhail Polivakha
 */
// @Service
public class HttpMasterRegistrationService implements MasterRegistrationService {

    @Autowired
    private SelfRegistrationConfig selfRegistrationConfig;

    @Override
    public void registerSelf(SelfRegistrationRequest selfRegistrationRequest) {}
}
