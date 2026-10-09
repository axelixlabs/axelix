package com.sivalabs.ft.features.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A transaction declared on an abstract method of an abstract class.
 */
public abstract class AbstractBookTestDataService {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public abstract void txOnAbstractMethod_runNplusOne_4();
}
