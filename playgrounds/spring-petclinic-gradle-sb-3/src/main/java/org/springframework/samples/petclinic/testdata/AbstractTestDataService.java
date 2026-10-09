package org.springframework.samples.petclinic.testdata;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * A transaction declared on an abstract method of an abstract class.
 */
public abstract class AbstractTestDataService {

    protected final DepartmentRepository departmentRepository;
    protected final EmployeeRepository employeeRepository;

    protected AbstractTestDataService(
            DepartmentRepository departmentRepository, EmployeeRepository employeeRepository) {
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public abstract void txOnAbstractMethod_runNplusOne_7();
}
