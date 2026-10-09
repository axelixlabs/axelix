package org.springframework.samples.petclinic.testdata;

import java.util.List;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on the methods of an abstract class: a concrete and an abstract
 * one.
 */
public abstract class AbstractTestDataService {

	protected final DepartmentRepository departmentRepository;

	protected final EmployeeRepository employeeRepository;

	protected AbstractTestDataService(DepartmentRepository departmentRepository,
			EmployeeRepository employeeRepository) {
		this.departmentRepository = departmentRepository;
		this.employeeRepository = employeeRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void txOnAbstractClassMethod_runNplusOne_6() {
		List<Employee> employees = employeeRepository.findAll();
		employees.forEach(e -> e.getDepartment().getName());
		TestDataDelays.sleepRandom();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public abstract void txOnAbstractMethod_runNplusOne_7();

}
