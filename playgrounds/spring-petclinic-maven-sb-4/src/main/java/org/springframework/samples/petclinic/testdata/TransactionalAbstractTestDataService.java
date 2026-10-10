package org.springframework.samples.petclinic.testdata;

import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the abstract class itself.
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
public abstract class TransactionalAbstractTestDataService {

	public abstract void txOnAbstractClass_runPagination_2(PageRequest page);

}
