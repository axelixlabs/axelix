package org.springframework.samples.petclinic.testdata;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transactions declared on methods: of the class itself, of its interface and of its
 * abstract parent.
 */
@Service
@Profile({ "default", "local" })
public class DefaultRestaurantTestDataService extends AbstractRestaurantTestDataService
		implements InterfaceRestaurantTestDataService {

	private final RestaurantRepository restaurantRepository;

	private final DishRepository dishRepository;

	public DefaultRestaurantTestDataService(RestaurantRepository restaurantRepository, ChefRepository chefRepository,
			DishRepository dishRepository) {
		super(chefRepository);
		this.restaurantRepository = restaurantRepository;
		this.dishRepository = dishRepository;
	}

	@Override
	public void loadRestaurantsWithChefs() {
		List<Restaurant> restaurants = restaurantRepository.findAll();
		restaurants.forEach(r -> r.getChefs().size());

		sleepRandom();
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void txOnClassMethod_runNplusOne_3() {
		List<Dish> dishes = dishRepository.findAll();
		dishes.forEach(d -> d.getIngredients().size());

		sleepRandom();
	}

	@javax.transaction.Transactional(javax.transaction.Transactional.TxType.REQUIRES_NEW)
	public void txJavax_runPagination_1(PageRequest page) {
		chefRepository.findAllWithDishesPaged(page);

		sleepRandom();
	}

}
