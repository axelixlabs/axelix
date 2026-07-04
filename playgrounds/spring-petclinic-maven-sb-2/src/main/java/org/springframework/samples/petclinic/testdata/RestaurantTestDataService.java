package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Profile({ "default", "local" })
public class RestaurantTestDataService {

	private final RestaurantRepository restaurantRepository;

	private final ChefRepository chefRepository;

	private final DishRepository dishRepository;

	public RestaurantTestDataService(RestaurantRepository restaurantRepository, ChefRepository chefRepository,
			DishRepository dishRepository) {
		this.restaurantRepository = restaurantRepository;
		this.chefRepository = chefRepository;
		this.dishRepository = dishRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_1() {
		List<Restaurant> restaurants = restaurantRepository.findAll();
		restaurants.forEach(r -> r.getChefs().size());

		sleepRandom();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_2() {
		List<Chef> chefs = chefRepository.findAll();
		chefs.forEach(c -> c.getDishes().size());

		sleepRandom();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_3() {
		List<Dish> dishes = dishRepository.findAll();
		dishes.forEach(d -> d.getIngredients().size());

		sleepRandom();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runPagination_1(PageRequest page) {
		chefRepository.findAllWithDishesPaged(page);

		sleepRandom();
	}

	private void sleepRandom() {
		try {
			long delay = ThreadLocalRandom.current().nextLong(10, 50 + 1);
			Thread.sleep(delay);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
	}

}
