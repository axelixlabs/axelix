package com.nucleonforge.axile.spring.master;

/**
 * Service responsible for registering the current service in the master registry
 *
 * @author Mikhail Polivakha
 */
public interface MasterRegistrationService {

    /**
     * Register itself within master
     */
    void registerSelf(SelfRegistrationRequest selfRegistrationRequest);
}
