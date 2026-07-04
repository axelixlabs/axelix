package org.springframework.samples.petclinic.testdata.airport;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface AirportRepository extends JpaRepository<Airport, Long> {

	@Query("SELECT a FROM Airport a JOIN FETCH a.flights")
	Page<Airport> findAllWithFlightsPaged(Pageable pageable);
}
