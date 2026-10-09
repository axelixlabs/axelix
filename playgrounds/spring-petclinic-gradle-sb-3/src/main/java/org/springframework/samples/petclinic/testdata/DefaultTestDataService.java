package org.springframework.samples.petclinic.testdata;

import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transactions declared on methods: of the class itself, of its interface and of its abstract parent.
 */
@Service
@Profile({"default", "local"})
public class DefaultTestDataService extends AbstractTestDataService implements InterfaceTestDataService {

    private final CompanyRepository companyRepository;

    public DefaultTestDataService(
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository) {
        super(departmentRepository, employeeRepository);
        this.companyRepository = companyRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void txOnClassMethod_runNplusOne_1() {
        List<Company> companies = companyRepository.findAll();
        companies.forEach(c -> c.getDepartments().size());
        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnInterfaceMethod_runNplusOne_2() {
        List<Department> departments = departmentRepository.findAll();
        departments.forEach(d -> d.getEmployees().size());
        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnParentInterface_runNplusOne_4() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> e.getProjects().size());
        TestDataDelays.sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void txOnClassMethod_runNplusOne_5() {
        List<Department> departments = departmentRepository.findAll();
        departments.forEach(d -> d.getCompany().getName());
        TestDataDelays.sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void txOnClassMethod_runNplusOne_6() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> e.getDepartment().getName());
        TestDataDelays.sleepRandom();
    }

    @Override
    public void txOnAbstractMethod_runNplusOne_7() {
        employeeRepository.findAll();
        TestDataDelays.sleepRandom();
    }

    @RequiresNewTransactional
    public void txMetaAnnotation_runPagination_3(PageRequest page) {
        departmentRepository.findAllWithEmployeesPaged(page);
        TestDataDelays.sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void txOnClassMethod_runPagination_4(PageRequest page) {
        companyRepository.findAllWithDepartmentsPaged(page);
        TestDataDelays.sleepRandom();
    }
}
