package org.springframework.samples.petclinic.testdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    @Query("SELECT c FROM Company c JOIN FETCH c.departments")
    Page<Company> findAllWithDepartmentsPaged(Pageable pageable);
}
