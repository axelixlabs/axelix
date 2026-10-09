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

	private final DefaultRestaurantTestDataService restaurantTestDataService;

	public RestaurantTestDataQueryRunner(DefaultRestaurantTestDataService restaurantTestDataService) {
		this.restaurantTestDataService = restaurantTestDataService;
	}

	@Scheduled(initialDelay = 10000, fixedRate = 60000)
	public void runTests() {
		restaurantTestDataService.txOnInterfaceDefaultMethod_runNplusOne_1();
		restaurantTestDataService.txOnAbstractClassMethod_runNplusOne_2();
		restaurantTestDataService.txOnClassMethod_runNplusOne_3();

		PageRequest pageRequest = PageRequest.of(0, 2);
		restaurantTestDataService.txJavax_runPagination_1(pageRequest);
	}

}
