package org.springframework.samples.petclinic.testdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("SELECT e FROM Employee e JOIN FETCH e.documents")
    Page<Employee> findAllWithDocumentsPaged(Pageable pageable);

    @Query("SELECT e FROM Employee e JOIN FETCH e.projects")
    Page<Employee> findAllWithProjectsPaged(Pageable pageable);
}
