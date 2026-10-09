package com.sivalabs.ft.features.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public interface ParentInterfaceBookTestDataService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void txOnParentInterface_runNplusOne_3();
}
