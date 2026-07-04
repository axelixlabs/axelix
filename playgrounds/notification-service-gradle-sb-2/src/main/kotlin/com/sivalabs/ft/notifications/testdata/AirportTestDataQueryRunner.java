package org.springframework.samples.petclinic.testdata.airport;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class AirportTestDataQueryRunner {

	private final AirportTestDataService airportTestDataService;

	public AirportTestDataQueryRunner(AirportTestDataService airportTestDataService) {
		this.airportTestDataService = airportTestDataService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runTests() {
		airportTestDataService.runNplusOne_1();

		PageRequest pageRequest = PageRequest.of(0, 2);
		airportTestDataService.runPagination_1(pageRequest);
	}
}
