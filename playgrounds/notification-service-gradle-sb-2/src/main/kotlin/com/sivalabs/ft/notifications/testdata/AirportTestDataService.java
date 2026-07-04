package org.springframework.samples.petclinic.testdata.airport;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AirportTestDataService {

	private final AirportRepository airportRepository;

	public AirportTestDataService(AirportRepository airportRepository) {
		this.airportRepository = airportRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_1() {
		List<Airport> airports = airportRepository.findAll();
		airports.forEach(a -> a.getFlights().size());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runPagination_1(PageRequest page) {
		airportRepository.findAllWithFlightsPaged(page);
	}
}
