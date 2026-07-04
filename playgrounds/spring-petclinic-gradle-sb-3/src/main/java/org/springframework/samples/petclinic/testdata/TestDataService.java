package org.springframework.samples.petclinic.testdata;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile({"default", "local"})
public class TestDataService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;

    public TestDataService(
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository) {
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_1() {
        List<Company> companies = companyRepository.findAll();
        companies.forEach(c -> c.getDepartments().size());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_2() {
        List<Department> departments = departmentRepository.findAll();
        departments.forEach(d -> d.getEmployees().size());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_3() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> e.getDocuments().size());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_4() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> e.getProjects().size());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_5() {
        List<Department> departments = departmentRepository.findAll();
        departments.forEach(d -> d.getCompany().getName());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_6() {
        List<Employee> employees = employeeRepository.findAll();
        employees.forEach(e -> e.getDepartment().getName());
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runNplusOne_7() {
        employeeRepository.findAll();
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_1(PageRequest page) {
        employeeRepository.findAllWithDocumentsPaged(page);
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_2(PageRequest page) {
        employeeRepository.findAllWithProjectsPaged(page);
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_3(PageRequest page) {
        departmentRepository.findAllWithEmployeesPaged(page);
        sleepRandom();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void runPagination_4(PageRequest page) {
        companyRepository.findAllWithDepartmentsPaged(page);
        sleepRandom();
    }

    private void sleepRandom() {
        try {
            long delay = ThreadLocalRandom.current().nextLong(10, 50 + 1);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
