package org.springframework.samples.petclinic.testdata;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Profile({ "default", "local" })
public class RestaurantTestDataQueryRunner {

	private final RestaurantTestDataService restaurantTestDataService;

	public RestaurantTestDataQueryRunner(RestaurantTestDataService restaurantTestDataService) {
		this.restaurantTestDataService = restaurantTestDataService;
	}

	@Scheduled(initialDelay = 10000, fixedRate = 60000)
	public void runTests() {
		restaurantTestDataService.runNplusOne_1();
		restaurantTestDataService.runNplusOne_2();
		restaurantTestDataService.runNplusOne_3();

		PageRequest pageRequest = PageRequest.of(0, 2);
		restaurantTestDataService.runPagination_1(pageRequest);
	}

}
