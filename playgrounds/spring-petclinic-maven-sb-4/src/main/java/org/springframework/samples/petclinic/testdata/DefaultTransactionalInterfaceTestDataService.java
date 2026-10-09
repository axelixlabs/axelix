package org.springframework.samples.petclinic.testdata;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile({ "default", "local" })
public class DefaultTransactionalInterfaceTestDataService implements TransactionalInterfaceTestDataService {

	private final EmployeeRepository employeeRepository;

	public DefaultTransactionalInterfaceTestDataService(EmployeeRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	public void txOnInterface_runNplusOne_3() {
		List<Employee> employees = employeeRepository.findAll();
		employees.forEach(e -> e.getDocuments().size());
		TestDataDelays.sleepRandom();
	}

}
