package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@Profile({ "default", "local" })
public class DefaultTransactionalAbstractTestDataService extends TransactionalAbstractTestDataService {

	private final EmployeeRepository employeeRepository;

	public DefaultTransactionalAbstractTestDataService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	public void txOnAbstractClass_runPagination_2(PageRequest page) {
		employeeRepository.findAllWithProjectsPaged(page);
		TestDataDelays.sleepRandom();
	}

}
