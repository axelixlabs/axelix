package com.sivalabs.ft.features.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the methods of an interface: its own and an inherited one.
 */
public interface InterfaceBookTestDataService extends ParentInterfaceBookTestDataService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void txOnInterfaceMethod_runNplusOne_2();
}
