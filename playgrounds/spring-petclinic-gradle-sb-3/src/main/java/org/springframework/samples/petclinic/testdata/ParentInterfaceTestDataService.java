package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface ParentInterfaceTestDataService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void txOnParentInterface_runNplusOne_4();
}
