package org.springframework.samples.petclinic.testdata.restaurant;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTestDataQueryRunner {

	private final RestaurantTestDataService restaurantTestDataService;

	public RestaurantTestDataQueryRunner(RestaurantTestDataService restaurantTestDataService) {
		this.restaurantTestDataService = restaurantTestDataService;
	}

	@EventListener(ApplicationReadyEvent.class)
	public void runTests() {
		restaurantTestDataService.runNplusOne_1();
		restaurantTestDataService.runNplusOne_2();
		restaurantTestDataService.runNplusOne_3();

		PageRequest pageRequest = PageRequest.of(0, 2);
		restaurantTestDataService.runPagination_1(pageRequest);
	}

}
