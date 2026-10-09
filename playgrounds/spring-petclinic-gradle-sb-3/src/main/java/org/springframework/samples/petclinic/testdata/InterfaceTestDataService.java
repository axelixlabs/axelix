package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the methods of an interface: its own and an inherited one.
 */
public interface InterfaceTestDataService extends ParentInterfaceTestDataService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void txOnInterfaceMethod_runNplusOne_2();
}
