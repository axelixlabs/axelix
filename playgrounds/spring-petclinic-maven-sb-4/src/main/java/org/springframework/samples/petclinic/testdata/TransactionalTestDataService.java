package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the class itself.
 */
@Service
@Profile({ "default", "local" })
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class TransactionalTestDataService {

	private final EmployeeRepository employeeRepository;

	public TransactionalTestDataService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public void txOnClass_runPagination_1(PageRequest page) {
		employeeRepository.findAllWithDocumentsPaged(page);
		TestDataDelays.sleepRandom();
	}

}
