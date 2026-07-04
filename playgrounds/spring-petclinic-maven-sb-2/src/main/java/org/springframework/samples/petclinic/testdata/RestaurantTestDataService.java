package org.springframework.samples.petclinic.testdata.restaurant;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
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
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_2() {
		List<Chef> chefs = chefRepository.findAll();
		chefs.forEach(c -> c.getDishes().size());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runNplusOne_3() {
		List<Dish> dishes = dishRepository.findAll();
		dishes.forEach(d -> d.getIngredients().size());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void runPagination_1(PageRequest page) {
		chefRepository.findAllWithDishesPaged(page);
	}

}
