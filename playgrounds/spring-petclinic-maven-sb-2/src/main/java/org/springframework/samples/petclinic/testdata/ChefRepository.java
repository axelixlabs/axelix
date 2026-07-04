package org.springframework.samples.petclinic.testdata;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChefRepository extends JpaRepository<Chef, Long> {

	@Query(value = "SELECT c FROM Chef c JOIN FETCH c.dishes", countQuery = "SELECT COUNT(c) FROM Chef c")

	Page<Chef> findAllWithDishesPaged(Pageable pageable);

}
