package org.springframework.samples.petclinic.testdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    @Query("SELECT d FROM Department d JOIN FETCH d.employees")
    Page<Department> findAllWithEmployeesPaged(Pageable pageable);
}
