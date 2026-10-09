package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the interface itself.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public interface TransactionalInterfaceTestDataService {

    void txOnInterface_runNplusOne_3();
}
